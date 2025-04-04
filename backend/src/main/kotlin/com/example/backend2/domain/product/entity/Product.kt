package com.example.backend2.domain.product.entity

import com.example.backend2.domain.auction.entity.Auction
import jakarta.persistence.*

@Entity
@Table(name = "PRODUCT_TABLE")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    var productId: Long? = null

    @Column(name = "PRODUCT_NAME", nullable = false)
    lateinit var productName: String

    @Column(name = "IMAGE_URL")
    var imageUrl: String? = null

    @Column(name = "DESCRIPTION")
    var description: String? = null

    @OneToOne(mappedBy = "product", cascade = [CascadeType.ALL])
    lateinit var auction: Auction

    // 기본 생성자
    constructor()

    // 모든 필드 생성자
    constructor(
        productName: String,
        imageUrl: String?,
        description: String?,
        auction: Auction)
    {
        this.productName = productName
        this.imageUrl = imageUrl
        this.description = description
        this.auction = auction
    }
}