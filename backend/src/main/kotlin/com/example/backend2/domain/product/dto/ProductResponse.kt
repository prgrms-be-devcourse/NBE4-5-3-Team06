package com.example.backend2.domain.product.dto

import com.example.backend2.domain.product.entity.Product

class ProductResponse(
    val productId: Long? = null,
    val productName: String,
    val imageUrl: String? = null,
    val description: String? = null,
) {
    companion object {
        // 엔티티를 DTO로 변환
        fun from(product: Product): ProductResponse =
            ProductResponse(
                productId = product.productId,
                productName = product.productName,
                imageUrl = product.imageUrl,
                description = product.description,
            )
    }
}
