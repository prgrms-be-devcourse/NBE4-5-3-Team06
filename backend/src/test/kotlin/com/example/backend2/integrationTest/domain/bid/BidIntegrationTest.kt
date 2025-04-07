package com.example.backend2.integrationTest.domain.bid

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.dto.AuctionBidRequest
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.bid.service.BidService
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.redis.RedisCommon
import com.example.backend2.global.utils.JwtProvider
import com.example.backend2.global.websocket.dto.WebSocketResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("입찰 통합 테스트")
class BidIntegrationTest {
    @Autowired
    private lateinit var bidRepository: BidRepository

    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bidService: BidService

    @Autowired
    private lateinit var redisCommon: RedisCommon

    @Autowired
    private lateinit var jwtProvider: JwtProvider

    @Autowired
    private lateinit var simpMessagingTemplate: SimpMessagingTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var seller: User
    private lateinit var bidder: User
    private lateinit var product: Product
    private lateinit var auction: Auction
    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        bidRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()

        // Redis 데이터 초기화
        val keys = redisCommon.getAllKeys()
        keys?.forEach { key ->
            if (key.startsWith("auction:")) {
                redisCommon.removeFromHash(key, "amount")
                redisCommon.removeFromHash(key, "userUUID")
            }
        }

        // 테스트용 판매자 생성
        seller =
            userRepository.save(
                User(
                    userUUID = "seller-uuid",
                    email = "seller@example.com",
                    nickname = "판매자",
                    password = "password",
                    role = Role.USER,
                ),
            )

        // 테스트용 입찰자 생성
        bidder =
            userRepository.save(
                User(
                    userUUID = "bidder-uuid",
                    email = "bidder@example.com",
                    nickname = "입찰자",
                    password = "password",
                    role = Role.USER,
                ),
            )

        // 테스트용 상품 생성
        product =
            productRepository.save(
                Product(
                    productName = "테스트 상품",
                    description = "테스트 설명",
                ),
            )

        // 테스트용 경매 생성
        auction =
            auctionRepository.save(
                Auction(
                    product = product,
                    startPrice = 10000,
                    minBid = 1000,
                    startTime = LocalDateTime.now().minusHours(1),
                    endTime = LocalDateTime.now().plusHours(1),
                    status = AuctionStatus.ONGOING,
                ),
            )

        // 테스트용 토큰 생성 (실제 로직에 맞게 구현)
        val claims =
            mapOf(
                "userUUID" to bidder.userUUID,
                "nickname" to bidder.nickname,
                "role" to bidder.role.name,
            )
        token = jwtProvider.generateToken(claims, bidder.email)

