@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.user.repository

import com.example.backend2.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailOrNickname(
        email: String,
        nickname: String,
    ): Optional<User>

    fun findByUserUUID(userUUID: String): Optional<User>

    fun findByEmail(email: String): Optional<User>
    
    /**
     * 이메일 패턴에 맞는 모든 사용자를 찾습니다. (테스트 계정 삭제용)
     * @param pattern 이메일 패턴 (SQL LIKE 패턴)
     * @return 패턴에 일치하는 모든 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE :pattern")
    fun findAllByEmailPattern(@Param("pattern") pattern: String): List<User>
}
