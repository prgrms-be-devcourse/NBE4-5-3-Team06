package com.example.backend2.domain.product.dto

import com.example.backend2.domain.product.entity.Product
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DisplayName("ProductResponse DTO 단위 테스트")
class ProductResponseTest {

    @Test
    @DisplayName("Product 엔티티를 ProductResponse로 변환 테스트")
    fun `from should convert Product to ProductResponse`() {
        // given
        val product = Product(
            productId = 1L,
            productName = "테스트 상품",
            imageUrl = "http://example.com/image.jpg",
            description = "테스트 설명"
        )

        // when
        val response = ProductResponse.from(product)

        // then
        assertThat(response.productId).isEqualTo(1L)
        assertThat(response.productName).isEqualTo("테스트 상품")
        assertThat(response.imageUrl).isEqualTo("http://example.com/image.jpg")
        assertThat(response.description).isEqualTo("테스트 설명")
    }

    @Test
    @DisplayName("null 값이 있는 Product 엔티티를 ProductResponse로 변환 테스트")
    fun `from should handle null values in Product`() {
        // given
        val product = Product(
            productId = 1L,
            productName = "테스트 상품",
            imageUrl = null,
            description = null
        )

        // when
        val response = ProductResponse.from(product)

        // then
        assertThat(response.productId).isEqualTo(1L)
        assertThat(response.productName).isEqualTo("테스트 상품")
        assertThat(response.imageUrl).isEqualTo("")
        assertThat(response.description).isEqualTo("")
    }
} 