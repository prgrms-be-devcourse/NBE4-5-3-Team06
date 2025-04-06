package com.example.backend2.domain.winner.dto

import com.example.backend2.domain.winner.entity.Winner
import java.time.LocalDateTime

data class WinnerCheckResponse(
    val auctionId: Long,
    val productName: String,
    val description: String,
    val winningBid: Int,
    val winTime: LocalDateTime,
    val imageUrl: String,
) {
    // Winner 엔티티를 낙찰자 조회 응답을 위한 DTO 로 변환
    companion object {
        fun from(winner: Winner): WinnerCheckResponse {
            val base =
                WinnerCheckResponse(
                    auctionId = winner.auction.auctionId ?: 0,
                    productName = winner.auction.product?.productName ?: "",
                    description = winner.auction.product?.description ?: "",
                    winningBid = winner.winningBid ?: 0,
                    winTime = winner.winTime,
                    imageUrl = winner.auction.product?.imageUrl ?: "",
                )
            return base
        }
    }
}
