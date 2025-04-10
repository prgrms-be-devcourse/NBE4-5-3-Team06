@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.user.entity

import com.example.backend2.data.Role
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.winner.entity.Winner
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "USER_TABLE")
data class User(
    @Id
    @Column(name = "USER_UUID", nullable = false, length = 50)
    val userUUID: String = "",
    @Column(name = "EMAIL", nullable = false, unique = true)
    var email: String = "",
    @Column(name = "NICKNAME", nullable = false, unique = true)
    var nickname: String = "",
    @Column(name = "PASSWORD", nullable = false)
    val password: String = "",
    @Column(name = "PROFILE_IMAGE", columnDefinition = "TEXT")
    var profileImage: String? = null,
    @Column(name = "CREATED_DATE")
    val createdDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "MODIFIED_AT")
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    val role: Role = Role.USER,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val winners: List<Winner> = mutableListOf(),
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val bids: List<Bid> = mutableListOf(),
)
