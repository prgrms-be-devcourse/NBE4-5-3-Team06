package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.dto.ProductResponse

data class AuctionCreateResponse(
    val auction: AuctionCreateDataResponse, // TODO: AuctionCreateDataResponse 만들기
    val product: ProductResponse,
) {
    companion object {
        fun from(auction: Auction) =
            AuctionCreateResponse(
                auction = AuctionCreateDataResponse.from(auction),
                product = ProductResponse.from(auction.product),
            )
    }
}
