package com.example.backend2.domain.user.service

import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.utils.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes

        // 구글에서 email, name 등 추출 (키 이름은 구글의 응답에 따라 다름)
        val email = attributes["email"] as? String ?: throw OAuth2AuthenticationException("이메일 정보가 없습니다.")
        val name = attributes["name"] as? String ?: "Unknown"

        // DB에서 사용자 검색, 없으면 신규 등록
        val user = userRepository.findByEmail(email).orElseGet {
            val newUser = User(
                email = email,
                nickname = name,
                password = passwordEncoder.encode("google") // 구글 로그인으로 비밀번호는 고정 처리
                // 기타 초기값
            )
            userRepository.save(newUser)
        }

        // JWT 토큰 발행 (기존 로그인 방식과 유사하게)
        val claims = hashMapOf<String, Any>(
            "userUUID" to user.userUUID,
            "nickname" to user.nickname,
            "role" to "ROLE_${user.role}"
        )
        val token = jwtProvider.generateToken(claims, email)
        // 여기서 사용자 정보, 토큰 등을 추가하여 커스터마이징 가능
        // 예: OAuth2User 확장을 통해 토큰 포함 객체 생성 가능

        // 기본적으로 oAuth2User를 반환 (필요 시 변환)
        return oAuth2User
    }
}
