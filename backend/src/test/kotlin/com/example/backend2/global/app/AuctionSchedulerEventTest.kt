package com.example.backend2.global.app

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
class AuctionSchedulerEventTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var auctionRepository: AuctionRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var scheduler: AuctionSchedulerEvent

    @BeforeEach
    fun setup() {
        redisTemplate = mockk()
        auctionRepository = mockk()
        eventPublisher = mockk(relaxed = true)

        scheduler = AuctionSchedulerEvent(redisTemplate, auctionRepository, eventPublisher)
    }

    @Test
    @DisplayName("경매가 종료되었으면 상태를 FINISHED로 바꾸고 이벤트를 발행한다")
    fun `경매가 종료되었으면 상태를 FINISHED로 바꾸고 이벤트를 발행한다`() {
        // given
        val auctionId = 1L
        val redisKey = "auction:$auctionId"
        val savedAuctionSlot = slot<Auction>()

        val auction = Auction(
            auctionId = auctionId,
            product = Product(productName = "테스트 상품"),
            startPrice = 10000,
            startTime = LocalDateTime.now().minusMinutes(10),
            endTime = LocalDateTime.now().minusSeconds(30),
            status = AuctionStatus.ONGOING,
        )

        every { redisTemplate.keys("auction:*") } returns setOf(redisKey)
        every { auctionRepository.findById(auctionId) } returns Optional.of(auction)
        every { auctionRepository.save(capture(savedAuctionSlot)) } answers { savedAuctionSlot.captured }

        println(" 상태 변경 전: ${auction.status} ")

        // when
        scheduler.processAuctions()

        println(" 상태 변경 후: ${savedAuctionSlot.captured.status} ")

        // then
        assertThat(savedAuctionSlot.captured.status).isEqualTo(AuctionStatus.FINISHED)
        verify { eventPublisher.publishEvent(any<AuctionFinishedEvent>()) }
    }
}