package com.example.backend2.global.redis.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// 분산 락을 위한 Redisson 설정
@Configuration
class RedissonConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
//    @Value("\${spring.data.redis.password}") private val password: String,
) {
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config
            .useSingleServer()
            .setAddress("redis://$host:$port")
//            .setPassword(password)
        return Redisson.create(config)
    }
}
