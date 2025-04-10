package com.example.backend2.global.messaging.subscriber

import com.example.backend2.global.messaging.dto.MessagePayload
import org.springframework.stereotype.Component

@Component
class RedisMessageSubscriber : MessageSubscriber<MessagePayload> {
    override fun subscribe(topic: String) {
    }

    override fun onMessage(
        topic: String,
        payload: MessagePayload,
    ) {
        println("Redis 구독 메시지 수신: $payload")
    }
}
