package com.example.backend2.global.exception

import com.example.backend2.global.dto.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException // 추가된 임포트
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionAdvisor {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void>> {
        val message = e.bindingResult.fieldErrors
            .map { it.defaultMessage ?: "" }
            .sorted()
            .joinToString("\n")
            .ifEmpty { "비어있습니다." } // 빈 경우 기본 메시지 추가

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(RsData("400-1", message))
    }

    @ExceptionHandler(ServiceException::class)
    fun serviceExceptionHandle(ex: ServiceException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
            .status(ex.statusCode)
            .body(RsData(ex.code, ex.msg))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        val response = mapOf("error" to (ex.message ?: "잘못된 인자(argument)"))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        val response = mapOf("error" to (ex.message ?: "잘못된 상태"))
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
}