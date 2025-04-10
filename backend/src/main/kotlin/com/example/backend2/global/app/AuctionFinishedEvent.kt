package com.example.backend2.global.app


import com.example.backend2.domain.auction.entity.Auction
import org.springframework.context.ApplicationEvent

// "경매가 종료되었을 때 발생하는 이벤트"를 나타내는 클래스
class AuctionFinishedEvent(
    source: Any,
    val auction: Auction
) : ApplicationEvent(source) {

    
}