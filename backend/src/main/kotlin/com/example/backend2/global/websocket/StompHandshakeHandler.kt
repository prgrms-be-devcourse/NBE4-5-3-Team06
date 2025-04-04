package com.example.backend2.global.websocket

import com.example.backend2.global.utils.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class StompHandshakeHandler(
    private val jwtProvider: JwtProvider,
) : HandshakeInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val httpRequest = request as? ServletServerHttpRequest
        if (httpRequest == null) return fail("WebSocket 요청이 HTTP 기반이 아님")

        val ServletRequest = httpRequest.servletRequest

        val token =
            extractToken(httpRequest)
                ?: return fail("JWT 토큰이 존재하지 않습니다.")

        val token = extractToken(httpRequest)
        if (token == null) return fail("JWT 토큰이 존재하지 않습니다.")

        val userUUID = jwtProvider.parseUserUUID(token)

        attributes["userUUID"] = userUUID ?: throw IllegalArgumentException("userUUID가 없습니다.")
        attributes["nickname"] = nickname ?: throw IllegalArgumentException("nickname이 없습니다.")

        // 토큰 유효성 검사
        if (!jwtProvider.validateToken(token)) {
            throw IllegalArgumentException("유효하지 않은 토큰입니다.")
        }
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
        // 추가 로직 필요 시 사용.
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val token = request.getHeader("Authorization")

        if (token == null) {
            token = request.getParameter("token")
        }

        return if (token != null && token.startsWith("Bearer ")) {
            token.substring(7)
        } else {
            token
        }
    }

    private fun fail(reason: String): Boolean {
        log.warn("Handshake failed: $reason")
        return false
    }
}
