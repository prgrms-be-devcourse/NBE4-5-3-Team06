package com.example.backend2.global.messaging.publisher

import com.example.backend2.global.messaging.dto.MessagePayload
import org.springframework.stereotype.Component

/**
 * Redis Pub/Sub 기반 구현체
 */
@Component
class RedisMessagePublisher(
    val redisTemplate: RedisTemplate<String, Any>,
) : MessagePublisher<MessagePayload?> {
    override fun publish(
        topic: String,
        message: MessagePayload,
    ) {
        val json = convertToJson(message)
        redisTemplate.convertAndSend(topic, json)
    }

    // 직렬화
    private fun convertToJson(messagePayload: MessagePayload): String {
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.convertValue(messagePayload, String.class);
        return messagePayload.toString()
    }
}
