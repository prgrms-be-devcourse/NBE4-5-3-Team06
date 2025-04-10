package com.example.backend2.global.app


import com.example.backend2.domain.user.service.CustomOAuth2User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // CustomOAuth2UserService에서 반환한 CustomOAuth2User를 꺼내 JWT 토큰을 추출
        val principal = authentication.principal
        val jwtToken = if (principal is CustomOAuth2User) {
            principal.token
        } else {
            "" // 또는 기본값 처리
        }

        // 방법 1: JWT 토큰을 URL 파라미터로 전달하여 리다이렉트 (비추천: 토큰 노출 우려)
        // val redirectUrl = "http://localhost:3000?token=$jwtToken"

        // 방법 2: JWT 토큰을 쿠키에 설정하여 클라이언트에 전달
        response.addHeader("Set-Cookie", "accessToken=$jwtToken; HttpOnly; Path=/")

        // 인증 성공 후 리다이렉트할 URL (예: 메인 페이지 또는 사용자 대시보드)
        response.sendRedirect("http://localhost:3000")
    }
}
