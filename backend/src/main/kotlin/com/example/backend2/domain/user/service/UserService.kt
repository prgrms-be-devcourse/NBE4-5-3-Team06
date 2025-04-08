@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.user.service

import com.example.backend2.data.Role
import com.example.backend2.domain.user.dto.*
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.utils.JwtProvider
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val emailService: EmailService,
) {

    fun getUserCheck(userUUID: String): UserCheckRequest {
        val user = findUserByUUID(userUUID)
        return UserCheckRequest.from(user)
    }

    fun signup(request: UserSignUpRequest): UserSignUpResponse {
        validateEmailVerification(request.email)

        userRepository.findByEmailOrNickname(request.email, request.nickname).ifPresent {
            throw ServiceException(HttpStatus.CONFLICT.value().toString(), "이미 사용 중인 이메일 또는 닉네임입니다.")
        }

        val newUser = User(
            userUUID = "${System.currentTimeMillis()}-${UUID.randomUUID()}",
            email = request.email,
            password = passwordEncoder.encode(request.password),
            nickname = request.nickname,
            role = Role.USER,
        )

        userRepository.save(newUser)
        emailService.deleteVerificationCode(request.email)

        return UserSignUpResponse.from(newUser)
    }

    fun login(request: UserSignInRequest): UserSignInResponse {
        val user = userRepository.findByEmail(request.email).orElseThrow {
            unauthorizedException()
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw unauthorizedException()
        }

        val token = jwtProvider.generateToken(createJwtClaims(user), user.email)
        return UserSignInResponse.from(user, token)
    }

    fun updateUser(userUUID: String, request: UserPutRequest): UserPutRequest {
        val user = findUserByUUID(userUUID)

        val updatedUser = user.copy(
            nickname = request.nickname ?: user.nickname,
            profileImage = request.profileImage ?: user.profileImage,
            email = request.email ?: user.email,
        )

        return UserPutRequest.from(userRepository.save(updatedUser))
    }

    // ---------- Private Helper Methods ----------

    private fun findUserByUUID(userUUID: String): User =
        userRepository.findByUserUUID(userUUID)
            .orElseThrow { ServiceException("400", "사용자가 존재하지 않습니다.") }

    private fun validateEmailVerification(email: String) {
        if (emailService.isVerificationExpired(email)) {
            throw ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "이메일 인증이 만료되었습니다. 다시 인증해 주세요.")
        }
        if (!emailService.isVerified(email)) {
            throw ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "이메일 인증이 완료되지 않았습니다.")
        }
    }

    private fun unauthorizedException(): ServiceException =
        ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "이메일 또는 비밀번호가 일치하지 않습니다.")

    private fun createJwtClaims(user: User): Map<String, Any> = mapOf(
        "userUUID" to user.userUUID,
        "nickname" to user.nickname,
        "role" to "ROLE_${user.role}"
    )

    // UUID를 기반으로 유저 검증
    fun getUserByUUID(userUUID: String): User =
        userRepository
            .findByUserUUID(userUUID)
            .orElseThrow { ServiceException("400", "사용자가 존재하지 않습니다.") }

}