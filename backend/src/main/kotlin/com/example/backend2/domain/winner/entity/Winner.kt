@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.winner.entity

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "WINNER_TABLE")
data class Winner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WINNER_ID")
    val winnerId: Long? = null,
    @ManyToOne
    @JoinColumn(name = "USER_UUID", nullable = false)
    val user: User =
        User(
            userUUID = "",
            email = "",
            nickname = "",
            password = "",
            role = com.example.backend2.data.Role.USER,
        ),
    @OneToOne
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    val auction: Auction =
        Auction(
            product =
                com.example.backend2.domain.product.entity
                    .Product(),
            startPrice = 0,
            minBid = 0,
        ),
    @Column(name = "WINNING_BID")
    val winningBid: Int? = 0,
    @Column(name = "WIN_TIME")
    val winTime: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun createWinner(
            user: User,
            auction: Auction,
            winningBid: Int?,
            winTime: LocalDateTime,
        ): Winner =
            Winner(
                user = user,
                auction = auction,
                winningBid = winningBid,
                winTime = winTime,
            )
    }
}
