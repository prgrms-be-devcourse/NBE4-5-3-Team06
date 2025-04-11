package com.example.backend2.integrationTest.domain.user

import com.example.backend2.domain.user.dto.EmailSendRequest
import com.example.backend2.domain.user.dto.EmailVerificationRequest
import com.example.backend2.domain.user.dto.UserSignInRequest
import com.example.backend2.domain.user.dto.UserSignUpRequest
import com.example.backend2.domain.user.service.EmailService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 통합 테스트")
class UserIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var emailService: EmailService

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun registerUser() {
        val email = "test@example.com"
        val verificationCode = "123456"

        `when`(emailService.isVerificationExpired(email)).thenReturn(false) // "이 이메일의 인증 유효기간이 만료되지 않았다고 가정해줘."
        `when`(emailService.isVerified(email)).thenReturn(true) // "이 이메일은 이미 인증된 상태라고 가정해줘."
        `when`(emailService.verifyCode(email, verificationCode)).thenReturn(true) // "이 이메일과 인증코드가 정확히 일치한다고 가정해줘."

        // 1. 이메일 인증 코드 요청
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/send-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(EmailSendRequest(email))),
            ).andExpect(MockMvcResultMatchers.status().isOk)

        // 2. 이메일 인증 코드 확인
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(EmailVerificationRequest(email, verificationCode))),
            ).andExpect(MockMvcResultMatchers.status().isOk)

        // 3. 회원가입 요청
        val request =
            UserSignUpRequest(
                email = email,
                password = "password123",
                nickname = "testUser",
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원가입이 완료되었습니다."))
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    fun loginSuccess() {
        val email = "test@example.com"
        val password = "password123"
        val nickname = "testUser"
        val verificationCode = "123456"

        // 이메일 인증 설정
        `when`(emailService.isVerificationExpired(email)).thenReturn(false)
        `when`(emailService.isVerified(email)).thenReturn(true)
        `when`(emailService.verifyCode(email, verificationCode)).thenReturn(true)

        // 1. 이메일 인증 코드 요청
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/send-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(EmailSendRequest(email))),
            ).andExpect(MockMvcResultMatchers.status().isOk)

        // 2. 이메일 인증 코드 확인
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(EmailVerificationRequest(email, verificationCode))),
            ).andExpect(MockMvcResultMatchers.status().isOk)

        // 3. 회원가입
        val signUpRequest =
            UserSignUpRequest(
                email = email,
                password = password,
                nickname = nickname,
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andExpect(MockMvcResultMatchers.status().isCreated)

        // 4. 로그인
        val loginRequest =
            UserSignInRequest(
                email = email,
                password = password,
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인이 완료되었습니다."))
    }
}