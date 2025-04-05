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
        ): AuctionCheckResponse {
            // 공통 값 먼저 설정
            val base =
                AuctionCheckResponse(
                    auctionId = auction.auctionId,
                    productName = auction.product.productName,
                    imageUrl = auction.product.imageUrl ?: "",
                    currentBid = currentBid,
                    status = auction.status.toString(),
                    startTime = auction.startTime,
                    endTime = auction.endTime,
                )

            // 예: 상태가 CANCELLED면 이미지 URL을 기본 이미지로 바꾸고 반환
            // explain: 기본적으로 base 를 반환 but, 확장성 생각해서 이렇게 구현해 놨음.(주석은 구현하면서 지울 예정)
            return if (auction.status.name == "ONGOING") {
                base.copy(imageUrl = "https://example.com/default-image.png")
            } else {
                base
            }
        }
    }
}
