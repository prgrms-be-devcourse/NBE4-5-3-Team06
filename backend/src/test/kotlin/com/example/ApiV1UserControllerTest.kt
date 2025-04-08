package com.example

import com.example.backend2.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc


@SpringBootTest
@AutoConfigureMockMvc
class ApiV1UserControllerTest
@Autowired constructor(
    val mvc: MockMvc,
    val userRepository: UserRepository
){
    @BeforeEach
    fun setUP() {
        userRepository.deleteAll()
    }




}