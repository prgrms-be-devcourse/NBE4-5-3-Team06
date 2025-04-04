package com.example.backend2.domain.bid.service

import com.example.backend2.domain.auction.dto.AuctionBidRequest
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.bid.entity.Bid
import com.example.backend2.domain.bid.repository.BidRepository
import com.example.backend2.global.redis.RedisCommon
import lombok.RequiredArgsConstructor

@org.springframework.stereotype.Service
@RequiredArgsConstructor
class BidService(
    private val auctionService: AuctionService,
    private val userService: UserService,
    private val bidRepository: BidRepository,
    private val redisCommon: RedisCommon,
    private val jwtProvider: JwtProviderl,
) {
    @Transactional
    fun createBid(
        auctionId: Long,
        request: AuctionBidRequest,
    ): BidCreateResponse {
        val hashKey = "auction:$auctionId"
        val now = java.time.LocalDateTime.now()

        // 유저 및 경매 정보 가져오기
        val userUUID: String = jwtProvider.parseUserUUID(request.getToken()) // jwt토큰 파싱해 유저UUID 가져오기
        val user: User = userService.getUserByUUID(userUUID)
        val auction: Auction = auctionService.getAuctionWithValidation(auctionId)

        // 경매 시간 검증
        validateAuctionTime(now, auction)

        // Redis에서 현재 최고가 조회
        val amount: Int = redisCommon.getFromHash(hashKey, "amount", Int::class.java)
        val highestUserUUID: String = redisCommon.getFromHash(hashKey, "userUUID", String::class.java) // 현재 최고 입찰자
        val currentBidAmount = amount ?: auction.getStartPrice() // DB 테스트를 위한 redis에 없으면 시작가로 설정

        if (userUUID == highestUserUUID) {
            throw ServiceException(
                org.springframework.http.HttpStatus.BAD_REQUEST
                    .toString(),
                "이미 최고 입찰자입니다. 다른 사용자의 입찰을 기다려주세요.",
            )
        }

        // 최소 입찰 단위 검증
        validateBidAmount(request.getAmount(), currentBidAmount, auction.getMinBid())

        // Redis에 입찰 정보 갱신
        redisCommon.putInHash(hashKey, "amount", request.getAmount())
        redisCommon.putInHash(hashKey, "userUUID", userUUID)

        // DB 저장 (낙찰용 로그로 남김)
        val bid: Bid = Bid.createBid(auction, user, request.getAmount(), java.time.LocalDateTime.now())
        bidRepository.save(bid)

        return BidCreateResponse.from(bid)
    }

    // 경매 시간 유효성 검증
    private fun validateAuctionTime(
        now: java.time.LocalDateTime,
        auction: Auction,
    ) {
        if (now.isBefore(auction.getStartTime())) {
            throw ServiceException(
                org.springframework.http.HttpStatus.BAD_REQUEST
                    .toString(),
                "경매가 시작 전입니다.",
            )
        } else if (now.isAfter(auction.getEndTime())) {
            throw ServiceException(
                org.springframework.http.HttpStatus.BAD_REQUEST
                    .toString(),
                "경매가 종료 되었습니다.",
            )
        }
    }

    /** 입찰 금액 유효성 검증
     * @param newAmount         받아온 입찰 금액
     * @param currentAmount     최근 조희 금액
     * @param minBidAmount      최소 입찰 금액 단위
     */
    private fun validateBidAmount(
        newAmount: Int,
        currentAmount: Int,
        minBidAmount: Int,
    ) {
        if (newAmount <= currentAmount) {
            throw ServiceException(
                org.springframework.http.HttpStatus.BAD_REQUEST
                    .toString(),
                "입찰 금액이 현재 최고가보다 낮습니다.",
            )
        }

        if (newAmount < (currentAmount + minBidAmount)) {
            throw ServiceException(
                org.springframework.http.HttpStatus.BAD_REQUEST
                    .toString(),
                "입찰 금액이 최소 입찰 단위보다 작습니다. 최소 " + (currentAmount + minBidAmount) + "원 이상 입찰해야 합니다.",
            )
        }
    }
} //
