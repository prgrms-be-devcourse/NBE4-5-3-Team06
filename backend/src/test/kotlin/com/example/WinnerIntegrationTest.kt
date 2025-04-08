package com.example

import com.example.backend2.data.AuctionStatus
import com.example.backend2.data.Role
import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.auction.repository.AuctionRepository
import com.example.backend2.domain.product.entity.Product
import com.example.backend2.domain.product.repository.ProductRepository
import com.example.backend2.domain.user.entity.User
import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.domain.winner.entity.Winner
import com.example.backend2.domain.winner.repository.WinnerRepository
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WinnerIntegrationTest {
    @Autowired
    lateinit var auctionRepository: AuctionRepository

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var winnerRepository: WinnerRepository


    @BeforeEach
    fun setup() {
        // 테스트 전 데이터 초기화
        winnerRepository.deleteAll()
        auctionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("낙찰 내역 조회 테스트")
    fun t1() {
        val user =
            userRepository.save(
                User(
                    userUUID = "testuser-UUID",
                    email = "testuser@example.com",
                    password = "qwer1234!",
                    nickname = "test1",
                    role = Role.USER,
                ),
            )

        val product =
            productRepository.save(
                Product(
                    productName = "MacBook Pro",
                    imageUrl = "",
                    description = "애플 노트북",
                ),
            )
        val aution =
            auctionRepository.save(
                Auction(
                    product = product,
                    startPrice = 1000,
                    minBid = 100,
                    startTime = LocalDateTime.now(),
                    endTime = LocalDateTime.now(),
                    status = AuctionStatus.FINISHED,
                ),
            )
        winnerRepository.save(
            Winner(
                user = user,
                auction = aution,
                winningBid = 2000,
                winTime = LocalDateTime.now(),
            )
        )
        val url = "/api/winner/${user.userUUID}/winner"

        //MockMvc를 통해 GET 요청을 실제로 수행
        val result = mvc.perform(get(url))
            .andDo(print()) //.andDo(print())로 콘솔에 요청/응답

        result
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("낙찰 내역 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].nickname").value("test1"))
            .andExpect(jsonPath("$.data[0].winingBid").value(2000))


    }
}
