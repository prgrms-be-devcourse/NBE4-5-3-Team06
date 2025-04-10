package com.example.backend2.domain.user.dto

data class EmailSendRequest(
    val email: String
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수 입력 항목입니다." }
        require(email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) { "이메일 형식이 올바르지 않습니다." }
    }
} 