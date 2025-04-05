package com.example.backend2.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailOrNickname(
        email: String,
        nickname: String,
    ): Optional<User>

    fun findByUserUUID(userUUID: String): Optional<User>

    fun findByEmail(email: String): Optional<User>
}
