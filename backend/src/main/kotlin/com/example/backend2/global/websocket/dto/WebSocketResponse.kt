package com.example.backend2.global.websocket.dto

import org.springframework.cglib.core.Local

data class WebSocketResponse(
    val message: String,
    val localDateTime: Local,
    val nickname: String,
    val currentBid: Int,
)
