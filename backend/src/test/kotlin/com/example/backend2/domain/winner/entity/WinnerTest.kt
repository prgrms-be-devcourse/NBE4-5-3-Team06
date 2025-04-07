package com.example.backend2.domain.winner.entity

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@DisplayName("Winner 엔티티 단위 테스트")
class WinnerTest {

    @Test
    @DisplayName("낙찰자 생성 테스트")
    fun `create winner should create new winner`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

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

        val winningBid = 1500
        val winTime = LocalDateTime.now()

        // when
        val winner = Winner(
            user = user,
            auction = auction,
            winningBid = winningBid,
            winTime = winTime
        )

        // then
        assertThat(winner.winnerId).isNull()
        assertThat(winner.user.userUUID).isEqualTo("test-uuid")
        assertThat(winner.auction.product.productName).isEqualTo("테스트 상품")
        assertThat(winner.winningBid).isEqualTo(1500)
        assertThat(winner.winTime).isEqualTo(winTime)
    }

    @Test
    @DisplayName("createWinner 정적 메서드 테스트")
    fun `createWinner static method should create winner`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

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

        val winningBid = 1500
        val winTime = LocalDateTime.now()

        // when
        val winner = Winner.createWinner(
            user = user,
            auction = auction,
            winningBid = winningBid,
            winTime = winTime
        )

        // then
        assertThat(winner.winnerId).isNull()
        assertThat(winner.user.userUUID).isEqualTo("test-uuid")
        assertThat(winner.auction.product.productName).isEqualTo("테스트 상품")
        assertThat(winner.winningBid).isEqualTo(1500)
        assertThat(winner.winTime).isEqualTo(winTime)
    }
} 