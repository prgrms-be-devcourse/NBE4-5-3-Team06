@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.auction.entity

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.winner.entity.Winner
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "AUCTION_TABLE")
data class Auction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUCTION_ID")
    val auctionId: Long? = null,
    @OneToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    val product: Product? = null,
    @Column(name = "START_PRICE")
    val startPrice: Int = 0,
    @Column(name = "MIN_BID")
    val minBid: Int = 0,
    @Column(name = "START_TIME")
    val startTime: LocalDateTime = LocalDateTime.now(),
    @Column(name = "END_TIME")
    val endTime: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    val status: AuctionStatus = AuctionStatus.UPCOMING,
    @OneToOne(mappedBy = "auction", cascade = [CascadeType.ALL])
    val winner: Winner? = null,
    @OneToMany(mappedBy = "auction", cascade = [CascadeType.ALL])
    val bids: MutableList<Bid> = mutableListOf(),
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    // 상태 변경
    fun setStatus(status: AuctionStatus): Auction = copy(status = status)

    // 낙찰자 설정 메서드
    fun setWinner(winner: Winner): Auction = copy(winner = winner)
}
