package com.example.backend2.domain.user.dto

data class UserSignUpRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val skipEmailVerification: Boolean = false // 이메일 인증 건너뛰기 옵션 (테스트용)
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수 입력 항목입니다." }
        require(email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) { "이메일 형식이 올바르지 않습니다." }
        require(password.isNotBlank()) { "비밀번호는 필수 입력 항목입니다." }
        require(password.length >= 8) { "비밀번호는 최소 8자 이상이어야 합니다." }
        require(nickname.isNotBlank()) { "닉네임은 필수 입력 항목입니다." }
        require(nickname.length in 3..16) { "닉네임은 3 ~ 16자 사이어야 합니다." }
    }
} 