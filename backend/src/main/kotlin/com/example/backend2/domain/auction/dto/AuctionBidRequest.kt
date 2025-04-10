package com.example.backend2.domain.auction.dto

data class AuctionBidRequest(
    val auctionId: Long,
    val amount: Int,
    val token: String,
)
