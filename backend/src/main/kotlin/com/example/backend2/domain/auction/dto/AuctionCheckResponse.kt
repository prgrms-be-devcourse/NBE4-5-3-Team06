package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import java.time.LocalDateTime

data class AuctionCheckResponse(
    val auctionId: Long?,
    val productName: String,
    val imageUrl: String,
    val currentBid: Int,
    val status: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    companion object {
        fun from(
            auction: Auction,
            currentBid: Int,
        ) = AuctionCheckResponse(
            auctionId = auction.auctionId,
            productName = auction.product.productName,
            imageUrl = auction.product.imageUrl ?: "",
            currentBid = currentBid,
            status = auction.status.toString(),
            startTime = auction.startTime,
            endTime = auction.endTime,
        )
    }
} 
