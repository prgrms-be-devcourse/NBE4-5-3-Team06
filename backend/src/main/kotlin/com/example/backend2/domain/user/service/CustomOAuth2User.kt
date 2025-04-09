package com.example.backend2.domain.user.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    private val oAuth2User: OAuth2User,
    val token: String, // JWT 토큰
) : OAuth2User {
    override fun getAuthorities(): Collection<GrantedAuthority> = oAuth2User.authorities

    override fun getAttributes(): Map<String, Any> = oAuth2User.attributes

    override fun getName(): String = oAuth2User.name
}
