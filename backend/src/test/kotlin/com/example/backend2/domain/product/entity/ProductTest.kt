package com.example.backend2.domain.product.entity

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

/**
 * 상품 엔티티의 단위 테스트 클래스
 * 상품 생성 및 경매 연결 기능을 테스트
 */
@DisplayName("Product 엔티티 단위 테스트")
class ProductTest {

    /**
     * 상품 생성 테스트
     * 새로운 상품이 올바른 속성값으로 생성되는지 확인
     */
    @Test
    @DisplayName("상품 생성 테스트")
    fun `create product should create new product`() {
        // given
        val productName = "테스트 상품"
        val imageUrl = "http://example.com/image.jpg"
        val description = "테스트 상품 설명"

        // when
        val product = Product(
            productName = productName,
            imageUrl = imageUrl,
            description = description
        )

        // then
        assertThat(product.productId).isNull()
        assertThat(product.productName).isEqualTo(productName)
        assertThat(product.imageUrl).isEqualTo(imageUrl)
        assertThat(product.description).isEqualTo(description)
        assertThat(product.auction).isNull()
    }

    /**
     * 상품과 경매 연결 테스트
     * 상품에 경매를 연결했을 때 양방향 관계가 올바르게 설정되는지 확인
     */
    @Test
    @DisplayName("상품에 경매 연결 테스트")
    fun `product should be linked with auction`() {
        // given
        val product = Product(
            productName = "테스트 상품",
            description = "테스트 설명"
        )

        val auction = Auction(
            product = product,
            startPrice = 1000,
            minBid = 100,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusDays(1),
            status = AuctionStatus.ONGOING
        )

        // when
        val updatedProduct = product.copy(auction = auction)

        // then
        assertThat(updatedProduct.auction).isNotNull
        assertThat(updatedProduct.auction?.startPrice).isEqualTo(1000)
        assertThat(updatedProduct.auction?.status).isEqualTo(AuctionStatus.ONGOING)
    }
} 