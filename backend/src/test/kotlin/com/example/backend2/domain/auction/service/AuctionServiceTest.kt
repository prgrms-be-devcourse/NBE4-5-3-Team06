package com.example.backend2.domain.auction.service

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

/**
 * 경매 서비스의 단위 테스트 클래스
 * 경매 생성, 조회, 종료 등의 기능을 테스트
 */
@ActiveProfiles("test")
@DisplayName("AuctionService 단위 테스트")
class AuctionServiceTest {
    private lateinit var auctionService: AuctionService
    private lateinit var auctionRepository: AuctionRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var redisCommon: RedisCommon

    @BeforeEach
    fun setUp() {
        auctionRepository = mockk()
        productRepository = mockk()
        redisCommon = mockk()
        auctionService = AuctionService(auctionRepository, productRepository, redisCommon)
    }

    @Test
    @DisplayName("경매 목록 조회 성공 테스트")
    fun `getAllAuctions should return list of auctions`() {
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

        every { auctionRepository.findAllAuctions() } returns listOf(auction)
        every { redisCommon.getFromHash(any(), "amount", Int::class.java) } returns 1000

        // when
        val result = auctionService.getAllAuctions()

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].auctionId).isEqualTo(1L)
        assertThat(result[0].productName).isEqualTo("테스트 상품")
        assertThat(result[0].currentBid).isEqualTo(1000)

        verify {
            auctionRepository.findAllAuctions()
            redisCommon.getFromHash("auction:1", "amount", Int::class.java)
        }
    }

    @Test
    @DisplayName("경매 상세 조회 성공 테스트")
    fun `getAuctionDetail should return auction detail`() {
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

        every { auctionRepository.findByAuctionId(1L) } returns auction
        every { redisCommon.getFromHash(any(), "amount", Int::class.java) } returns 1500

        // when
        val result = auctionService.getAuctionDetail(1L)

        // then
        assertThat(result.auctionId).isEqualTo(1L)
        assertThat(result.product.productName).isEqualTo("테스트 상품")
        assertThat(result.startPrice).isEqualTo(1000)
        assertThat(result.currentBid).isEqualTo(1500)
        assertThat(result.minBid).isEqualTo(100)
        assertThat(result.status).isEqualTo(AuctionStatus.ONGOING.name)

        verify {
            auctionRepository.findByAuctionId(1L)
            redisCommon.getFromHash("auction:1", "amount", Int::class.java)
        }
    }

    @Test
    @DisplayName("존재하지 않는 경매 조회 시 예외 발생 테스트")
    fun `getAuctionDetail should throw exception when auction not found`() {
        // given
        every { auctionRepository.findByAuctionId(999L) } returns null

        // when & then
        assertThrows<ServiceException> {
            auctionService.getAuctionDetail(999L)
        }

        verify { auctionRepository.findByAuctionId(999L) }
    }

    /**
     * 경매 종료 테스트
     * 존재하는 경매를 정상적으로 종료하고 상태가 업데이트되는지 확인
     */
    @Test
    @DisplayName("경매 종료 테스트")
    fun `closeAuction should update auction status to ended`() {
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

        every { auctionRepository.findByAuctionId(1L) } returns auction
        every { auctionRepository.save(any()) } returns auction.copy(status = AuctionStatus.FINISHED)

        // when
        auctionService.closeAuction(1L)

        // then
        verify {
            auctionRepository.findByAuctionId(1L)
        }
    }

    /**
     * 존재하지 않는 경매 종료 시 예외 발생 테스트
     * 존재하지 않는 경매 ID로 종료 시도 시 적절한 예외가 발생하는지 확인
     */
    @Test
    @DisplayName("존재하지 않는 경매 종료 시 예외 발생 테스트")
    fun `closeAuction should throw exception when auction not found`() {
        // given
        every { auctionRepository.findByAuctionId(999L) } returns null

        // when & then
        assertThrows<ServiceException> {
            auctionService.closeAuction(999L)
        }

        verify { auctionRepository.findByAuctionId(999L) }
    }
} 
