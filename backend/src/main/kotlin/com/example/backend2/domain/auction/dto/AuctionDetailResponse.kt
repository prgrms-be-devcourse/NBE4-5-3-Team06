package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.dto.ProductResponse
import java.time.LocalDateTime

data class AuctionDetailResponse(
    val auctionId: Long?,
    val product: ProductResponse,
    val startPrice: Int,
    val currentBid: Int,
    val minBid: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String,
) {
    companion object {
        fun from(
            auction: Auction,
            amount: Int,
        ): AuctionDetailResponse {
            val product = auction.product ?: throw IllegalStateException("Product cannot be null for auction detail")
            
            val base =
                AuctionDetailResponse(
                    auctionId = auction.auctionId,
                    product = ProductResponse.from(product),
                    startPrice = auction.startPrice,
                    currentBid = amount,
                    minBid = auction.minBid,
                    startTime = auction.startTime,
                    endTime = auction.endTime,
                    status = auction.status.name,
                )

            return base
        }
    }
}
