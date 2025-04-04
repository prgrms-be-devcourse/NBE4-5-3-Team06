package com.example.backend2.global.messaging.subscriber;

public interface MessageSubscriber<T> {
    void subscribe(String topic);
    void onMessage(String topic,T payload);

}