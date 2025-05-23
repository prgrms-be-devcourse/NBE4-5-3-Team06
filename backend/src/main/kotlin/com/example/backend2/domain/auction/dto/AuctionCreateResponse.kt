package com.example.backend2.domain.auction.dto

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.dto.ProductResponse

data class AuctionCreateResponse(
    val auction: AuctionCreateDataResponse,
    val product: ProductResponse,
) {
    companion object {
        fun from(auction: Auction): AuctionCreateResponse {
            val product = auction.product ?: throw IllegalStateException("Product cannot be null for auction creation")
            
            val base =
                AuctionCreateResponse(
                    auction = AuctionCreateDataResponse.from(auction),
                    product = ProductResponse.from(product),
                )

            return base
        }
    }
}
