package com.example.backend2.domain.user.dto

data class EmailVerificationRequest(
    val email: String,
    val code: String
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수 입력 항목입니다." }
        require(email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) { "이메일 형식이 올바르지 않습니다." }
        require(code.isNotBlank()) { "인증 코드는 필수 입력 항목입니다." }
    }
} 