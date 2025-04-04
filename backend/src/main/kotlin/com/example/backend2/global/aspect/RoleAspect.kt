package com.example.backend2.global.aspect

import org.example.bidflow.data.Role

@lombok.extern.slf4j.Slf4j
@org.aspectj.lang.annotation.Aspect
@org.springframework.stereotype.Component
class RoleAspect {
    // @HasRole 어노테이션이 메서드나 클래스에 있으면 이 메서드 실행
    @Around("@annotation(hasRole)")
    @Throws(Throwable::class)
    fun checkRole(joinPoint: ProceedingJoinPoint, hasRole: HasRole): Any {
        // 어노테이션에서 필요한 역할(Role)을 가져옴 (예: "ADMIN")

        val requiredRole: Role = hasRole.value()

        // 현재 메서드를 호출하는 사용자의 인증 정보를 SecurityContext 에서 가져옴
        val auth: org.springframework.security.core.Authentication =
            SecurityContextHolder.getContext().getAuthentication()

        // 인증되지 않았거나, 로그인이 안 된 상태면 예외를 발생시킴 (401 UNAUTHORIZED)
        if (auth == null || !auth.isAuthenticated) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.")
        }

        // 사용자가 요구된 역할을 가지고 있는지 체크 (예: ROLE_ADMIN 이 있는지 확인)
        val hasAuthority = auth.authorities.stream()
            .map<String> { obj: GrantedAuthority -> obj.getAuthority() }
            .anyMatch { authority: String -> authority == "ROLE_" + requiredRole.name() }

        // 만약 사용자가 역할이 없다면 예외를 발생 (403 FORBIDDEN)
        if (!hasAuthority) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "권한이 없습니다.")
        }

        RoleAspect.log.info("권한 확인 성공: {}", requiredRole)
        return joinPoint.proceed() // 권한이 있으면, 원래 메서드를 계속 진행하도록 허용
    }
}