package com.example.backend2.unitTest.domain.user.repository

import com.example.backend2.data.Role
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

/**
 * 사용자 레포지토리의 단위 테스트 클래스
 * 사용자 저장 및 조회 기능을 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 단위 테스트")
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    /**
     * 사용자 저장 테스트
     * 새로운 사용자가 데이터베이스에 정상적으로 저장되는지 확인
     */
    @Test
    @DisplayName("사용자 저장 테스트")
    fun `save should persist user`() {
        // given
        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )

        // when
        val savedUser = userRepository.save(user)

        // then
        assertThat(savedUser.userUUID).isEqualTo("test-uuid")
        assertThat(savedUser.email).isEqualTo("test@example.com")
        assertThat(savedUser.nickname).isEqualTo("테스트유저")
        assertThat(savedUser.role).isEqualTo(Role.USER)
    }

    /**
     * 이메일로 사용자 조회 테스트
     * 존재하는 이메일로 조회 시 해당 사용자가 반환되는지 확인
     */
    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    fun `findByEmail should return user when exists`() {
        // given
        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )
        userRepository.save(user)

        // when
        val foundUser = userRepository.findByEmail("test@example.com")

        // then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().userUUID).isEqualTo("test-uuid")
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
    }

    @Test
    @DisplayName("이메일 또는 닉네임으로 사용자 조회 테스트")
    fun `findByEmailOrNickname should return user when exists`() {
        // given
        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )
        userRepository.save(user)

        // when
        val foundByEmail = userRepository.findByEmailOrNickname("test@example.com", "다른닉네임")
        val foundByNickname = userRepository.findByEmailOrNickname("다른이메일@example.com", "테스트유저")

        // then
        assertThat(foundByEmail).isPresent
        assertThat(foundByEmail.get().email).isEqualTo("test@example.com")
        assertThat(foundByNickname).isPresent
        assertThat(foundByNickname.get().nickname).isEqualTo("테스트유저")
    }

    @Test
    @DisplayName("UUID로 사용자 조회 테스트")
    fun `findByUserUUID should return user when exists`() {
        // given
        val user =
            User(
                userUUID = "test-uuid",
                email = "test@example.com",
                nickname = "테스트유저",
                password = "password",
                role = Role.USER,
            )
        userRepository.save(user)

        // when
        val foundUser = userRepository.findByUserUUID("test-uuid")

        // then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().userUUID).isEqualTo("test-uuid")
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
    }
} 
