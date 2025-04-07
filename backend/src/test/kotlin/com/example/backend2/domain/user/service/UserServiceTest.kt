package com.example.backend2.domain.user.service

import com.example.backend2.data.Role
import com.example.backend2.domain.user.dto.*
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.utils.JwtProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

/**
 * 사용자 서비스의 단위 테스트 클래스
 * 회원가입, 로그인, 사용자 정보 수정 등의 기능을 테스트
 */
@DisplayName("UserService 단위 테스트")
class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtProvider: JwtProvider
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtProvider = mockk()
        emailService = mockk()
        userService = UserService(userRepository, passwordEncoder, jwtProvider, emailService)
    }

    /**
     * 회원가입 성공 테스트
     * 이메일 인증이 완료된 사용자의 회원가입이 성공적으로 처리되는지 확인
     */
    @Test
    @DisplayName("회원가입 성공 테스트")
    fun `signup should create new user successfully`() {
        // given
        val request = UserSignUpRequest(
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저"
        )

        every { emailService.isVerificationExpired(any()) } returns false
        every { emailService.isVerified(any()) } returns true
        every { userRepository.findByEmailOrNickname(any(), any()) } returns Optional.empty()
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { userRepository.save(any()) } returns User(
            userUUID = "test-uuid",
            email = request.email,
            password = "encodedPassword",
            nickname = request.nickname,
            role = Role.USER
        )
        every { emailService.deleteVerificationCode(any()) } returns Unit

        // when
        val response = userService.signup(request)

        // then
        assertThat(response.userUUID).isNotNull
        verify { userRepository.save(any()) }
        verify { emailService.deleteVerificationCode(request.email) }
    }

    /**
     * 이메일 인증 만료 시 회원가입 실패 테스트
     * 이메일 인증이 만료된 경우 회원가입이 실패하고 적절한 예외가 발생하는지 확인
     */
    @Test
    @DisplayName("이메일 인증 만료 시 회원가입 실패 테스트")
    fun `signup should throw exception when email verification is expired`() {
        // given
        val request = UserSignUpRequest(
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저"
        )

        every { emailService.isVerificationExpired(any()) } returns true

        // when & then
        val exception = assertThrows<ServiceException> {
            userService.signup(request)
        }

        assertThat(exception.message).isEqualTo("이메일 인증이 만료되었습니다. 다시 인증해 주세요.")
    }

    /**
     * 로그인 성공 테스트
     * 올바른 이메일과 비밀번호로 로그인 시 토큰과 사용자 정보가 반환되는지 확인
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    fun `login should return token and user info`() {
        // given
        val request = UserSignInRequest(
            email = "test@example.com",
            password = "password"
        )

        val user = User(
            userUUID = "test-uuid",
            email = request.email,
            password = "encodedPassword",
            nickname = "테스트유저",
            role = Role.USER
        )

        every { userRepository.findByEmail(any()) } returns Optional.of(user)
        every { jwtProvider.generateToken(any(), any()) } returns "test-token"

        // when
        val response = userService.login(request)

        // then
        assertThat(response.token).isEqualTo("test-token")
        assertThat(response.userUUID).isEqualTo("test-uuid")
        assertThat(response.nickname).isEqualTo("테스트유저")
    }

    /**
     * 사용자 정보 수정 테스트
     * 사용자의 닉네임, 프로필 이미지, 이메일 정보가 올바르게 수정되는지 확인
     */
    @Test
    @DisplayName("사용자 정보 수정 테스트")
    fun `updateUser should update user information`() {
        // given
        val userUUID = "test-uuid"
        val request = UserPutRequest(
            nickname = "수정된닉네임",
            profileImage = "http://example.com/new-profile.jpg",
            email = "updated@example.com"
        )

        val existingUser = User(
            userUUID = userUUID,
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저",
            role = Role.USER
        )

        every { userRepository.findByUserUUID(any()) } returns Optional.of(existingUser)
        every { userRepository.save(any()) } returns existingUser.copy(
            nickname = request.nickname!!,
            profileImage = request.profileImage,
            email = request.email!!
        )

        // when
        val response = userService.updateUser(userUUID, request)

        // then
        assertThat(response.nickname).isEqualTo("수정된닉네임")
        assertThat(response.profileImage).isEqualTo("http://example.com/new-profile.jpg")
        assertThat(response.email).isEqualTo("updated@example.com")
    }
} 