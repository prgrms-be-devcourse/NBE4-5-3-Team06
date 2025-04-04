package com.example.backend2.global.app

import com.example.backend2.global.filter.JwtAuthenticationFilter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

@org.springframework.context.annotation.Configuration
@EnableWebSecurity
class SecurityConfig {
    // 비밀번호 인코더
    @org.springframework.context.annotation.Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @org.springframework.context.annotation.Bean
    @Throws(java.lang.Exception::class)
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter?
    ): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) } // CORS 설정 (필요 시 disable 또는 설정)
            .csrf { csrf -> csrf.disable() } // CSRF 설정
            .headers { headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) } // H2 콘솔을 위한 iframe 허용
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // Preflight 요청 허용
                    .requestMatchers(
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/send-code",
                        "/api/auth/vertify",
                        "/api/auth/logout"
                    ).permitAll()
                    .requestMatchers("/api/auth/users/**").authenticated()
                    .requestMatchers("/api/auctions/{auctionId}", "/api/auctions").permitAll()
                    .requestMatchers("/api/auctions/{userUUID}/winner").authenticated()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @org.springframework.context.annotation.Bean
    fun corsConfigurationSource(): org.springframework.web.cors.CorsConfigurationSource {
        val configuration: CorsConfiguration = CorsConfiguration()
        configuration.addAllowedOrigin("http://localhost:3000")
        configuration.addAllowedOrigin("http://35.203.149.35:3000")
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("*")
        configuration.setAllowCredentials(true)

        val source = org.springframework.web.cors.UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}