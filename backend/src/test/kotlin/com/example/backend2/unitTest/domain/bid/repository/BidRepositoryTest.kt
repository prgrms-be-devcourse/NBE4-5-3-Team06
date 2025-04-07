package com.example.backend2.unitTest.domain.bid.repository

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BidRepository 단위 테스트")
class BidRepositoryTest {
    @Autowired
    private lateinit var bidRepository: BidRepository

    @Test
    @DisplayName("입찰 저장 테스트")
    fun `save should persist bid`() {
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
                auction = auction,
                user = user,
                amount = 1500,
                bidTime = LocalDateTime.now(),
            )

        // when
        val savedBid = bidRepository.save(bid)

        // then
        assertThat(savedBid.bidId).isNotNull
        assertThat(savedBid.auction?.product?.productName).isEqualTo("테스트 상품")
        assertThat(savedBid.user?.userUUID).isEqualTo("test-uuid")
        assertThat(savedBid.amount).isEqualTo(1500)
    }
}
