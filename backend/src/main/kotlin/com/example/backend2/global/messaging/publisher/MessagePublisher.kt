package com.example.backend2.global.messaging.publisher

interface MessagePublisher<T> {
    /**
     * 특정 토픽(채널)에 메세지를 발행
     * @param topic     토픽(채널)명
     * @param message   발행할 메시지
     */
    fun publish(
        topic: String,
        message: T,
    )
}
