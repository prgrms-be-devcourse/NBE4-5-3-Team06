package com.example.backend2.domain.user.entity

import com.example.backend2.data.Role
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.winner.entity.Winner
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "USER_TABLE")
class User(
    @Id
    @Column(name = "USER_UUID", nullable = false, length = 50)
    var userUUID: String,

    @Column(name = "EMAIL", nullable = false, unique = true)
    var email: String,

    @Column(name = "NICKNAME", nullable = false, unique = true)
    var nickname: String,

    @Column(name = "PASSWORD", nullable = false)
    var password: String,

    @Column(name = "profileImage", columnDefinition = "TEXT")
    var profileImage: String? = null,

    @Column(name = "CREATED_DATE")
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "MODIFIED_AT")
    var modifiedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    var role: Role = Role.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var winners: MutableList<Winner> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var bids: MutableList<Bid> = mutableListOf()
) {
    // 기본 생성자
    constructor() : this(
        userUUID = "",
        email = "",
        nickname = "",
        password = "",
        profileImage = null,
        createdDate = LocalDateTime.now(),
        modifiedAt = LocalDateTime.now(),
        role = Role.USER
    )

    // (회원가입 시 사용)
    constructor(userUUID: String, email: String, nickname: String, password: String, role: Role) : this(
        userUUID = userUUID,
        email = email,
        nickname = nickname,
        password = password,
        profileImage = null,
        createdDate = LocalDateTime.now(),
        modifiedAt = LocalDateTime.now(),
        role = role,
        winners = mutableListOf(),
        bids = mutableListOf()
    )

    // 프로필 이미지 수정 메서드
    fun updateProfileImage(newProfileImage: String) {
        this.profileImage = newProfileImage
        this.modifiedAt = LocalDateTime.now()
    }

    // 닉네임 변경 메서드
    fun updateNickname(newNickname: String) {
        this.nickname = newNickname
        this.modifiedAt = LocalDateTime.now()
    }

    // 닉네임 변경 메서드
    fun updateSetEmail(email: String) {
        this.email = email
        this.modifiedAt = LocalDateTime.now()
    }
}
