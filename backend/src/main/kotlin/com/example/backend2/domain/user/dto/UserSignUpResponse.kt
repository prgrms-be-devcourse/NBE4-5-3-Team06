package com.example.backend2.domain.user.dto

import com.example.backend2.domain.user.entity.User

data class UserSignUpResponse(
    val userUUID: String,
) {
    companion object {
        fun from(user: User): UserSignUpResponse =
            UserSignUpResponse(
                userUUID = user.userUUID,
            )
    }
} 
