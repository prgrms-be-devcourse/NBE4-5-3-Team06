package com.example.backend2.domain.user.dto

import com.example.backend2.domain.user.entity.User

data class UserPutRequest(
    val profileImage: String? = null,
    val nickname: String? = null,
    val email: String? = null,
) {
    companion object {
        fun from(user: User): UserPutRequest =
            UserPutRequest(
                profileImage = user.profileImage,
                nickname = user.nickname,
                email = user.email,
            )
    }
}
