package com.example.backend2.global.filter

import com.example.backend2.domain.user.service.JwtBlacklistService
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.utils.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val jwtBlacklistService: JwtBlacklistService,
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null) {
            // 블랙리스트 확인
            if (jwtBlacklistService.isBlacklisted(token)) {
                throw ServiceException(
                    HttpStatus.UNAUTHORIZED.value().toString(),
                    "로그아웃한 토큰으로 접근할 수 없습니다.",
                )
            }

            // 토큰 유효성 검사
            if (jwtProvider.validateToken(token)) {
                // 토큰에서 필요한 정보 추출
                val username = jwtProvider.getUsername(token)
                val role = jwtProvider.parseRole(token) // 👉 role 추출
                println("Extracted Role: $role")

                // 직접 UserDetails 생성
                val userDetails: UserDetails = User
                    .builder()
                    .username(username)
                    .password("") // 비밀번호는 인증에 필요하지 않음
                    .authorities(SimpleGrantedAuthority(role)) // 권한 설정
                    .build()

                println("Authorities: ${userDetails.authorities}")

                // 인증 객체 생성
                val authenticationToken =
                    UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities,
                    )

                // SecurityContextHolder에 인증 정보 등록
                SecurityContextHolder.getContext().authentication = authenticationToken
            }
        }

        filterChain.doFilter(request, response)
    }

    // 헤더에서 토큰 추출 (Authorization: Bearer <token>)
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
