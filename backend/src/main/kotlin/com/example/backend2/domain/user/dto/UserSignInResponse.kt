package com.example.backend2.domain.user.dto

import com.example.backend2.domain.user.entity.User

data class UserSignInResponse(
    val token: String, // 발급된 JWT 토큰
    val userUUID: String, // 사용자 고유 식별자
    val nickname: String, // 사용자 닉네임
) {
    companion object {
        // User 객체와 토큰을 받아 응답 객체를 생성하는 정적 메서드
        fun from(
            user: User,
            token: String,
        ): UserSignInResponse {
            val base =
                UserSignInResponse(
                    token = token,
                    userUUID = user.userUUID,
                    nickname = user.nickname,
                )
            return base
        }
    }
}
