@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.product.entity

import com.example.backend2.domain.auction.entity.Auction
import jakarta.persistence.*

@Entity
@Table(name = "PRODUCT_TABLE")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    val productId: Long? = null,
    @Column(name = "PRODUCT_NAME", nullable = false)
    val productName: String = "",
    @Column(name = "IMAGE_URL")
    val imageUrl: String? = null,
    @Column(name = "DESCRIPTION")
    val description: String? = null,
    @OneToOne(mappedBy = "product", cascade = [CascadeType.ALL])
    val auction: Auction? = null,
)
