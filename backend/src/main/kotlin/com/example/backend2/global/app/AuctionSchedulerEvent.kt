package com.example.backend2.global.app

import com.example.backend2.data.AuctionStatus
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.redis.core.RedisTemplate
import java.time.LocalDateTime
import java.util.*

// 경매 종료 -> 종료 상태 변경 및 이벤트 발행
@Service
class AuctionSchedulerEvent(
    private val redisTemplate: RedisTemplate<String, String>,
    private val auctionRepository: AuctionRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    // @Scheduled(fixedDelay = 30000) -> 약 30초
    // @Scheduled(fixedDelay = 10000) -> 5초
    // @Scheduled(fixedDelay = 1000) -> 아예 실행 X. 로그도 안뜨는거같던데
    @Transactional
    @Scheduled(fixedDelay = 3000) // 3초마다 실행 -> 경매 종료 시 바로 실행
    fun processAuctions() {
        val keysSet: Set<String> = redisTemplate?.keys("auction:*") ?: emptySet()
        
        if (keysSet.isEmpty()) {
            log.info { "[Scheduler] 현재 진행 중인 경매가 없습니다." }
            return
        }

        for (key in keysSet) {
            val auctionId = key.split(":").dropLastWhile { element -> element.isEmpty() }.toTypedArray()[1].toLong()
            val auctionOpt = auctionRepository?.findById(auctionId) ?: continue
            
            if (auctionOpt.isEmpty) continue

            val auction: Auction = auctionOpt.get()

            // 경매 종료 시간 도달 && 아직 종료 처리 안 됐을 때
            if (LocalDateTime.now().isAfter(auction.endTime) && auction.status != AuctionStatus.FINISHED) {
                // Update auction status
                val updatedAuction = auction.copy(status = AuctionStatus.FINISHED)
                auctionRepository?.save(updatedAuction)
                log.info { "[Scheduler] 경매 종료 처리 - 경매 ID: $auctionId" }

                // 즉시 이벤트 발행
                eventPublisher?.publishEvent(AuctionFinishedEvent(this, updatedAuction))
            }
        }
    }
} //
