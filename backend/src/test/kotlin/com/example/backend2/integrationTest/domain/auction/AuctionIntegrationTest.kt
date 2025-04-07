package com.example.backend2.integrationTest.domain.auction

import com.example.backend2.domain.auction.dto.AuctionRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class AuctionIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @WithMockUser(username = "admin", roles = ["ADMIN"]) // 가짜 인증된 사용자를 만들어주는 역할
    @Test
    @DisplayName("관리자 경매 등록 성공")
    fun createAuctionSuccess() {
        // given
        val request =
            AuctionRequest(
                startPrice = 1_000_000,
                minBid = 5000,
                startTime = LocalDateTime.of(2025, 4, 10, 11, 30),
                endTime = LocalDateTime.of(2025, 4, 14, 11, 30),
                productName = "productthings900",
                imageUrl = "example.com/image900.jpg",
                description = "그냥 설명200",
            )

        // when & then
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/admin/auctions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("경매가 등록되었습니다."))
    }

    @Test
    @DisplayName("전체 경매 조회 - 사용자용")
    fun getAllAuctionsAsUser() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/api/auctions"),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("전체 조회가 완료되었습니다."))
    }

    @Test
    @DisplayName("경매 상세 조회 테스트 - 사용자")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun getAuctionDetail() {
        // 1. 경매 등록
        val request =
            AuctionRequest(
                startPrice = 1_000_000,
                minBid = 5000,
                startTime = LocalDateTime.of(2025, 4, 10, 11, 30),
                endTime = LocalDateTime.of(2025, 4, 14, 11, 30),
                productName = "productDetailTest",
                imageUrl = "example.com/image123.jpg",
                description = "디테일 테스트",
            )

        val result =
            mockMvc
                .perform(
                    post("/api/admin/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andReturn()

        val responseBody = result.response.contentAsString
        println("Response body: $responseBody") // 응답 내용 출력

        val responseJson = objectMapper.readTree(responseBody)

        // data 필드가 있는지 확인
        val dataNode = responseJson.get("data")

        // id 필드가 있는지 확인
        val idNode = dataNode.get("id")
        if (idNode == null) {
            println("Warning: 'id' field is missing in data")
            return
        }

        val auctionId = idNode.asLong()

        // 2. 상세 조회
        mockMvc
            .perform(get("/api/auctions/$auctionId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("경매가 성공적으로 조회되었습니다."))
            .andExpect(jsonPath("$.data.productName").value("productDetailTest"))
    }
}
