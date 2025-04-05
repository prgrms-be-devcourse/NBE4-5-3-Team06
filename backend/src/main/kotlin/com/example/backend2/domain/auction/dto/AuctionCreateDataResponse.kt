package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import java.time.LocalDateTime

data class AuctionCreateDataResponse(
    val auctionId: Long?,
    val productId: Long?,
    val startPrice: Int,
    val minBid: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String
) {
    companion object {
        fun from(auction: Auction): AuctionCreateDataResponse {
            val base = AuctionCreateDataResponse(
                auctionId = auction.auctionId,
                productId = auction.product.productId,
                startPrice = auction.startPrice,
                minBid = auction.minBid,
                startTime = auction.startTime,
                endTime = auction.endTime,
                status = auction.status.name
            )
            
            return base
        }
    }
} 