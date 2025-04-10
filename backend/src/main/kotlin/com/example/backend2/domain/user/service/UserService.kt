@file:Suppress("ktlint:standard:no-wildcard-imports") // 코틀린

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
        val user = getUserByUUID(userUUID) // userUUID로 조회
        return UserCheckRequest.from(user) // DTO로 반환한다
    }

    fun signup(request: UserSignUpRequest): UserSignUpResponse {
        // 테스트용 계정은 이메일 인증 건너뛰기
        if (!request.skipEmailVerification) {
            if (emailService.isVerificationExpired(request.email)) {
                throw ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "이메일 인증이 만료되었습니다. 다시 인증해 주세요.")
            }

            if (!emailService.isVerified(request.email)) {
                throw ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "이메일 인증이 완료되지 않았습니다.")
            }
        }

        // 이메일 혹은 닉네임으로 존재하는 유저가 있는지 확인
        val existingUser = userRepository.findByEmailOrNickname(request.email, request.nickname)

        // 이메일 혹은 닉네임이 중복되는 경우 예외 처리
        if (existingUser.isPresent) {
            throw ServiceException(HttpStatus.CONFLICT.value().toString(), "이미 사용 중인 이메일 또는 닉네임입니다.")
        }

        // User 엔티티 생성
        val user =
            User(
                userUUID = "${System.currentTimeMillis()}-${UUID.randomUUID()}",
                email = request.email,
                password = passwordEncoder.encode(request.password),
                nickname = request.nickname,
                role = Role.USER,
            )

        // 데이터베이스에 저장
        userRepository.save(user)

        // Redis에서 인증 정보 삭제 (skipEmailVerification이 false인 경우에만)
        if (!request.skipEmailVerification) {
            emailService.deleteVerificationCode(request.email)
        }

        return UserSignUpResponse.from(user)
    }

    // UUID를 기반으로 유저 검증
    fun getUserByUUID(userUUID: String): User =
        userRepository
            .findByUserUUID(userUUID)
            .orElseThrow { ServiceException("400", "사용자가 존재하지 않습니다.") }

    fun login(request: UserSignInRequest): UserSignInResponse {
        // 이메일을 기준으로 사용자를 찾음 (존재하지 않으면 예외 발생)
        val user = userRepository
            .findByEmail(request.email)
            .orElseThrow {
                ServiceException(
                    HttpStatus.UNAUTHORIZED.value().toString(),
                    "이메일 또는 비밀번호가 일치하지 않습니다."
                )
            }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ServiceException(
                HttpStatus.UNAUTHORIZED.value().toString(),
                "이메일 또는 비밀번호가 일치하지 않습니다."
            )
        }
        // JWT 토큰 발행 시 포함할 사용자 정보 설정
        val claims = hashMapOf<String, Any>()
        claims["userUUID"] = user.userUUID
        claims["nickname"] = user.nickname
        claims["role"] = "ROLE_${user.role}"

        // JWT 토큰 생성
        val token = jwtProvider.generateToken(claims, request.email)

        // 응답 객체 생성 및 반환
        return UserSignInResponse.from(user, token)
    }

    fun updateUser(
        userUUID: String,
        request: UserPutRequest,
    ): UserPutRequest {
        val user =
            userRepository
                .findByUserUUID(userUUID)
                .orElseThrow { ServiceException("404", "사용자 정보를 찾을 수 없습니다.") }

        // copy()를 사용하여 필요한 속성만 변경
        val updatedUser =
            user.copy(
                nickname = request.nickname ?: user.nickname,
                profileImage = request.profileImage ?: user.profileImage,
                email = request.email ?: user.email,
            )

        return UserPutRequest.from(userRepository.save(updatedUser))
    }

    /**
     * 테스트 계정(example.com 도메인 이메일)만 삭제하는 메서드
     * @return 삭제된 계정 수
     */
    fun deleteTestAccounts(): Int {
        val testEmailPattern = "%@example.com"
        val testAccounts = userRepository.findAllByEmailPattern(testEmailPattern)
        val count = testAccounts.size
        
        if (count > 0) {
            userRepository.deleteAll(testAccounts)
        }
        
        return count
    }

    fun findOrCreateGoogleUser(email: String): User {
        return userRepository.findByEmail(email).orElseGet {
            val newUser = User(
                userUUID = "${System.currentTimeMillis()}-${UUID.randomUUID()}",
                email = email,
                nickname = email.substringBefore("@"),
                password = passwordEncoder.encode("google"), // 임시 비번
                profileImage = null,
                role = Role.USER
            )
            userRepository.save(newUser)
        }
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("해당 이메일로 사용자를 찾을 수 없습니다.") }
    }

}
