package com.example.backend2.domain.winner.repository

import com.example.backend2.domain.winner.entity.Winner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WinnerRepository : JpaRepository<Winner, Long> {
    fun findByUserUserUUID(userUUID: String): List<Winner>
}
