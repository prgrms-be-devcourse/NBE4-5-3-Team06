package com.example.backend2.global.redis

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.data.AuctionStatus
import com.example.backend2.global.app.AuctionFinishedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

// 경매 종료 -> 종료 상태 변경 및 이벤트 발행
@Service
class AuctionSchedulerEvent(
     val redisTemplate: RedisTemplate<String, String>? = null,
     val auctionRepository: AuctionRepository? = null,
     val eventPublisher: ApplicationEventPublisher? = null
) {


    // @Scheduled(fixedDelay = 30000) -> 약 30초
    // @Scheduled(fixedDelay = 10000) -> 5초
    // @Scheduled(fixedDelay = 1000) -> 아예 실행 X. 로그도 안뜨는거같던데
    @Transactional
    @Scheduled(fixedDelay = 3000) // 3초마다 실행 -> 경매 종료 시 바로 실행
    fun processAuctions() {
        val keys: Set<String> = redisTemplate.keys("auction:*")

        if (keys.isEmpty()) {
            AuctionSchedulerEvent.log.info("[Scheduler] 현재 진행 중인 경매가 없습니다.")
            return
        }

        for (key in keys) {
            val auctionId = key.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toLong()
            val auctionOpt: Optional<Auction> = auctionRepository.findById(auctionId)
            if (auctionOpt.isEmpty()) continue

            val auction: Auction = auctionOpt.get()

            // 경매 종료 시간 도달 && 아직 종료 처리 안 됐을 때
            if (LocalDateTime.now().isAfter(auction.getEndTime()) && auction.getStatus() !== AuctionStatus.FINISHED) {
                auction.setStatus(AuctionStatus.FINISHED)
                auctionRepository.save(auction)
                AuctionSchedulerEvent.log.info("[Scheduler] 경매 종료 처리 - 경매 ID: {}", auctionId)

                // 즉시 이벤트 발행
                eventPublisher.publishEvent(AuctionFinishedEvent(this, auction))
            }
        }
    }
} //
