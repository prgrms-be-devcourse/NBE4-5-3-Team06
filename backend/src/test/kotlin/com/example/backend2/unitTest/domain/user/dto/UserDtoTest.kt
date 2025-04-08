package com.example.backend2.domain.user.dto

import com.example.backend2.data.Role
import com.example.backend2.domain.user.entity.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

/**
 * 사용자 DTO의 단위 테스트 클래스
 * DTO 변환 및 응답 생성 기능을 테스트
 */
@ActiveProfiles("test")
@DisplayName("User DTO 단위 테스트")
class UserDtoTest {

    /**
     * 회원가입 응답 DTO 변환 테스트
     * User 엔티티가 UserSignUpResponse로 올바르게 변환되는지 확인
     */
    @Test
    @DisplayName("UserSignUpResponse 변환 테스트")
    fun `UserSignUpResponse from should convert User to response`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        // when
        val response = UserSignUpResponse.from(user)

        // then
        assertThat(response.userUUID).isEqualTo("test-uuid")
    }

    /**
     * 로그인 응답 DTO 변환 테스트
     * User 엔티티와 토큰이 UserSignInResponse로 올바르게 변환되는지 확인
     */
    @Test
    @DisplayName("UserSignInResponse 변환 테스트")
    fun `UserSignInResponse from should convert User and token to response`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )
        val token = "test-token"

        // when
        val response = UserSignInResponse.from(user, token)

        // then
        assertThat(response.token).isEqualTo("test-token")
        assertThat(response.userUUID).isEqualTo("test-uuid")
        assertThat(response.nickname).isEqualTo("테스트유저")
    }

    @Test
    @DisplayName("UserPutRequest 변환 테스트")
    fun `UserPutRequest from should convert User to request`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            profileImage = "http://example.com/profile.jpg",
            role = Role.USER
        )

        // when
        val request = UserPutRequest.from(user)

        // then
        assertThat(request.email).isEqualTo("test@example.com")
        assertThat(request.nickname).isEqualTo("테스트유저")
        assertThat(request.profileImage).isEqualTo("http://example.com/profile.jpg")
    }

    @Test
    @DisplayName("UserCheckRequest 변환 테스트")
    fun `UserCheckRequest from should convert User to request`() {
        // given
        val now = LocalDateTime.now()
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            profileImage = "http://example.com/profile.jpg",
            role = Role.USER,
            createdDate = now
        )

        // when
        val request = UserCheckRequest.from(user)

        // then
        assertThat(request.userUUID).isEqualTo("test-uuid")
        assertThat(request.email).isEqualTo("test@example.com")
        assertThat(request.nickname).isEqualTo("테스트유저")
        assertThat(request.profileImage).isEqualTo("http://example.com/profile.jpg")
        assertThat(request.createdAt).isEqualTo(now)
    }
} 