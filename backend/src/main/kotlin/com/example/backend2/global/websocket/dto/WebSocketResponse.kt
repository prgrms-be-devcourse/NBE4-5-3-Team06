package com.example.backend2.global.websocket.dto

import java.time.LocalDateTime

data class WebSocketResponse(
    val message: String = "",
    val localDateTime: LocalDateTime = LocalDateTime.now(),
    val nickname: String = "",
    val currentBid: Int = 0,
)
