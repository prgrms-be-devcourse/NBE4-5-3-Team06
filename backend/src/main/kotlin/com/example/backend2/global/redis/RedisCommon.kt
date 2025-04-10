package com.example.backend2.global.redis

import com.google.gson.Gson
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class RedisCommon(
    private val template: RedisTemplate<String, String>,
    private val gson: Gson,
) {
    private val timeUnit: Duration = Duration.ofSeconds(5)

    fun getAllKeys(): Set<String>? = template.keys("*")

    fun <T> putInHash(
        key: String,
        field: String,
        value: T,
    ) {
        template.opsForHash<String, String>().put(key, field, gson.toJson(value))
    }

    fun <T> getFromHash(
        key: String,
        field: String,
        clazz: Class<T>,
    ): T? {
        val result = template.opsForHash<String, String>().get(key, field)
        return result?.let { gson.fromJson(it, clazz) }
    }

    fun removeFromHash(
        key: String,
        field: String,
    ) {
        template.opsForHash<String, String>().delete(key, field)
    }

    fun setExpireAt(
        key: String,
        expireTime: LocalDateTime,
    ) {
        val secondsUntilExpire = Duration.between(LocalDateTime.now(), expireTime).seconds
        if (secondsUntilExpire > 0) {
            template.expire(key, secondsUntilExpire, TimeUnit.SECONDS)
        }
    }

    fun getTTL(key: String): Long? = template.getExpire(key, TimeUnit.SECONDS)

    // 동시성 제어를 위한 트랜잭션 처리 메서드
    fun <T> executeInTransaction(operation: () -> T): T =
        // EXPLAIN: 트랜잭션 내의 모든 명령은 원자적으로 실행되며, 중간에 실패하면 Discard 로 롤백된다.
        template.execute { connection ->
            try {
                connection.multi() // .multi()는 Redis 의 트랜잭션을 시작: 모든 명령어는 큐에 적재됨.
                val result = operation()
                connection.exec() // .exec()는 트랜잭션 내 명령 실행: 큐 내의 명령어를 실행
                result
            } catch (e: Exception) {
                connection.discard() // .discard()는 트랜잭션을 취소
                throw e
            }
        } ?: throw IllegalStateException("Redis transaction failed")

    /**값을 원자적으로 갱신하는 메서드:
     * 저장 전에 사용자 정의 조건(condition)을 확인.
     * 조건이 만족될 때만 값을 업데이트하고, 그렇지 않으면 업데이트를 건너뜁니다.
     * */
    fun <T> putInHashAtomically(
        key: String,
        field: String,
        value: T,
        condition: (T?) -> Boolean,
    ): Boolean =
        executeInTransaction {
            val currentValue = getFromHash(key, field, value!!::class.java)
            if (condition(currentValue)) {
                putInHash(key, field, value)
                true
            } else {
                false
            }
        }
}