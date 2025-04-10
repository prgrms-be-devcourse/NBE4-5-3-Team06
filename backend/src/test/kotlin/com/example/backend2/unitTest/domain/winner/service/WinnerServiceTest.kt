package com.example.backend2.unitTest.domain.winner.service

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
import com.example.backend2.domain.winner.service.WinnerService
import com.example.backend2.global.exception.ServiceException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@DisplayName("WinnerService 단위 테스트")
class WinnerServiceTest {
    private lateinit var winnerService: WinnerService
    private lateinit var winnerRepository: WinnerRepository

    @BeforeEach
    fun setUp() {
        winnerRepository = mockk()
        winnerService = WinnerService(winnerRepository)
    }

    @Test
    @DisplayName("사용자의 낙찰 내역 조회 성공 테스트")
    fun `getWinnerList should return list of winners`() {
        // given
        val userUUID = "test-uuid"
        val user = User(
            userUUID = userUUID,
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        val product = Product(
            productName = "테스트 상품",
            description = "테스트 설명",
            imageUrl = "http://example.com/image.jpg"
        )

        val auction = Auction(
            auctionId = 1L,
            product = product,
            startPrice = 1000,
            minBid = 100,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusDays(1),
            status = AuctionStatus.ONGOING
        )

        val winner = Winner(
            winnerId = 1L,
            user = user,
            auction = auction,
            winningBid = 1500,
            winTime = LocalDateTime.now()
        )

        every { winnerRepository.findByUserUserUUID(userUUID) } returns listOf(winner)

        // when
        val response = winnerService.getWinnerList(userUUID)

        // then
        assertThat(response).isNotNull
        assertThat(response.code).isEqualTo("200")
        assertThat(response.msg).isEqualTo("낙찰 내역 조회가 완료되었습니다.")

        val winners = response.data
        assertThat(winners).isNotNull
        assertThat(winners!!).hasSize(1)
        assertThat(winners[0].auctionId).isEqualTo(1L)
        assertThat(winners[0].productName).isEqualTo("테스트 상품")
        assertThat(winners[0].description).isEqualTo("테스트 설명")
        assertThat(winners[0].winningBid).isEqualTo(1500)
        assertThat(winners[0].imageUrl).isEqualTo("http://example.com/image.jpg")
    }

    @Test
    @DisplayName("낙찰 내역이 없을 경우 예외 발생 테스트")
    fun `getWinnerList should throw exception when no winners exist`() {
        // given
        val userUUID = "test-uuid"
        every { winnerRepository.findByUserUserUUID(userUUID) } returns emptyList()

        // when & then
        val exception = assertThrows<ServiceException> {
            winnerService.getWinnerList(userUUID)
        }

        assertThat(exception.message).isEqualTo("낙찰자가 존재하지 않습니다.")
    }
} 