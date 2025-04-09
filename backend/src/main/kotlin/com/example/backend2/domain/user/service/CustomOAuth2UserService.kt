package com.example.backend2.domain.user.service

import com.example.backend2.data.Role
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.utils.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes

        val email = attributes["email"] as? String ?: throw OAuth2AuthenticationException("이메일 정보가 없습니다.")
        val name = attributes["name"] as? String ?: "Unknown"
        val profileImage = attributes["picture"] as? String

        val user = userRepository.findByEmail(email).orElseGet {
            val newUser = User(
                userUUID = "${System.currentTimeMillis()}-${UUID.randomUUID()}",
                email = email,
                nickname = name,
                password = passwordEncoder.encode("google"),
                profileImage = profileImage,
                role = Role.USER
            )
            userRepository.save(newUser)
        }

        val claims = hashMapOf<String, Any>(
            "userUUID" to user.userUUID,
            "nickname" to user.nickname,
            "role" to "ROLE_${user.role}"
        )
        val token = jwtProvider.generateToken(claims, email)

        return CustomOAuth2User(oAuth2User, token)
    }
}
