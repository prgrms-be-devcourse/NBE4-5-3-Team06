package com.example.backend2.global.messaging.dto

class MessagePayload {
    val eventType: String? = null // 이벤트 타입 (예: AUCTION_BID, USER_SIGNUP 등)
    val sender: String? = null // 발신자 정보
    val data: Any? = null // JSON 형태 데이터
}
