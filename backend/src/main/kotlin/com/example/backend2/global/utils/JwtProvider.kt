@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.global.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider {
    companion object {
        private const val SECRET = "ff124f1-51e8-775g-66ru-eer8e7ntefffb2e123456789012345"
        private const val EXPIRATION_TIME: Long = 1000L * 60 * 60 * 24 // 24시간
    }

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(SECRET.toByteArray())

    fun generateToken(
        claims: Map<String, Any>,
        email: String,
    ): String =
        Jwts
            .builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()

    fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun parseUserUUID(token: String): String? = getClaim(token, "userUUID")

    fun parseNickname(token: String): String? = getClaim(token, "nickname")

    fun parseRole(token: String): String? = getClaim(token, "role")

    fun getUsername(token: String): String? = parseClaims(token).subject

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: JwtException) {
            false
        }

    private fun getClaim(
        token: String,
        key: String,
    ): String? = parseClaims(token).get(key, String::class.java)
}
