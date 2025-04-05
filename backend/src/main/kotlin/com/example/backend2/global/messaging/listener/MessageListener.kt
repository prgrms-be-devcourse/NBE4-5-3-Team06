package com.example.backend2.global.messaging.listener

import com.example.backend2.global.messaging.dto.MessagePayload
import org.springframework.stereotype.Repository

/**
 * 실제 메시지를 받아 로직 처리(핸들링)라는 Listener
 * Subscribe의 onMessage 호출되거나,
 * 혹은 @KafkaListener 등을 통해 직접 로직 처리 가능할 듯!
 */

@Repository
interface MessageListener {
    fun handleMessage(payload: MessagePayload)
}
