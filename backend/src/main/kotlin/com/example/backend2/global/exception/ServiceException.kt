package com.example.backend2.global.exception

import com.example.backend2.global.dto.RsData

class ServiceException(code: String, message: String) : RuntimeException(message) {
    private val rsData: RsData<*> = RsData(code, message, null)

    val code: String
        get() = rsData.code

    val msg: String
        get() = rsData.msg

    val statusCode: Int
        get() = rsData.getStatusCode() // 메서드명 수정
}