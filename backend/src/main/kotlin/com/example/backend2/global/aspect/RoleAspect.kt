package com.example.backend2.global.aspect

import com.example.backend2.data.Role
import com.example.backend2.global.annotation.HasRole
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class RoleAspect {
    private val log by lazy { LoggerFactory.getLogger(this::class.java) }

    // @HasRole 어노테이션이 메서드나 클래스에 있으면 이 메서드 실행
    @Around("@annotation(hasRole)")
    @Throws(Throwable::class)
    fun checkRole(
        joinPoint: ProceedingJoinPoint,
        hasRole: HasRole,
    ): Any {
        val requiredRole: Role = hasRole.value
        val auth: Authentication = SecurityContextHolder.getContext().authentication

        if (auth == null || !auth.isAuthenticated) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.")
        }

        // 사용자가 요구된 역할을 가지고 있는지 체크 (예: ROLE_ADMIN 이 있는지 확인)
        val hasAuthority =
            auth.authorities
                .map { it.authority }
                .any { it == "ROLE_${requiredRole.name}" }

        if (!hasAuthority) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }

        log.info("권한 확인 성공: {}", requiredRole)
        return joinPoint.proceed() // 권한이 있으면, 원래 메서드를 계속 진행하도록 허용
    }
}
