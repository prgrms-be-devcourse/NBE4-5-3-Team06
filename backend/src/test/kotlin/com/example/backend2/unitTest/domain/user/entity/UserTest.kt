package com.example.backend2.unitTest.domain.user.entity

import com.example.backend2.data.Role
import com.example.backend2.domain.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

/**
 * 사용자 엔티티의 단위 테스트 클래스
 * 사용자 생성 및 정보 업데이트 기능을 테스트
 */
@ActiveProfiles("test")
@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    /**
     * 사용자 생성 테스트
     * 새로운 사용자가 올바른 속성값으로 생성되는지 확인
     */
    @Test
    @DisplayName("사용자 생성 테스트")
    fun `create user should create new user`() {
        // given
        val userUUID = "test-uuid"
        val email = "test@example.com"
        val nickname = "테스트유저"
        val password = "password"
        val profileImage = "http://example.com/profile.jpg"
        val role = Role.USER

        // when
        val user = User(
            userUUID = userUUID,
            email = email,
            nickname = nickname,
            password = password,
            profileImage = profileImage,
            role = role
        )

        // then
        assertThat(user.userUUID).isEqualTo(userUUID)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.nickname).isEqualTo(nickname)
        assertThat(user.password).isEqualTo(password)
        assertThat(user.profileImage).isEqualTo(profileImage)
        assertThat(user.role).isEqualTo(role)
        assertThat(user.createdDate).isNotNull
        assertThat(user.modifiedAt).isNotNull
        assertThat(user.winners).isEmpty()
        assertThat(user.bids).isEmpty()
    }

    /**
     * 사용자 정보 업데이트 테스트
     * 사용자의 정보가 올바르게 업데이트되는지 확인
     */
    @Test
    @DisplayName("사용자 정보 업데이트 테스트")
    fun `update user should update user information`() {
        // given
        val user = User(
            userUUID = "test-uuid",
            email = "test@example.com",
            nickname = "테스트유저",
            password = "password",
            role = Role.USER
        )

        // when
        val updatedUser = user.copy(
            email = "updated@example.com",
            nickname = "수정된유저",
            profileImage = "http://example.com/new-profile.jpg"
        )

        // then
        assertThat(updatedUser.userUUID).isEqualTo(user.userUUID)
        assertThat(updatedUser.email).isEqualTo("updated@example.com")
        assertThat(updatedUser.nickname).isEqualTo("수정된유저")
        assertThat(updatedUser.password).isEqualTo(user.password)
        assertThat(updatedUser.profileImage).isEqualTo("http://example.com/new-profile.jpg")
        assertThat(updatedUser.role).isEqualTo(user.role)
    }
} 