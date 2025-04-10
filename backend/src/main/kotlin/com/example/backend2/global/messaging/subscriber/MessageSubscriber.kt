package com.example.backend2.global.messaging.subscriber

interface MessageSubscriber<T> {
    fun subscribe(topic: String)

    fun onMessage(
        topic: String,
        payload: T,
    )
}
