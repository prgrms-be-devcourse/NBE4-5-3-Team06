@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.backend2.domain.user.controller

import com.example.backend2.domain.user.dto.*
import com.example.backend2.domain.user.service.EmailService
import com.example.backend2.domain.user.service.JwtBlacklistService
import com.example.backend2.domain.user.service.UserService
import com.example.backend2.global.dto.RsData
import com.example.backend2.global.utils.JwtProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Suppress("ktlint:standard:no-consecutive-comments")
@RestController
@RequestMapping("/api/auth")
class UserController(
    private val userService: UserService,
    private val blacklistService: JwtBlacklistService,
    private val emailService: EmailService,
    private val jwtProvider: JwtProvider,
) {
    private val log = KotlinLogging.logger {}

    // 회원가입
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: UserSignUpRequest,
    ): ResponseEntity<RsData<UserSignUpResponse>> {
        val response = userService.signup(request)

        // 회원가입 성공 시 응답 데이터 생성 (201: Created)
        val rsData = RsData("201", "회원가입이 완료되었습니다.", response)

        return ResponseEntity.status(HttpStatus.CREATED).body(rsData)
    }

    // Explain: 회원가입 컨트롤러를 더 간단하게 처리 => 임시 기록
    /*@PostMapping("/signup")
    fun signup(
        @RequestBody request: UserSignUpRequest,
    ): ResponseEntity<RsData<UserSignUpResponse>> =
        userService
            .signup(request)
            .toResponseEntity(
                status = HttpStatus.CREATED,
                message = "회원가입이 완료되었습니다.",
            )*/

    @PostMapping("/login")
    fun signIn(
        @RequestBody request: UserSignInRequest,
    ): ResponseEntity<RsData<UserSignInResponse>> {
        // 로그인 서비스 호출
        val response = userService.login(request)

        // 성공 응답 생성
        val rsData = RsData("200", "로그인이 완료되었습니다.", response)

        // HTTP 200 OK 응답 반환
        return ResponseEntity.ok(rsData)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Map<String, String>> {
        val cleanToken = token.replace("Bearer ", "") // "Bearer " 제거

        blacklistService.addToBlacklist(cleanToken)
        return ResponseEntity.ok(mapOf("message" to "로그아웃이 완료되었습니다."))
    }

    @GetMapping("/users/{userUUID}") // 특정 사용자 조회
    fun getUser(
        @PathVariable("userUUID") userUUID: String,
    ): ResponseEntity<RsData<UserCheckRequest>> {
        val userCheck = userService.getUserCheck(userUUID)
        val rsData = RsData("200", "사용자 조회가 완료되었습니다.", userCheck)
        return ResponseEntity.ok(rsData)
    }

    @PutMapping("/users/{userUUID}")
    fun putUser(
        @PathVariable("userUUID") userUUID: String,
        @RequestBody request: UserPutRequest,
    ): ResponseEntity<RsData<UserPutRequest>> {
        val userPut = userService.updateUser(userUUID, request)
        val rsData = RsData("200", "사용자 정보 수정이 완료되었습니다.", userPut)
        return ResponseEntity.ok(rsData)
    }

    @PostMapping("/send-code")
    fun sendVerificationCode(
        @RequestBody request: EmailSendRequest,
    ): ResponseEntity<RsData<Unit>> {
        log.error { "Request to send verification code failed: $request" }
        emailService.sendVerificationCode(request.email)
        val rsData = RsData<Unit>("200", "인증코드가 전송되었습니다.")
        return ResponseEntity.ok(rsData)
    }

    @PostMapping("/verify")
    fun verify(
        @RequestBody request: EmailVerificationRequest,
    ): ResponseEntity<RsData<Unit>> =
        when (emailService.verifyCode(request.email, request.code)) {
            true -> ResponseEntity.ok(RsData("200", "이메일 인증이 처리되었습니다."))
            false ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(RsData("400", "인증코드가 일치하지 않습니다."))
        }

    // 테스트 계정만 삭제하는 API (example.com 도메인 계정)
    @DeleteMapping("/test-accounts")
    fun deleteTestAccounts(): ResponseEntity<RsData<Map<String, Int>>> {
        log.info { "테스트 계정 삭제 요청" }
        val count = userService.deleteTestAccounts()
        val rsData = RsData("200", "테스트 계정이 삭제되었습니다.", mapOf("deletedCount" to count))
        return ResponseEntity.ok(rsData)
    }
//
//    @GetMapping("/users/email")
//    fun getUserByEmail(
//        @RequestParam email: String,
//        @RequestHeader("Authorization") token: String
//    ): ResponseEntity<RsData<UserCheckRequest>> {
//        val tokenValue = token.removePrefix("Bearer ")
//        val tokenEmail = jwtProvider.getUsername(tokenValue)
//
//        if (tokenEmail != email) {
//            throw ServiceException("403", "다른 사용자의 정보를 조회할 수 없습니다.")
//        }
//
//        val user = userService.getUserByEmail(email)
//        val rsData = RsData("200", "사용자 조회 성공", UserCheckRequest.from(user))
//        return ResponseEntity.ok(rsData)
//    }


    @PostMapping("/token")
    fun issueJwt(@RequestBody request: JwtRequest): ResponseEntity<RsData<Map<String, String?>>> {
        println("요청 받은 이메일: ${request.email}")

        // 사용자 조회 시 없으면 자동 등록
        val user = userService.findOrCreateGoogleUser(request.email)

        val claims = mapOf(
            "userUUID" to user.userUUID,
            "nickname" to user.nickname,
            "role" to "ROLE_${user.role}",
            "sub" to user.email
        )

        val token = jwtProvider.generateToken(claims, user.email)
        return ResponseEntity.ok(
            RsData("200", "토큰 발급 성공", mapOf(
                "token" to token,
                "email" to user.email,
                "userUUID" to user.userUUID,
                "nickname" to user.nickname,
                "profileImage" to user.profileImage // null일 수도 있으니 프론트에서 처리
            ))
        )
    }


    data class JwtRequest(val email: String)
}
