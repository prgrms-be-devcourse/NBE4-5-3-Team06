package com.example.backend2.domain.auction.dto

import java.time.LocalDateTime

data class AuctionRequest(
    val startPrice: Int? = null,
    val minBid: Int? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val productName: String,
    val imageUrl: String? = null,
    val description: String? = null
) {
    init {
        require(productName.isNotBlank()) { "상품 이름은 필수 입력 항목입니다." }
    }
} 