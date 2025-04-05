@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.user.repository

import com.example.backend2.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailOrNickname(
        email: String,
        nickname: String,
    ): Optional<User>

    fun findByUserUUID(userUUID: String): Optional<User>

    fun findByEmail(email: String): Optional<User>
}
