package com.example.backend2.unitTest.domain.bid.entity

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("Bid 엔티티 단위 테스트")
class BidTest {
    @Test
    @DisplayName("입찰 금액 업데이트 테스트")
    fun `updateAmount should update bid amount and time`() {
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

        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )

        val bid =
            Bid(
                bidId = 1L,
                auction = auction,
                user = user,
                amount = 1000,
                bidTime = LocalDateTime.now(),
            )

        // when
        val updatedBid = bid.updateAmount(1500)

        // then
        assertThat(updatedBid.amount).isEqualTo(1500)
        assertThat(updatedBid.bidTime).isAfter(bid.bidTime)
        assertThat(updatedBid.auction).isEqualTo(bid.auction)
        assertThat(updatedBid.user).isEqualTo(bid.user)
    }

    @Test
    @DisplayName("입찰 생성 테스트")
    fun `createBid should create new bid`() {
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

        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )

        val amount = 1500
        val bidTime = LocalDateTime.now()

        // when
        val bid = Bid.createBid(auction, user, amount, bidTime)

        // then
        assertThat(bid.auction).isEqualTo(auction)
        assertThat(bid.user).isEqualTo(user)
        assertThat(bid.amount).isEqualTo(amount)
        assertThat(bid.bidTime).isEqualTo(bidTime)
    }
} 
