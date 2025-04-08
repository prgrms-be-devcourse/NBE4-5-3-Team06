package com.example.backend2.global.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JwtProviderTest {
    private lateinit var jwtProvider: JwtProvider

    @BeforeEach
    fun setUp() {
        jwtProvider = JwtProvider()
    }

    @Test
    fun `토큰 생성 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"

        // When
        val token = jwtProvider.generateToken(claims, email)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `토큰에서 클레임 추출 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val extractedClaims = jwtProvider.parseClaims(token)

        // Then
        assertNotNull(extractedClaims)
        assertEquals("test-uuid-123", extractedClaims["userUUID"])
        assertEquals("testUser", extractedClaims["nickname"])
        assertEquals("ROLE_USER", extractedClaims["role"])
        assertEquals(email, extractedClaims.subject)
    }

    @Test
    fun `토큰에서 userUUID 추출 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val userUUID = jwtProvider.parseUserUUID(token)

        // Then
        assertNotNull(userUUID)
        assertEquals("test-uuid-123", userUUID)
    }

    @Test
    fun `토큰에서 nickname 추출 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val nickname = jwtProvider.parseNickname(token)

        // Then
        assertNotNull(nickname)
        assertEquals("testUser", nickname)
    }

    @Test
    fun `토큰에서 role 추출 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val role = jwtProvider.parseRole(token)

        // Then
        assertNotNull(role)
        assertEquals("ROLE_USER", role)
    }

    @Test
    fun `토큰에서 username 추출 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val username = jwtProvider.getUsername(token)

        // Then
        assertNotNull(username)
        assertEquals(email, username)
    }

    @Test
    fun `유효한 토큰 검증 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"
        val token = jwtProvider.generateToken(claims, email)

        // When
        val isValid = jwtProvider.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `잘못된 형식의 토큰 검증 실패 테스트`() {
        // Given
        val invalidToken = "invalid.token.format"

        // When
        val isValid = jwtProvider.validateToken(invalidToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `만료된 토큰 검증 실패 테스트`() {
        // Given
        val claims =
            mapOf(
                "userUUID" to "test-uuid-123",
                "nickname" to "testUser",
                "role" to "ROLE_USER",
            )
        val email = "test@example.com"

        // 만료된 토큰을 생성하기 위해 JwtProvider 클래스를 수정하여 만료 시간을 과거로 설정
        val tempJwtProvider = JwtProvider()
        val secretKeyField = JwtProvider::class.java.getDeclaredField("secretKey")
        secretKeyField.isAccessible = true
        val secretKey = secretKeyField.get(tempJwtProvider) as javax.crypto.SecretKey

        val expirationTimeField = JwtProvider::class.java.getDeclaredField("EXPIRATION_TIME")
        expirationTimeField.isAccessible = true
        val expirationTime = expirationTimeField.get(null) as Long

        val expiredToken =
            io.jsonwebtoken.Jwts
                .builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(Date(System.currentTimeMillis() - expirationTime * 2))
                .setExpiration(Date(System.currentTimeMillis() - expirationTime))
                .signWith(secretKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact()

        // When
        val isValid = jwtProvider.validateToken(expiredToken)

        // Then
        assertFalse(isValid)
    }
}
