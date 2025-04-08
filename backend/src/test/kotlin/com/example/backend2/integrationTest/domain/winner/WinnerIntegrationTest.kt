@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.integrationTest.domain.winner

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.domain.winner.controller.WinnerController
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WinnerIntegrationTest {
    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var winnerRepository: WinnerRepository

    @Test
    @DisplayName("낙찰 내역 조회 테스트") // 종료된 경매 -> 낙찰 내역 조회
    @WithMockUser(username = "testUser-UUID", roles = ["USER"])
    fun winnerList() {
        // 낙찰자 생성
        val user =
            userRepository.save(
                User(
                    userUUID = "testUser-UUID",
                    email = "testuser@example.com",
                    password = "password123",
                    nickname = "testUser",
                    role = Role.USER,
                ),
            )
        // 경매에 등록된 상품 생성
        val product =
            productRepository.save(
                Product(
                    productName = "MacBook Pro",
                    imageUrl = "",
                    description = "애플 노트북",
                ),
            )
        // 종료된 경매 생성
        val aution =
            auctionRepository.save(
                Auction(
                    product = product,
                    startPrice = 1000,
                    minBid = 100,
                    startTime = LocalDateTime.now().minusDays(2), // 경매 시작 하기 2일전, 지금보다 과거여야 경매가 이미 시작된것으로 처리되기때문
                    endTime = LocalDateTime.now(), // 현재 시간 기준 1일전 경매 종료, 지금보다 과거여야 경매가 끝난 걸로 간주되기 때문
                    status = AuctionStatus.FINISHED, // 경매가 종료된 상태
                ),
            )
        // DB에 종료된 경매 저장
        winnerRepository.save(
            Winner(
                user = user,
                auction = aution,
                winningBid = 2000,
                winTime = LocalDateTime.now().minusDays(1),
                // 낙찰이 종료 후에 이루어졌다는 걸 시간으로 보여주기 위해 wintime도 현재보다 하루 전으로 설정함
            ),
        )

        val url = "/api/auctions/${user.userUUID}/winner"

        // MockMvc를 통해 GET 요청을 실제로 수행
        val result =
            mvc
                .perform(get(url))
                .andDo(print()) // .andDo(print())로 콘솔에 요청/응답

        result
            .andExpect(handler().handlerType(WinnerController::class.java))
            .andExpect(handler().methodName("getWinnerList")) // WinnerController의 getWinnerList 메서드가 호출되었는지 확인
            .andExpect(status().isOk) // HTTP 상태 코드가 200 인지 확인
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("200")) // 기대값(200)과 같은지 확인
            .andExpect(jsonPath("$.msg").value("낙찰 내역 조회가 완료되었습니다.")) // 기대값("낙찰 내역 조회가 완료되었습니다)와 같은지 확인
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].productName").value("MacBook Pro")) // 상품명 확인
            .andExpect(jsonPath("$.data[0].description").value("애플 노트북")) // 상품 설명 확인
            .andExpect(jsonPath("$.data[0].winningBid").value(2000)) // 낙찰가 확인
            .andExpect(jsonPath("$.data[0].imageUrl").value("")) // 이미지 URL 확인
    }
}
