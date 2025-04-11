package com.example.backend2.unitTest.domain.auction.entity

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.winner.entity.Winner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

/**
 * 경매 엔티티의 단위 테스트 클래스
 * 경매 생성 및 낙찰자 설정 기능을 테스트
 */
@ActiveProfiles("test")
@DisplayName("Auction 엔티티 단위 테스트")
class AuctionTest {
    /**
     * 경매 생성 테스트
     * 새로운 경매가 올바른 속성값으로 생성되는지 확인
     */
    @Test
    @DisplayName("경매 생성 테스트")
    fun `create auction should create new auction`() {
        // given
        val product =
            Product(
                productId = 1L,
                productName = "테스트 상품",
                description = "테스트 설명",
            )

        val auction =
            Auction(
                auctionId = 1L,
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusDays(1),
                status = AuctionStatus.UPCOMING,
            )

        // when
        val updatedAuction = auction.setStatus(AuctionStatus.ONGOING)

        // then
        assertThat(updatedAuction.status).isEqualTo(AuctionStatus.ONGOING)
        assertThat(updatedAuction.auctionId).isEqualTo(auction.auctionId)
        assertThat(updatedAuction.product).isEqualTo(auction.product)
    }

    /**
     * 낙찰자 설정 테스트
     * 경매에 낙찰자를 설정했을 때 관련 정보가 올바르게 업데이트되는지 확인
     */
    @Test
    @DisplayName("낙찰자 설정 테스트")
    fun `setWinner should set auction winner`() {
        // given
        val product =
            Product(
                productId = 1L,
                productName = "테스트 상품",
                description = "테스트 설명",
            )

        val auction =
            Auction(
                auctionId = 1L,
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusDays(1),
                status = AuctionStatus.ONGOING,
            )

        val winner =
            Winner(
                winnerId = 1L,
                auction = auction,
                winningBid = 1500,
            )

        // when
        val updatedAuction = auction.setWinner(winner)

        // then
        assertThat(updatedAuction.winner).isEqualTo(winner)
        assertThat(updatedAuction.auctionId).isEqualTo(auction.auctionId)
        assertThat(updatedAuction.product).isEqualTo(auction.product)
    }
}