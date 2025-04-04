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
class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUCTION_ID")
    var auctionId: Long? = null

    @OneToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    lateinit var product: Product

    @Column(name = "START_PRICE")
    var startPrice: Int? = null

    @Column(name = "MIN_BID")
    var minBid: Int? = null

    @Column(name = "START_TIME")
    lateinit var startTime: LocalDateTime

    @Column(name = "END_TIME")
    lateinit var endTime: LocalDateTime

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    lateinit var status: AuctionStatus

    @OneToOne(mappedBy = "auction", cascade = [CascadeType.ALL])
    var winner: Winner? = null

    @OneToMany(mappedBy = "auction", cascade = [CascadeType.ALL])
    var bids: MutableList<Bid>? = mutableListOf()

    @CreatedDate
    var createdAt: LocalDateTime? = null

    constructor()

    constructor(
        product: Product,
        startPrice: Int?,
        minBid: Int?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        status: AuctionStatus,
        winner: Winner?,
        bids: MutableList<Bid> = mutableListOf(),
    ) {
        this.product = product
        this.startPrice = 0
        this.minBid = 0
        this.startTime = startTime
        this.endTime = endTime
        this.status = status
        this.winner = winner
        this.bids = bids
    }

    fun setStatus(status: AuctionStatus) {
        this.status = status;
    }

    // 낙찰자 설정 메서드
    fun setWinner(winner: Winner) {
        this.winner = winner;
    }
}