        // Redis에 경매 정보 초기 설정
        val hashKey = "auction:${auction.auctionId}"
        redisCommon.putInHash(hashKey, "amount", 10000)
    }

    @Test
    @DisplayName("유효한 입찰 생성 통합 테스트")
    fun createValidBid() {
        // given
        val bidRequest =
            AuctionBidRequest(
                auctionId = auction.auctionId!!,
                amount = 12000,
                token = token,
            )

        // when - WebSocket 요청 대신 서비스 직접 호출
        val beforeCount = bidRepository.count()

        // BidController의 createBids 메서드와 유사한 호출 패턴
        val userUUID = jwtProvider.parseUserUUID(bidRequest.token)
        val nickname = jwtProvider.parseNickname(bidRequest.token)

        val response = bidService.createBid(bidRequest.auctionId, bidRequest)

        val webSocketResponse =
            WebSocketResponse(
                message = "입찰 성공",
                localDateTime = LocalDateTime.now(),
                nickname = response.nickname,
                currentBid = bidRequest.amount,
            )

        simpMessagingTemplate.convertAndSend("/topic/auction/${bidRequest.auctionId}", webSocketResponse)

        // then
        // 1. DB에 입찰이 저장되었는지 확인
        val afterCount = bidRepository.count()
        assertThat(afterCount).isEqualTo(beforeCount + 1)

        // 2. Redis에 입찰 정보가 업데이트되었는지 확인
        val hashKey = "auction:${auction.auctionId}"
        val updatedAmount = redisCommon.getFromHash(hashKey, "amount", Int::class.java)
        val updatedUserUUID = redisCommon.getFromHash(hashKey, "userUUID", String::class.java)

        assertThat(updatedAmount).isEqualTo(12000)
        assertThat(updatedUserUUID).isEqualTo(bidder.userUUID)

        // 3. 저장된 입찰 정보 확인
        val savedBid = bidRepository.findAll().first()
        assertThat(savedBid.amount).isEqualTo(12000)
        assertThat(savedBid.user.userUUID).isEqualTo(bidder.userUUID)
        assertThat(savedBid.auction.auctionId).isEqualTo(auction.auctionId)
    }

    @Test
    @DisplayName("최소 입찰 금액 미만 입찰 시 예외 발생 테스트")
    fun bidBelowMinimumAmount() {
        // given
        val bidRequest =
            AuctionBidRequest(
                auctionId = auction.auctionId!!,
                amount = 10500, // 현재가(10000) + 최소입찰단위(1000) 미만 금액
                token = token,
            )

        val beforeCount = bidRepository.count()

        // when & then - 예외가 발생하는지 확인
        try {
            bidService.createBid(bidRequest.auctionId, bidRequest)
            // 예외가 발생하지 않으면 실패
            assert(false) { "최소 입찰 금액 미만 입찰 시 예외가 발생해야 함" }
        } catch (e: Exception) {
            // 예외 발생 확인
            assertThat(e.message).contains("입찰 금액이 최소 입찰 단위보다 작습니다")
        }

        // DB에 입찰이 저장되지 않았는지 확인
        val afterCount = bidRepository.count()
        assertThat(afterCount).isEqualTo(beforeCount)

        // Redis의 정보가 변경되지 않았는지 확인
        val hashKey = "auction:${auction.auctionId}"
        val currentAmount = redisCommon.getFromHash(hashKey, "amount", Int::class.java)
        assertThat(currentAmount).isEqualTo(10000) // 초기값 유지
    }

    @Test
    @DisplayName("동시에 여러 입찰이 들어올 경우 처리 테스트")
    fun multipleBidsSimultaneously() {
        // given
        // 여러 사용자 생성
        val bidders =
            (1..5).map { i ->
                userRepository.save(
                    User(
                        userUUID = "bidder-uuid-$i",
                        email = "bidder$i@example.com",
                        nickname = "입찰자$i",
                        password = "password",
                        role = Role.USER,
                    ),
                )
            }

        val bidderTokens =
            bidders.map { user ->
                val claims =
                    mapOf(
                        "userUUID" to user.userUUID,
                        "nickname" to user.nickname,
                        "role" to user.role.name,
                    )
                jwtProvider.generateToken(claims, user.email)
            }

        // when
        // 동시에 여러 입찰 요청 시뮬레이션
        val futures =
            bidders.mapIndexed { index, _ ->
                CompletableFuture.runAsync {
                    val bidRequest =
                        AuctionBidRequest(
                            auctionId = auction.auctionId!!,
                            amount = 11000 + (index * 1000), // 11000, 12000, 13000, 14000, 15000
                            token = bidderTokens[index],
                        )

                    try {
                        bidService.createBid(bidRequest.auctionId, bidRequest)
                    } catch (e: Exception) {
                        // 예외 로깅
                        println("입찰 예외 for bidder ${index + 1}: ${e.message}")
                    }
                }
            }

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(*futures.toTypedArray()).join()

        // then
        // Redis에 최종 입찰 정보 확인
        val hashKey = "auction:${auction.auctionId}"
        val finalAmount = redisCommon.getFromHash(hashKey, "amount", Int::class.java)
        val finalUserUUID = redisCommon.getFromHash(hashKey, "userUUID", String::class.java)

        // 최고가 확인 (15000)
        assertThat(finalAmount).isGreaterThanOrEqualTo(15000)

        // DB에 저장된 모든 입찰 기록 확인
        val savedBids = bidRepository.findAll()
        assertThat(savedBids).isNotEmpty()

        // 가장 높은 입찰 확인
        val highestBid = savedBids.maxByOrNull { it.amount }
        assertThat(highestBid).isNotNull
        assertThat(highestBid?.amount).isGreaterThanOrEqualTo(15000)
    }

    @Test
    @DisplayName("경매 종료 후 입찰 시도 시 예외 발생 테스트")
    fun bidAfterAuctionEnd() {
        // given
        // 경매 종료 상태로 변경
        val endedAuction = auction.setStatus(AuctionStatus.FINISHED)
        auctionRepository.save(endedAuction.copy(endTime = LocalDateTime.now().minusHours(1)))

        val bidRequest =
            AuctionBidRequest(
                auctionId = auction.auctionId!!,
                amount = 12000,
                token = token,
            )

        val beforeCount = bidRepository.count()

        // when & then
        try {
            bidService.createBid(bidRequest.auctionId, bidRequest)
            // 예외가 발생하지 않으면 실패
            assert(false) { "종료된 경매에 입찰 시 예외가 발생해야 함" }
        } catch (e: Exception) {
            // 예외 발생 확인
            assertThat(e.message).contains("경매가 종료 되었습니다")
        }

        // DB에 입찰이 저장되지 않았는지 확인
        val afterCount = bidRepository.count()
        assertThat(afterCount).isEqualTo(beforeCount)
    }
}
