package com.example.backend2.global.messaging.subscriber;

import lombok.RequiredArgsConstructor;
import org.example.bidflow.global.messaging.dto.MessagePayload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageSubscriber<MessagePayload> {

    @Override
    public void subscribe(String topic) {

    }

    @Override
    public void onMessage(String topic, MessagePayload payload) {
        System.out.println("Redis 구독 메시지 수신: " + payload);
    }
}