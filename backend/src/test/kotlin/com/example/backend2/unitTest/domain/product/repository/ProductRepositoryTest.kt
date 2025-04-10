package com.example.backend2.unitTest.domain.product.repository

import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

/**
 * 상품 레포지토리의 단위 테스트 클래스
 * 상품 저장 및 조회 기능을 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository 단위 테스트")
class ProductRepositoryTest {
    @Autowired
    private lateinit var productRepository: ProductRepository

    /**
     * 상품 저장 테스트
     * 새로운 상품이 데이터베이스에 정상적으로 저장되는지 확인
     */
    @Test
    @DisplayName("상품 저장 테스트")
    fun `save should persist product`() {
        // given
        val product =
            Product(
                productName = "테스트 상품",
                imageUrl = "http://example.com/image.jpg",
                description = "테스트 설명",
            )

        // when
        val savedProduct = productRepository.save(product)

        // then
        assertThat(savedProduct.productId).isNotNull
        assertThat(savedProduct.productName).isEqualTo("테스트 상품")
        assertThat(savedProduct.imageUrl).isEqualTo("http://example.com/image.jpg")
        assertThat(savedProduct.description).isEqualTo("테스트 설명")
    }

    /**
     * 상품 ID로 조회 테스트
     * 존재하는 상품 ID로 조회 시 해당 상품이 반환되는지 확인
     */
    @Test
    @DisplayName("상품 ID로 조회 테스트")
    fun `findById should return product when exists`() {
        // given
        val product =
            Product(
                productName = "테스트 상품",
                description = "테스트 설명",
            )
        val savedProduct = productRepository.save(product)

        // when
        val foundProduct = productRepository.findById(savedProduct.productId!!).orElse(null)

        // then
        assertThat(foundProduct).isNotNull
        assertThat(foundProduct!!.productName).isEqualTo("테스트 상품")
        assertThat(foundProduct.description).isEqualTo("테스트 설명")
    }

    /**
     * 존재하지 않는 상품 ID로 조회 시 null 반환 테스트
     * 존재하지 않는 상품 ID로 조회 시 null이 반환되는지 확인
     */
    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 시 null 반환 테스트")
    fun `findById should return null when product not exists`() {
        // when
        val foundProduct = productRepository.findById(999L).orElse(null)

        // then
        assertThat(foundProduct).isNull()
    }
}