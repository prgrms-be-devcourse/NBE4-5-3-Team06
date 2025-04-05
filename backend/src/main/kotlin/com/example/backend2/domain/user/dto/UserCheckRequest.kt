package com.example.backend2.domain.user.dto

import com.example.backend2.domain.user.entity.User
import java.time.LocalDateTime

data class UserCheckRequest(
    val userUUID: String,
    val nickname: String,
    val email: String,
    val createdAt: LocalDateTime,
    val profileImage: String?,
) {
    companion object {
        fun from(user: User): UserCheckRequest =
            UserCheckRequest(
                userUUID = user.userUUID,
                nickname = user.nickname,
                email = user.email,
                createdAt = user.createdDate,
                profileImage = user.profileImage,
            )
    }
}
