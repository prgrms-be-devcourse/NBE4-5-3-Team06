package com.example.backend2.global.websocket

import com.example.backend2.global.utils.JwtProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
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
    private val log = KotlinLogging.logger {}

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val httpRequest = request as? ServletServerHttpRequest ?: return fail("WebSocket 요청이 HTTP 기반이 아님")
        val servletRequest = httpRequest.servletRequest

        val token = extractToken(servletRequest) ?: return fail("JWT 토큰이 존재하지 않음")
        if (!jwtProvider.validateToken(token)) return fail("JWT 토큰이 유효하지 않음")

        val userUUID = jwtProvider.parseUserUUID(token) ?: return fail("userUUID가 없습니다.")
        val nickname = jwtProvider.parseNickname(token) ?: return fail("nickname이 없습니다.")

        attributes["userUUID"] = userUUID
        attributes["nickname"] = nickname

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
        val token = request.getHeader("Authorization") ?: request.getParameter("token")
        if (token.isNullOrBlank()) return null

        return if (token.startsWith("Bearer ")) {
            token.substring(7)
        } else {
            token
        }
    }

    private fun fail(reason: String): Boolean {
        log.warn { "Handshake failed: $reason" }
        return false
    }
}
