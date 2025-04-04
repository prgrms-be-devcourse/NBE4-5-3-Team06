package com.example.backend2.global.filter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        // config.addAllowedOrigin("http://localhost:3000") // 로컬 주소 주석 처리
        config.addAllowedOrigin("http://35.203.149.35:3000")  // 배포 주소 추가
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        config.setAllowCredentials(true)

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}