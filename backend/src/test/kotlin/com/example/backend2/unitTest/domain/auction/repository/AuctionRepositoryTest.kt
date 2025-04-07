package com.example.backend2.unitTest.domain.auction.repository

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AuctionRepository 단위 테스트")
class AuctionRepositoryTest {
    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Test
    @DisplayName("경매 목록 조회 테스트")
    fun `findAllAuctions should return all auctions with products`() {
        // given
        val product =
            Product(
                productName = "테스트 상품",
                description = "테스트 설명",
            )

        val auction =
            Auction(
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusDays(1),
                status = AuctionStatus.ONGOING,
            )

        auctionRepository.save(auction)

        // when
        val result = auctionRepository.findAllAuctions()

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].product.productName).isEqualTo("테스트 상품")
        assertThat(result[0].startPrice).isEqualTo(1000)
        assertThat(result[0].status).isEqualTo(AuctionStatus.ONGOING)
    }

    @Test
    @DisplayName("경매 ID로 조회 테스트")
    fun `findByAuctionId should return auction when exists`() {
        // given
        val product =
            Product(
                productName = "테스트 상품",
                description = "테스트 설명",
            )

        val auction =
            Auction(
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusDays(1),
                status = AuctionStatus.ONGOING,
            )

        val savedAuction = auctionRepository.save(auction)

        // when
        val result = auctionRepository.findByAuctionId(savedAuction.auctionId!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.product.productName).isEqualTo("테스트 상품")
        assertThat(result.startPrice).isEqualTo(1000)
        assertThat(result.status).isEqualTo(AuctionStatus.ONGOING)
    }

    @Test
    @DisplayName("존재하지 않는 경매 ID로 조회 시 null 반환 테스트")
    fun `findByAuctionId should return null when auction not exists`() {
        // when
        val result = auctionRepository.findByAuctionId(999L)

        // then
        assertThat(result).isNull()
    }
} 
