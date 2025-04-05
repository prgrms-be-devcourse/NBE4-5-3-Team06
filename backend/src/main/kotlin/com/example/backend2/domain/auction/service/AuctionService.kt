@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.auction.service

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.dto.*
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.global.annotation.HasRole
import com.example.backend2.global.dto.RsData
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuctionService(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val productRepository: ProductRepository,
    private val redisCommon: RedisCommon,
) {
    private val log = KotlinLogging.logger {}

    // 사용자-모든 경매 목록을 조회하고 AuctionResponse DTO 리스트로 변환
    fun getAllAuctions(): List<AuctionCheckResponse> {
        // 경매 목록 조회 <AuctionRepository 에서 조회>
        val auctions =
            auctionRepository
                .findAllAuctions()
                .takeIf { it.isNotEmpty() } // if문 체크 대신 사용
                ?: throw ServiceException("404", "경매 목록 조회 실패") // null 이면 실행

        // Auction 엔티티를 AuctionCheckResponse DTO로 변환
        return auctions.map { auction ->
            val hashKey = "auction:${auction.auctionId}"
            val amount = redisCommon.getFromHash(hashKey, "amount", Int::class.java) ?: 0
            log.info { "amount: $amount" }
            AuctionCheckResponse.from(auction, amount)
        }
    }

    // 관리자- 모든 경매 목록을 조회 (관리자)
    @HasRole(Role.ADMIN)
    fun getAdminAllAuctions(): List<AuctionAdminResponse> {
        val auctions =
            auctionRepository
                .findAllAuctionsWithProductAndWinner()
                .takeIf { it.isNotEmpty() } // if문 체크 대신 사용
                ?: throw ServiceException("404", "경매 목록 조회 실패") // null 이면 실행

        return auctions.map { auction ->
            val hashKey = "auction:${auction.auctionId}"
            val amount = redisCommon.getFromHash(hashKey, "amount", Int::class.java) ?: 0
            log.info { "amount: $amount" }
            AuctionAdminResponse.from(auction, amount)
        }
    }

    // 경매 등록 서비스 (관리자)
    @HasRole(Role.ADMIN)
    @Transactional
    fun createAuction(requestDto: AuctionRequest): RsData<AuctionCreateResponse> {
        // 경매 종료 시간이 시작 시간보다 빠르면 예외 처리
        requestDto.startTime?.let { startTime ->
            requestDto.endTime?.let { endTime ->
                require(!startTime.isAfter(endTime)) { "경매 종료 시간이 시작 시간보다 빠를 수 없습니다." }
            }
        }

        // 최소 등록 시간 검증 (현재 시간 기준 최소 2일 전)
        requestDto.startTime?.let { startTime ->
            val now = LocalDateTime.now()
            require(!startTime.isBefore(now.plusDays(2))) { "상품 등록 시간은 최소 2일 전부터 가능합니다." }
        }

        // 상품 정보 저장
        val product =
            Product(
                productName = requestDto.productName,
                imageUrl = requestDto.imageUrl,
                description = requestDto.description,
            ).also { productRepository.save(it) }

        // 경매 정보 저장
        val auction =
            Auction(
                product = product,
                startPrice = requestDto.startPrice ?: 0,
                minBid = requestDto.minBid ?: 0,
                startTime = requestDto.startTime ?: LocalDateTime.now(),
                endTime = requestDto.endTime ?: LocalDateTime.now().plusDays(1),
                status = AuctionStatus.UPCOMING,
            ).also { auctionRepository.save(it) }

        // Redis 설정
        setupRedisForAuction(auction)

        // 성공 응답 반환
        return RsData("201", "경매가 등록되었습니다.", AuctionCreateResponse.from(auction))
    }

    // Redis 설정 메서드
    private fun setupRedisForAuction(auction: Auction) {
        val hashKey = "auction:${auction.auctionId}"

        // 입찰 시작가 설정
        redisCommon.putInHash(hashKey, "amount", auction.startPrice)

        // TTL 설정 (종료 시간 + 2분)
        val expireTime = auction.endTime.plusMinutes(2)
        redisCommon.setExpireAt(hashKey, expireTime)
    }

    // 외부 요청에 대한 거래 종료 기능
    @Transactional
    fun closeAuction(auctionId: Long) {
        val auction =
            auctionRepository.findByAuctionId(auctionId)
                ?: throw IllegalArgumentException("진행 중인 경매를 찾을 수 없습니다.")

        auction.setStatus(AuctionStatus.FINISHED)
    }

    // 경매 데이터 검증 후 DTO 반환
    @Transactional
    fun getAuctionDetail(auctionId: Long): AuctionDetailResponse {
        val auction = getAuctionWithValidation(auctionId)

        // Redis 에서 최고가 가져오기
        val hashKey = "auction:${auction.auctionId}"
        val amount = redisCommon.getFromHash(hashKey, "amount", Int::class.java) ?: 0
        log.info { "DetailCurrentAmount: $amount" }

        return AuctionDetailResponse.from(auction, amount)
    }

    // 경매 조회 및 상태 검증 메서드
    fun getAuctionWithValidation(auctionId: Long): Auction =
        auctionRepository.findByAuctionId(auctionId)
            ?: throw ServiceException("400-1", "경매가 존재하지 않습니다.")
}
