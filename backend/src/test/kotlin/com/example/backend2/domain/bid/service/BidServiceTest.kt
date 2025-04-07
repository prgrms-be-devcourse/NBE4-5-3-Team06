package com.example.backend2.domain.bid.service

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.dto.AuctionBidRequest
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.service.AuctionService
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.service.UserService
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import com.example.backend2.global.utils.JwtProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat

/**
 * 입찰 서비스의 단위 테스트 클래스
 * 입찰 생성, 검증, 예외 처리 등의 기능을 테스트
 */
@DisplayName("BidService 단위 테스트")
class BidServiceTest {

    private lateinit var bidService: BidService
    private lateinit var auctionService: AuctionService
    private lateinit var userService: UserService
    private lateinit var bidRepository: BidRepository
    private lateinit var redisCommon: RedisCommon
    private lateinit var jwtProvider: JwtProvider

    @BeforeEach
    fun setUp() {
        auctionService = mockk()
        userService = mockk()
        bidRepository = mockk()
        redisCommon = mockk()
        jwtProvider = mockk()
        bidService = BidService(auctionService, userService, bidRepository, redisCommon, jwtProvider)
    }

    /**
     * 입찰 생성 성공 테스트
     * 정상적인 입찰 요청에 대해 입찰이 성공적으로 생성되는지 확인
     */
    @Test
    @DisplayName("입찰 생성 성공 테스트")
    fun `createBid should create bid successfully`() {
        // given
        val product = Product(
            productId = 1L,
            productName = "테스트 상품",
            description = "테스트 설명"
        )
        
        val auction = Auction(
            auctionId = 1L,
            product = product,
            startPrice = 1000,
            minBid = 100,
            startTime = LocalDateTime.now().minusHours(1),
            endTime = LocalDateTime.now().plusHours(1),
            status = AuctionStatus.ONGOING
        )

        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        val request = AuctionBidRequest(
            auctionId = 1L,
            amount = 1500,
            token = "test-token"
        )

        every { jwtProvider.parseUserUUID(request.token) } returns "test-uuid"
        every { userService.getUserByUUID("test-uuid") } returns user
        every { auctionService.getAuctionWithValidation(1L) } returns auction
        every { redisCommon.getFromHash(any(), "amount", Int::class.java) } returns 1000
        every { redisCommon.getFromHash(any(), "userUUID", String::class.java) } returns "other-uuid"
        every { bidRepository.save(any()) } returns Bid(
            bidId = 1L,
            auction = auction,
            user = user,
            amount = 1500,
            bidTime = LocalDateTime.now()
        )

        // when
        val result = bidService.createBid(1L, request)

        // then
        assertThat(result.auctionId).isEqualTo(1L)
        assertThat(result.userUUID).isEqualTo("test-uuid")
        assertThat(result.bidAmount).isEqualTo(1500)
        assertThat(result.nickname).isEqualTo("테스트유저")

        verify { 
            jwtProvider.parseUserUUID(request.token)
            userService.getUserByUUID("test-uuid")
            auctionService.getAuctionWithValidation(1L)
            redisCommon.getFromHash("auction:1", "amount", Int::class.java)
            redisCommon.getFromHash("auction:1", "userUUID", String::class.java)
            redisCommon.putInHash("auction:1", "amount", 1500)
            redisCommon.putInHash("auction:1", "userUUID", "test-uuid")
            bidRepository.save(any())
        }
    }

    /**
     * 경매 시작 전 입찰 시도 시 예외 발생 테스트
     * 아직 시작되지 않은 경매에 대한 입찰 시도 시 적절한 예외가 발생하는지 확인
     */
    @Test
    @DisplayName("경매 시작 전 입찰 시 예외 발생 테스트")
    fun `createBid should throw exception when auction not started`() {
        // given
        val product = Product(
            productId = 1L,
            productName = "테스트 상품",
            description = "테스트 설명"
        )
        
        val auction = Auction(
            auctionId = 1L,
            product = product,
            startPrice = 1000,
            minBid = 100,
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            status = AuctionStatus.UPCOMING
        )

        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        val request = AuctionBidRequest(
            auctionId = 1L,
            amount = 1500,
            token = "test-token"
        )

        every { jwtProvider.parseUserUUID(request.token) } returns "test-uuid"
        every { userService.getUserByUUID("test-uuid") } returns user
        every { auctionService.getAuctionWithValidation(1L) } returns auction

        // when & then
        assertThrows<ServiceException> {
            bidService.createBid(1L, request)
        }

        verify { 
            jwtProvider.parseUserUUID(request.token)
            userService.getUserByUUID("test-uuid")
            auctionService.getAuctionWithValidation(1L)
        }
    }

    @Test
    @DisplayName("최소 입찰 단위 미달 시 예외 발생 테스트")
    fun `createBid should throw exception when bid amount is less than minimum increment`() {
        // given
        val product = Product(
            productId = 1L,
            productName = "테스트 상품",
            description = "테스트 설명"
        )
        
        val auction = Auction(
            auctionId = 1L,
            product = product,
            startPrice = 1000,
            minBid = 100,
            startTime = LocalDateTime.now().minusHours(1),
            endTime = LocalDateTime.now().plusHours(1),
            status = AuctionStatus.ONGOING
        )

        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        val request = AuctionBidRequest(
            auctionId = 1L,
            amount = 1050, // 현재가(1000) + 최소입찰단위(100)보다 작음
            token = "test-token"
        )

        every { jwtProvider.parseUserUUID(request.token) } returns "test-uuid"
        every { userService.getUserByUUID("test-uuid") } returns user
        every { auctionService.getAuctionWithValidation(1L) } returns auction
        every { redisCommon.getFromHash(any(), "amount", Int::class.java) } returns 1000
        every { redisCommon.getFromHash(any(), "userUUID", String::class.java) } returns "other-uuid"

        // when & then
        assertThrows<ServiceException> {
            bidService.createBid(1L, request)
        }

        verify { 
            jwtProvider.parseUserUUID(request.token)
            userService.getUserByUUID("test-uuid")
            auctionService.getAuctionWithValidation(1L)
            redisCommon.getFromHash("auction:1", "amount", Int::class.java)
            redisCommon.getFromHash("auction:1", "userUUID", String::class.java)
        }
    }
} 