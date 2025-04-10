package com.example.backend2.global.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RsData<T> (
    val code: String,
    val msg: String,
    val data: T? = null
) {

    @JsonIgnore
    fun getStatusCode(): Int {
        return try {
            val statusCodeStr = code.split("-")[0]
            statusCodeStr.toInt()
        } catch (e: Exception) {
            500 // 기본 에러 코드
        }
    }
}