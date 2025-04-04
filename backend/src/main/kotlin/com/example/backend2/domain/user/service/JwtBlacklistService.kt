package com.example.backend2.domain.user.service

import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import com.example.backend2.global.utils.JwtProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class JwtBlacklistService(
    private val redisCommon: RedisCommon,
    private val jwtProvider: JwtProvider
) {

    companion object {
        private const val BLACKLIST_PREFIX = "blacklist:"
    }

    // 블랙리스트에 추가 (로그아웃 처리)
    fun addToBlacklist(token: String) {
        val claims: Claims

        try {
            claims = jwtProvider.parseClaims(token)
        } catch (e: ExpiredJwtException) {
            throw ServiceException(HttpStatus.UNAUTHORIZED.value().toString(), "만료된 토큰입니다.")
        } catch (e: Exception) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value().toString(), "유효하지 않은 토큰입니다.")
        }

        val expirationTime = claims.expiration.time
        val ttl = expirationTime - System.currentTimeMillis()

        // 만료 시간 계산 (하루)
        // 이 값은 Redis에 저장된 토큰이 남은 시간만큼 유효하도록 설정
        val ttlSeconds = ttl / 1000

        val key = getKey(token)
        redisCommon.putInHash(key, "blacklisted", "true")
        redisCommon.setExpireAt(key, LocalDateTime.now().plusSeconds(ttlSeconds))
    }

    // 키 생성
    private fun getKey(token: String): String {
        return "$BLACKLIST_PREFIX$token"
    }

    // 블랙리스트 여부 확인
    fun isBlacklisted(token: String): Boolean {
        val key = getKey(token)
        return "true" == redisCommon.getFromHash(key, "blacklisted", String::class.java)
    }
}