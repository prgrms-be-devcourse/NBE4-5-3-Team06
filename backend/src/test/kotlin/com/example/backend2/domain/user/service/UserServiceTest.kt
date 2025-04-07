package com.example.backend2.domain.user.service

import com.example.backend2.data.Role
import com.example.backend2.domain.user.dto.UserPutRequest
import com.example.backend2.domain.user.dto.UserSignInRequest
import com.example.backend2.domain.user.dto.UserSignUpRequest
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.utils.JwtProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    @Test
    @DisplayName("사용자 정보 조회 성공")
    fun userCheck() {
        // Given - 유저 직접 저장
        val user = User(
            userUUID = "user-123",
            email = "test@example.com",
            password = "test1234",
            nickname = "tester",
            role = Role.USER
        )
        userRepository.save(user)

        // When
        val result = userService.getUserCheck(user.userUUID)

        // UserCheckRequest 객체에서 email,nickname값이 저장한 User 객체의 값과 일치하는지 확인
        assertEquals("test@example.com", result.email)
        assertEquals("tester", result.nickname)
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 사용자 정보를 찾을 수 없음")
    fun noUserCheck() {
        //존재 하지 않는 userUUId 설정
        val userUUID ="No-exist-userUUID"

        //예외 발생을 감지
        val exception =assertThrows<ServiceException> {
            userService.getUserCheck(userUUID)
        }
        // code,message가 예상한 결과 인지 확인
        assertEquals("400", exception.code)
        assertEquals("사용자가 존재하지 않습니다.", exception.message)

    }
    @Test
    @DisplayName("사용자 정보 수정 성공")
    fun updateUser() {
        val userUUID = "user-123"

        val user = User(
            userUUID = userUUID, //UserUUID로 유저 식별
            email = "test@example.com",
            password = "test1234",
            nickname = "tester",
            profileImage = "test.jpg",
            role = Role.USER
        )
        userRepository.save(user)

        //  수정하고자 하는 값을 담은 DTO
        val request = UserPutRequest(
            email = "test2@example.com",
            nickname = "tester2",
            profileImage = "new_test.jpg"
        )

        val result = userService.updateUser(userUUID, request)

        assertEquals("test2@example.com", result.email)
        assertEquals("tester2", result.nickname)
        assertEquals("new_test.jpg", result.profileImage)
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 사용자 정보를 찾을 수 없음")
    fun updateUserFail(){
        val userUUID = "No-exist-userUUID"

        //사용자가 보내는 정보 수정 요청
        val request = UserPutRequest(
            email = "test2@example.com",
            nickname = "test2",
            profileImage = "new_test.jpg"
        )
        //예외 발생
        val exception =assertThrows<ServiceException> {
            userService.updateUser(userUUID, request)
        }
        //기대한 값과 같은지 확인하는 코드
        assertEquals("404", exception.code)
        assertEquals("사용자 정보를 찾을 수 없습니다.", exception.message)
    }
} 