package com.example.backend2.domain.user.service

import com.example.backend2.domain.user.repository.UserRepository
import com.example.backend2.global.exception.ServiceException
import com.example.backend2.global.redis.RedisCommon
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmailService(
    private val mainSender: JavaMailSender,
    private val redisCommon: RedisCommon,
    private val userRepository: UserRepository,
) {
    private val log = KotlinLogging.logger {}

    fun sendVerificationCode(email: String) {
        if (userRepository.findByEmail(email).isPresent) {
            log.warn { "Email already registered: $email" }
            throw ServiceException(HttpStatus.CONFLICT.value().toString(), "이미 존재하는 이메일입니다.")
        }

        val verificationCode = generateCode()
        val hashKey = getAuthHashKey(email)

        redisCommon.putInHash(hashKey, "code", verificationCode)
        redisCommon.setExpireAt(hashKey, LocalDateTime.now().plusSeconds(VERIFICATION_CODE_EXPIRATION.toLong()))

        // ✅ 비동기로 이메일 전송
        sendVerificationEmailAsync(email, verificationCode)
    }

    @Async
    fun sendVerificationEmailAsync(email: String, code: String) {
        try {
            val message = mainSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(email)
            helper.setSubject("Verification Code")
            helper.setText(buildVerificationEmailHtml(code), true)

            mainSender.send(message)
            log.info { "Verification code sent to $email" }
        } catch (e: Exception) {
            log.error(e) { "Failed to send verification email to $email" }
        }
    }

    fun verifyCode(email: String, code: String): Boolean {
        val hashKey = getAuthHashKey(email)
        val storedCode = redisCommon.getFromHash(hashKey, "code", String::class.java)

        if (storedCode == null) {
            log.warn { "Verification time expired for email: $email" }
            throw ServiceException("400", "인증시간이 만료되었습니다.")
        }

        return if (storedCode == code) {
            redisCommon.putInHash(hashKey, "verify", "true")
            redisCommon.setExpireAt(hashKey, LocalDateTime.now().plusSeconds(EMAIL_AUTH_EXPIRATION.toLong()))
            log.info { "Verification code matched for email: $email" }
            true
        } else {
            log.warn { "Verification code mismatch for email: $email" }
            false
        }
    }

    fun isVerified(email: String): Boolean {
        val hashKey = getAuthHashKey(email)
        val checkVerified = redisCommon.getFromHash(hashKey, "verify", String::class.java)
        return checkVerified == "true"
    }

    fun deleteVerificationCode(key: String) {
        val authHashKey = getAuthHashKey(key)
        redisCommon.setExpireAt(authHashKey, LocalDateTime.now().plusSeconds(10))
    }

    fun isVerificationExpired(email: String): Boolean {
        val hashKey = getAuthHashKey(email)
        val ttl = redisCommon.getTTL(hashKey)
        return ttl == null || ttl <= 0
    }

    private fun generateCode(): String = ((Math.random() * 900000).toInt() + 100000).toString()

    private fun buildVerificationEmailHtml(code: String): String = """
        <html>
        <body style='font-family: Arial, sans-serif; background-color: #F1F1F1; padding: 20px;'>
        <div style='max-width: 600px; margin: 0 auto; padding: 30px; background-color: #FFFFFF; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);'>
            <h2 style='color: #4CAF50; font-size: 24px; text-align: center;'>인증을 위한 이메일 인증번호</h2>
            <p style='font-size: 16px; color: #333;'>안녕하세요, <strong>회원님</strong>.</p>
            <p style='font-size: 16px; color: #555;'>요청하신 인증 번호는 아래와 같습니다:</p>
            <div style='text-align: center; padding: 20px; background-color: #F9F9F9; border-radius: 8px; margin: 20px 0;'>
                <h1 style='font-size: 36px; color: #4CAF50; font-weight: bold;'>$code</h1>
                <p style='font-size: 16px; color: #555;'>이 코드를 입력하여 이메일 인증을 완료하세요.</p>
            </div>
            <p style='font-size: 14px; color: #777;'>감사합니다!</p>
            <footer style='font-size: 12px; color: #aaa; text-align: center;'>
                <p>&copy; 2025 Your Company</p>
            </footer>
        </div>
        </body>
        </html>
    """.trimIndent()

    companion object {
        private const val VERIFICATION_CODE_EXPIRATION = 60 * 4 // 4분
        private const val EMAIL_AUTH_EXPIRATION = 60 * 10 // 인증 완료 후 10분간 유지

        private fun getAuthHashKey(email: String): String = "auth:$email"
    }
}
