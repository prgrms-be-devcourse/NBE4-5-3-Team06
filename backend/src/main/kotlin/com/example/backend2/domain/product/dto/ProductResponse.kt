package com.example.backend2.domain.product.dto

import com.example.backend2.domain.product.entity.Product

data class ProductResponse(
    val productId: Long?,
    val productName: String,
    val imageUrl: String,
    val description: String,
) {
    // 엔티티를 DTO로 변환
    companion object {
        fun from(product: Product): ProductResponse {
            val base =
                ProductResponse(
                    productId = product.productId,
                    productName = product.productName,
                    imageUrl = product.imageUrl ?: "",
                    description = product.description ?: "",
                )
            return base
        }
    }
}
