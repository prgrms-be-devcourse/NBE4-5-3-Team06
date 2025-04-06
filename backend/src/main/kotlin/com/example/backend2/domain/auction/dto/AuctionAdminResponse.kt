package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import java.time.LocalDateTime

data class AuctionAdminResponse(
    val auctionId: Long?,
    val productName: String,
    val imageUrl: String? = "",
    val startPrice: Int,
    val currentPrice: Int,
    val status: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val nickname: String? = null,
    val winningBid: Int? = null,
    val winTime: LocalDateTime? = null,
) {
    companion object {
        fun from(
            auction: Auction,
            currentPrice: Int,
        ): AuctionAdminResponse {
            val base =
                AuctionAdminResponse(
                    auctionId = auction.auctionId,
                    productName = auction.product?.productName ?: "",
                    imageUrl = auction.product?.imageUrl,
                    startPrice = auction.startPrice,
                    currentPrice = currentPrice,
                    status = auction.status.name,
                    startTime = auction.startTime,
                    endTime = auction.endTime,
                )

            return if (auction.status.name == "FINISHED" && auction.winner != null) {
                base.copy(
                    nickname = auction.winner.user.nickname ?: "없음",
                    winningBid = auction.winner.winningBid ?: 0,
                    winTime = auction.winner.winTime,
                )
            } else {
                base.copy(
                    nickname = "없음",
                    winningBid = 0,
                    winTime = null,
                )
            }
        }
    }
}
