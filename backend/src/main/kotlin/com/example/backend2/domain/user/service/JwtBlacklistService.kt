package com.example.backend2.domain.user.service

import com.example.backend2.global.redis.RedisCommon
import com.example.backend2.global.utils.JwtProvider
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service

@Service
class JwtBlacklistService(
    private val redisCommon: RedisCommon,
    private val jwtProvider: JwtProvider
) {

    companion object {
        private const val BLACKLIST_PREFIX = "blacklsit:"
    }

    // 블랙리스트에 추가 (로그아웃 처리)
    fun addToBlackList(token: String) {
        val claims: Claims


    }

}