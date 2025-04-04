package com.example.backend2.domain.bid.entity

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "BID_TABLE")
class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BID_ID")
    var bidId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    var auction: Auction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_UUID", nullable = false)
    var user: User? = null

    @Column(name = "AMOUNT")
    var amount: Int? = null

    @Column(name = "BID_TIME")
    var bidTime: LocalDateTime? = null

    // 기본 생성자
    constructor() {}

    // Auction과 User만 받는 생성자
    constructor(auction: Auction, user: User) {
        this.auction = auction
        this.user = user
    }

    // 모든 필드를 받는 생성자
    constructor(auction: Auction, user: User, amount: Int?, bidTime: LocalDateTime?) {
        this.auction = auction
        this.user = user
        this.amount = amount
        this.bidTime = bidTime
    }

    // TODO: 왜 static 메서드여야만 하는가? - Ref.
    companion object {
        // 입찰을 생성하는 정적 메서드
        fun createBid(auction: Auction, user: User, amount: Int, bidTime: LocalDateTime): Bid {
            return Bid().apply {
                this.auction = auction
                this.user = user
                this.amount = amount
                this.bidTime = bidTime
            }
        }
    }

    // Bid 엔티티 수정
    fun updateAmount(newAmount: Int): Bid {
        this.amount = newAmount
        this.bidTime = LocalDateTime.now() // 금액 변경 시 입찰 시간 갱신
        return this // 갱신된 객체 반환
    }
}