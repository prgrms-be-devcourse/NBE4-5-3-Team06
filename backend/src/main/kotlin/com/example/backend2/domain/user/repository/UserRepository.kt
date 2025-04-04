package com.example.backend2.domain.user.repository

import com.example.backend2.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailOrNickname(email: String, nickname: String): User?
    fun findByUserUUID(userUUID: String): User?
    fun findByEmail(email: String): User?
}