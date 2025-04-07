package com.example.backend2.global.app

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
import com.example.backend2.global.redis.RedisCommon
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import java.util.*

class AuctionListenerEventTest {

    private lateinit var winnerRepository: WinnerRepository
    private lateinit var userRepository: UserRepository
    private lateinit var simpMessagingTemplate: SimpMessagingTemplate
    private lateinit var redisCommon: RedisCommon
    private lateinit var listener: AuctionListenerEvent

    @BeforeEach
    fun setup() {
        winnerRepository = mockk(relaxed = true)
        userRepository = mockk()
        simpMessagingTemplate = mockk(relaxed = true)
        redisCommon = mockk()

        listener = AuctionListenerEvent(
            winnerRepository,
            userRepository,
            simpMessagingTemplate,
            redisCommon
        )
    }

    @Test
    @DisplayName("입찰 정보가 존재하면 낙찰자를 저장하고 WebSocket 메시지를 전송한다")
    fun `입찰 정보가 존재하면 낙찰자를 저장하고 WebSocket 메시지를 전송한다`() {
        // given
        val auctionId = 10L
        val redisKey = "auction:$auctionId"
        val auction = Auction(
            auctionId = auctionId,
            product = Product(productName = "울트라북"),
            endTime = LocalDateTime.now()
        )
        val event = AuctionFinishedEvent(source = this, auction = auction)

        val userUUID = "user-uuid"
        val amount = 50000
        val user = User(
            userUUID = userUUID,
            email = "user@test.com",
            nickname = "tester",
            password = "pw"
        )

        val winnerSlot = slot<Winner>()
        val messageSlot = slot<Map<String, Any?>>()

        every { redisCommon.getFromHash(redisKey, "amount", Int::class.java) } returns amount
        every { redisCommon.getFromHash(redisKey, "userUUID", String::class.java) } returns userUUID
        every { userRepository.findByUserUUID(userUUID) } returns Optional.of(user)
        every { winnerRepository.save(capture(winnerSlot)) } answers { winnerSlot.captured }
        every { simpMessagingTemplate.convertAndSend("/sub/auction/$auctionId", capture(messageSlot)) } just Runs

        // when
        listener.handleAuctionFinished(event)

        // then
        // Winner 저장 확인
        verify { winnerRepository.save(any()) }
        assertThat(winnerSlot.captured.winningBid).isEqualTo(amount)
        assertThat(winnerSlot.captured.user.userUUID).isEqualTo(userUUID)
        assertThat(winnerSlot.captured.auction.auctionId).isEqualTo(auctionId)

        // WebSocket 메시지 전송 확인
        verify {
            simpMessagingTemplate.convertAndSend(
                "/sub/auction/$auctionId",
                any<Map<String, Any?>>()
            )
        }

        assertThat(messageSlot.captured["auctionId"]).isEqualTo(auctionId)
        assertThat(messageSlot.captured["winnerNickname"]).isEqualTo("tester")
        assertThat(messageSlot.captured["winningBid"]).isEqualTo(amount)

        // 낙찰자 정보 출력
        println("== 낙찰자 저장 성공! ==")
        println("auctionId: ${winnerSlot.captured.auction.auctionId}")
        println("userUUID: ${winnerSlot.captured.user.userUUID}")
        println("nickname: ${winnerSlot.captured.user.nickname}")
        println("winningBid: ${winnerSlot.captured.winningBid}")
    }

    @Test
    @DisplayName("입찰 금액이 없으면 아무 처리도 하지 않는다")
    fun `입찰 금액이 없으면 아무 처리도 하지 않는다`() {
        // given
        val auction = Auction(
            auctionId = 20L,
            product = Product(productName = "갤럭시북"),
            endTime = LocalDateTime.now()
        )
        val event = AuctionFinishedEvent(source = this, auction = auction)

        every { redisCommon.getFromHash("auction:20", "amount", Int::class.java) } returns null
        every { redisCommon.getFromHash("auction:20", "userUUID", String::class.java) } returns null

        // when
        listener.handleAuctionFinished(event)

        // then
        verify(exactly = 0) { winnerRepository.save(any()) }
        verify(exactly = 0) {
            simpMessagingTemplate.convertAndSend(
                any<String>(),
                any<Map<String, Any?>>()
            )
        }
    }
}
