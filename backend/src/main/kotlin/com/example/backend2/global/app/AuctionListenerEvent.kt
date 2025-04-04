package com.example.backend2.global.app


import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
import com.example.backend2.global.redis.RedisCommon
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.*

// 이벤트 수신 및 낙찰자 처리 후 WebSocket 메시지 전송

@Component
class AuctionListenerEvent(
    val winnerRepository: WinnerRepository? = null,
    val userRepository: UserRepository? = null,
    val simpMessagingTemplate: SimpMessagingTemplate? = null,
    val redisCommon: RedisCommon? = null
) {


    // 이벤트 발생 시 즉시 실행
    @EventListener
    fun handleAuctionFinished(event: AuctionFinishedEvent) {
        val auction: Auction = event.auction
        val auctionId: Long? = auction.auctionId
        val key = "auction:$auctionId"

        // 입찰 정보 확인
        val amount = redisCommon?.getFromHash(key, "amount", Int::class.java)
        val userUUID = redisCommon?.getFromHash(key, "userUUID", String::class.java)

        if (amount == null) {
            log.warn("[Scheduler] 입찰 금액 없음, 경매 ID: $auctionId")
            return
        }

        if (userUUID == null) {
            log.warn("[AuctionEvent] 입찰자 없음 - 경매 ID: auctionId ")
            return
        }

        val user: User? = userRepository?.findByUserUUID(userUUID)
        if (user == null) {
            log.warn("[AuctionEvent] 사용자 없음 - UUID: $userUUID , 경매 ID: auctionId")
            return
        }


        // 낙찰자 저장
        val winner: Winner = Winner(
            winningBid = amount,
            winTime = auction.endTime,
            user = user,
            auction = auction
        )

        winnerRepository?.save(winner)
        log.info("[AuctionEvent] 낙찰자 저장 - 경매 ID: $auctionId , 금액: $amount")

        // WebSocket 메시지 전송
        val message: MutableMap<String, Any?>()
        message["auctionId"] = auctionId
        message["winnerNickname"] = user.nickname
        message["winningBid"] = amount



        simpMessagingTemplate.convertAndSend("/sub/auction/$auctionId", message)
       log.info("[AuctionEvent] WebSocket 전송 완료 - 경매 ID: $auctionId")
    }
}