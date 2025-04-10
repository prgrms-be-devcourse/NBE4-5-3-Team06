package com.example.backend2.integrationTest.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@TestConfiguration
@ActiveProfiles("test")
class IntegrationTestConfig {
    @Bean
    @Primary
    fun mockMvc(webApplicationContext: WebApplicationContext): MockMvc =
        MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper()
}