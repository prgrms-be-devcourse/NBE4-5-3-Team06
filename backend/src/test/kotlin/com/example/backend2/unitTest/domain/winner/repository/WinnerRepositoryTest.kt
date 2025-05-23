package com.example.backend2.unitTest.domain.winner.repository

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
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
@DisplayName("WinnerRepository 단위 테스트")
class WinnerRepositoryTest {

    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var winnerRepository: WinnerRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    @DisplayName("낙찰자 저장 테스트")
    fun `save should persist winner`() {
        // given
        val user = userRepository.save(
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER
            )
        )

        val product = productRepository.save(
            Product(
                productName = "테스트 상품",
                description = "테스트 설명"
            )
        )

        val auction = auctionRepository.save(
            Auction(
                product = product,
                startPrice = 1000,
                minBid = 100,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusDays(1),
                status = AuctionStatus.ONGOING
            )
        )

        val winner = Winner(
            user = user,
            auction = auction,
            winningBid = 1500,
            winTime = LocalDateTime.now()
        )

        // when
        val savedWinner = winnerRepository.save(winner)

        // then
        assertThat(savedWinner.winnerId).isNotNull
        assertThat(savedWinner.user.userUUID).isEqualTo("test-uuid")
        assertThat(savedWinner.auction.product.productName).isEqualTo("테스트 상품")
        assertThat(savedWinner.winningBid).isEqualTo(1500)
    }

    @Test
    @DisplayName("사용자 UUID로 낙찰자 조회 테스트")
    fun `findByUserUserUUID should return winners when exists`() {
        // given
        val user = userRepository.save(
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "tester",
                password = "1234",
                role = Role.USER,
            )
        )

        val product = productRepository.save(
            Product(productName = "테스트 상품")
        )

        val auction = auctionRepository.save(
            Auction(
                product = product,
                startPrice = 10000,
                minBid = 1000,
                startTime = LocalDateTime.now().minusHours(2),
                endTime = LocalDateTime.now().minusMinutes(1),
                status = AuctionStatus.FINISHED
            )
        )

        val winner = Winner(
            user = user,
            auction = auction,
            winningBid = 15000,
            winTime = LocalDateTime.now()
        )

        winnerRepository.save(winner)
        // when
        val winners = winnerRepository.findByUserUserUUID("test-uuid")

        // then
        assertThat(winners).hasSize(1)
        assertThat(winners[0].user.userUUID).isEqualTo("test-uuid")
        assertThat(winners[0].auction.product.productName).isEqualTo("테스트 상품")
        assertThat(winners[0].winningBid).isEqualTo(15000)
    }

    @Test
    @DisplayName("존재하지 않는 사용자 UUID로 낙찰자 조회 시 빈 리스트 반환 테스트")
    fun `findByUserUserUUID should return empty list when user not exists`() {
        // when
        val winners = winnerRepository.findByUserUserUUID("non-existent-uuid")

        // then
        assertThat(winners).isEmpty()
    }
} 