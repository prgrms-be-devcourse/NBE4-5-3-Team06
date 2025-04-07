package com.example.backend2.integrationTest.domain.user

import com.example.backend2.domain.user.dto.UserSignUpRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
// @ActiveProfiles("test")
@DisplayName("사용자 통합 테스트")
class UserIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun registerUser() {
        val request =
            UserSignUpRequest(
                email = "test@example.com",
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
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("회원가입 성공"))
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    fun loginSuccess() {
        val request =
            UserSignUpRequest(
                email = "test@example.com",
                password = "password123",
                nickname = "testUser",
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인이 완료되었습니다."))
    }
}
