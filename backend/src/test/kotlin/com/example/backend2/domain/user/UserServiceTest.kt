package com.example.backend2.domain.user.service

import com.example.backend2.data.Role
import com.example.backend2.domain.user.dto.UserPutRequest
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.utils.JwtProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test


class UserServiceTest{

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtProvider: JwtProvider
    private lateinit var emailService: EmailService

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, passwordEncoder, jwtProvider, emailService)
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