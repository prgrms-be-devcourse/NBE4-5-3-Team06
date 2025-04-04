package com.example.backend2.global.websocket

import com.example.backend2.global.utils.JwtProvider
import lombok.extern.slf4j.Slf4j
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class StompHandshakeHandler(
    val jwtProvider: JwtProvider
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        if (request !is ServletServerHttpRequest) {
            log.error("WebSocket 요청이 HTTP 요청이 아닙니다.");
            throw IllegalArgumentException("WebSocket 요청이 HTTP 요청이 아닙니다.");
        }
    }
}