package com.example.backend2.unitTest.domain.bid.repository

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@Sql(statements = ["DELETE FROM BID_TABLE", "DELETE FROM WINNER_TABLE", "DELETE FROM AUCTION_TABLE", "DELETE FROM PRODUCT_TABLE", "DELETE FROM USER_TABLE"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("BidRepository 단위 테스트")
class BidRepositoryTest {
    @Autowired
    private lateinit var bidRepository: BidRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    @DisplayName("입찰 저장 테스트")
    fun `save should persist bid`() {
        // given
        // 고유한 Product 생성 및 저장
        val product =
            Product(
                productName = "테스트상품-001",
                description = "테스트용 상품 설명",
                imageUrl = "https://example.com/product.png",
            )
        productRepository.save(product)

        // User 생성 및 저장
        val user =
            User(
                userUUID = "test-user-001",
                email = "test@example.com",
                password = "encodedPassword",
                nickname = "tester",
                role = Role.USER,
                createdDate = LocalDateTime.now(),
                modifiedAt = LocalDateTime.now(),
            )
        userRepository.save(user)

        // Auction 생성 및 저장
        val auction =
            Auction(
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now().plusMinutes(5),
                status = AuctionStatus.ONGOING,
            )
        auctionRepository.save(auction)

        // Bid 생성 및 저장
        val bid =
            Bid.createBid(
                auction = auction,
                user = user,
                amount = 1200,
                bidTime = LocalDateTime.now(),
            )

        // when
        val savedBid = bidRepository.save(bid)

        // then
        assertThat(savedBid.bidId).isNotNull
        assertThat(savedBid.auction?.product?.productName).isEqualTo("테스트상품-001")
        assertThat(savedBid.user?.userUUID).isEqualTo("test-user-001")
        assertThat(savedBid.amount).isEqualTo(1200)

        // 출력 확인
        println("입찰 저장 성공")
        println("입찰 ID: ${savedBid.bidId}")
        println("상품명: ${savedBid.auction?.product?.productName}")
        println("유저 UUID: ${savedBid.user?.userUUID}")
        println("입찰 금액: ${savedBid.amount}")
    }
}