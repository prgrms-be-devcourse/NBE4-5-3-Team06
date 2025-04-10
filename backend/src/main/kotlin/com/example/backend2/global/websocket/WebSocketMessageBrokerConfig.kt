package com.example.backend2.global.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketMessageBrokerConfig(
    val stompHandshakeHandler: StompHandshakeHandler,
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/sub") // 구독(Subscribe) 경로 (서버 -> 클라이언트 로 메시지 보낼 때)
        config.setApplicationDestinationPrefixes("/app") // 메시지 보낼 prefix (클라이언트 -> 서버)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 클라이언트가 연결할 엔드포인트
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("http://localhost:*", "http://35.203.149.35:3000") // CORS 허용 (모든 도메인 허용)
            .addInterceptors(stompHandshakeHandler) // HandshakeInterceptor 추가 (JWT 검증)
            .withSockJS() // socket fallback 지원
    }
}
