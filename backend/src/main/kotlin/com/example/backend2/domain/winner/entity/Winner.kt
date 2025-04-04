package com.example.backend2.domain.winner.entity

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "WINNER_TABLE")
class Winner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WINNER_ID")
    var winnerId: Long? = null

    @ManyToOne
    @JoinColumn(name = "USER_UUID", nullable = false)
    lateinit var user: User

    @OneToOne
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    lateinit var auction: Auction

    @Column(name = "WINNING_BID")
    var winningBid: Int? = null

    @Column(name = "WIN_TIME")
    lateinit var winTime: LocalDateTime

    constructor()

    constructor(
        user: User,
        auction: Auction,
        winningBid: Int?,
        winTime: LocalDateTime
    ) {
        this.user = user
        this.auction = auction
        this.winningBid = winningBid
        this.winTime = winTime
    }
}