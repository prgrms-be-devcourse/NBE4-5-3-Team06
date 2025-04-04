package com.example.backend2.global.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketMessageBrokerConfig (
    val stompHandshakeHandler: StompHandshakeHandler
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/sub")
        config.setApplicationDestinationPrefixes("app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/auction/{auctionId}")
            .setAllowedOrigins("http://35.203.149.35:3000")
            .addInterceptors(stompHandshakeHandler)
            .withSockJS()
    }
}