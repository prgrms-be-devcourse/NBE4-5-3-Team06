package com.example.backend2.global.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor("ff124f1-51e8-775g-66ru-eer8e7ntefffb2e123456789012345".toByteArray())
    private val expirationTime: Long = 1000L * 60 * 60 * 24 // 24 hours in milliseconds

    fun generateToken(claims: Map<String, Any>, email: String): String {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun parseUserUUID(token: String): String? {
        return parseClaims(token).get("userUUID", String::class.java)
    }

    fun parseNickname(token: String): String? {
        return parseClaims(token).get("nickname", String::class.java)
    }

    fun parseRole(token: String): String? {
        return parseClaims(token).get("role", String::class.java)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        }
    }

    fun getUsername(token: String): String? {
        return parseClaims(token).subject
    }
}
