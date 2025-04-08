package com.example.backend2.domain.bid.controller

import com.example.backend2.domain.auction.dto.AuctionBidRequest
import com.example.backend2.domain.bid.dto.BidCreateResponse
import com.example.backend2.domain.bid.service.BidService
import com.example.backend2.global.utils.JwtProvider
import com.example.backend2.global.websocket.dto.WebSocketResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/auctions")
class BidController(
    private val bidService: BidService,
    private val jwtProvider: JwtProvider,
    private val simpMessagingTemplate: SimpMessagingTemplate,
) {
    private val log = KotlinLogging.logger {}

// 경매 입찰 컨트롤러
    @MessageMapping("/auction/bid")
    fun createBids(
        @Payload request: AuctionBidRequest,
    ) {
        val userUUID = jwtProvider.parseUserUUID(request.token)
        val nickname = jwtProvider.parseNickname(request.token)

        log.info { "입찰 요청 수신, userUUID: $userUUID, nickname: $nickname, auctionId: ${request.auctionId}, amount: ${request.amount}" }

        val response: BidCreateResponse = bidService.createBid(request.auctionId, request)
        // 입찰 성공 시 Websocket 메시지 보낼 데이터

        val res =
            WebSocketResponse(
                message = "입찰 성공",
                localDateTime = LocalDateTime.now(),
                nickname = response.nickname,
                currentBid = request.amount,
            )

        simpMessagingTemplate.convertAndSend("/sub/auction/${request.auctionId}", res)
        log.info { "입찰 브로드캐스트 완료: /sub/auction/${request.auctionId}" }
    }
}
