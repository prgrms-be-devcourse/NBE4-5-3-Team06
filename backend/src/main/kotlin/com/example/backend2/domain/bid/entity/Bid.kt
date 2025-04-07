@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.bid.entity

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "BID_TABLE")
data class Bid(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BID_ID")
    val bidId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    val auction: Auction = Auction(
        product = com.example.backend2.domain.product.entity.Product(),
        startPrice = 0,
        minBid = 0
    ),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_UUID", nullable = false)
    val user: User = User(
        userUUID = "",
        email = "",
        nickname = "",
        password = "",
        role = com.example.backend2.data.Role.USER
    ),
    @Column(name = "AMOUNT")
    val amount: Int = 0,
    @Column(name = "BID_TIME")
    val bidTime: LocalDateTime = LocalDateTime.now(),
) {
    fun updateAmount(newAmount: Int): Bid =
        copy(
            amount = newAmount,
            bidTime = LocalDateTime.now(),
        )

    companion object {
        fun createBid(
            auction: Auction,
            user: User,
            amount: Int,
            bidTime: LocalDateTime,
        ): Bid =
            Bid(
                auction = auction,
                user = user,
                amount = amount,
                bidTime = bidTime,
            )
    }
}
