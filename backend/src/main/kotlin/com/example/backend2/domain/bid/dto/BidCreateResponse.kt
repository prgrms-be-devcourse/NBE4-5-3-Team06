package com.example.backend2.domain.bid.dto

import com.example.backend2.domain.bid.entity.Bid
import java.time.LocalDateTime

data class BidCreateResponse(
    val auctionId: Long,
    val userUUID: String,
    val title: String,
    val bidAmount: Int,
    val bidTime: LocalDateTime,
    val nickname: String,
) {
    companion object {
        fun from(bid: Bid): BidCreateResponse {
            val auction = bid.auction ?: throw IllegalStateException("Auction cannot be null")
            val product = auction.product
            val user = bid.user ?: throw IllegalStateException("User cannot be null")

            return BidCreateResponse(
                auctionId = auction.auctionId ?: throw IllegalStateException("Auction ID cannot be null"),
                userUUID = user.userUUID,
                title = product.productName,
                bidAmount = bid.amount,
                bidTime = bid.bidTime,
                nickname = user.nickname,
            )
        }
    }
}
