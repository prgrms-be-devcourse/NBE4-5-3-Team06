package com.example.backend2.global.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import com.fasterxml.jackson.databind.ObjectMapper

@TestConfiguration
@ActiveProfiles("test")
class IntegrationTestConfig {
    
    @Bean
    @Primary
    fun mockMvc(webApplicationContext: WebApplicationContext): MockMvc {
        return MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }
    
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }
} 