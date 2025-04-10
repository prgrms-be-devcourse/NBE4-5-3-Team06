package com.example.backend2.domain.user.dto

data class UserSignInRequest(
    val email: String,
    val password: String
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수 입력 항목입니다." }
        require(email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) { "이메일 형식이 올바르지 않습니다." }
        require(password.isNotBlank()) { "비밀번호는 필수 입력 항목입니다." }
        require(password.length >= 8) { "비밀번호는 최소 8자 이상이어야 합니다." }
    }
} 