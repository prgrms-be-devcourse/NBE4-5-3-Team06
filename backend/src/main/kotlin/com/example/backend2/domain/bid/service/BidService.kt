package com.example.backend2.domain.bid.service

import com.example.backend2.domain.auction.dto.AuctionBidRequest
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.service.AuctionService
import com.example.backend2.domain.bid.dto.BidCreateResponse
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.service.UserService
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import com.example.backend2.global.utils.JwtProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BidService(
    private val auctionService: AuctionService,
    private val userService: UserService,
    private val bidRepository: BidRepository,
    private val redisCommon: RedisCommon,
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    fun createBid(
        auctionId: Long,
        request: AuctionBidRequest,
    ): BidCreateResponse {
        val hashKey = "auction:$auctionId"
        val now = java.time.LocalDateTime.now()

        // 유저 및 경매 정보 가져오기
        val userUUID: String? = jwtProvider.parseUserUUID(request.token) // jwt토큰 파싱해 유저UUID 가져오기
        val user: User = userService.getUserByUUID(userUUID!!)
        val auction: Auction = auctionService.getAuctionWithValidation(auctionId)

        // 경매 시간 검증
        if (now.isBefore(auction.startTime)) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "경매가 시작 전입니다.")
        }
        if (now.isAfter(auction.endTime)) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "경매가 종료 되었습니다.")
        }

        // Redis에서 현재 최고가 조회
        val amount: Int? = redisCommon.getFromHash(hashKey, "amount", Int::class.java)
        val highestUserUUID: String? = redisCommon.getFromHash(hashKey, "userUUID", String::class.java) // 현재 최고 입찰자
        val currentBidAmount = amount ?: auction.startPrice // DB 테스트를 위한 redis에 없으면 시작가로 설정

        // 최고 입찰자 검증
        if (userUUID == highestUserUUID) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "이미 최고 입찰자입니다. 다른 사용자의 입찰을 기다려주세요.")
        }

        // Redis 에 입찰 정보를 원자적으로 갱신
        val bidSuccess =
            /**
             A 사용자의 트랜잭션 / B 사용자의 트랜잭션이 병렬처리되는 것이 아니라,
             큐에 순서대로 적재되어 차례로 처리된다.
             */
            redisCommon.executeInTransaction {
                val currentAmount = redisCommon.getFromHash(hashKey, "amount", Int::class.java) ?: auction.startPrice
                val currentUserUUID = redisCommon.getFromHash(hashKey, "userUUID", String::class.java)

                // 입찰 금액 검증
                if (request.amount <= currentAmount) {
                    throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "입찰 금액이 현재 최고가보다 낮습니다.")
                }
                // 최소 입찰 단위 검증
                if (request.amount < (currentAmount + auction.minBid)) {
                    throw ServiceException(
                        HttpStatus.BAD_REQUEST.value().toString(),
                        "입찰 금액이 최소 입찰 단위보다 작습니다. 최소 ${currentAmount + auction.minBid}원 이상 입찰해야 합니다.",
                    )
                }

                // 동시성 제어를 위한 조건 검사
                if (request.amount > currentAmount && userUUID != currentUserUUID) {
                    // Redis에 입찰 정보를 원자적으로 업데이트
                    redisCommon.putInHash(hashKey, "amount", request.amount)
                    redisCommon.putInHash(hashKey, "userUUID", userUUID)
                    true
                } else {
                    false
                }
            }

        if (!bidSuccess) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "입찰에 실패했습니다. 다른 사용자의 입찰을 기다려주세요.")
        }

        // 입찰 정보 저장
        val bid = Bid.createBid(auction, user, request.amount, now)
        val savedBid = bidRepository.save(bid)

        return BidCreateResponse.from(savedBid)
    }
}
