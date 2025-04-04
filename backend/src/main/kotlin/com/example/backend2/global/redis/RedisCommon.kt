package com.example.backend2.global.redis

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class RedisCommon(
    private val template: RedisTemplate<String, String>,
    private val gson: Gson
) {
    private val timeUnit: Duration = Duration.ofSeconds(5)

    fun <T> getData(key: String, clazz: Class<T>): T? {
        val jsonValue = template.opsForValue().get(key) ?: return null
        return gson.fromJson(jsonValue, clazz)
    }

    fun getAllKeys(): Set<String>? = template.keys("*")

    fun <T> setData(key: String, value: T) {
        val jsonValue = gson.toJson(value)
        template.opsForValue().set(key, jsonValue)
        template.expire(key, timeUnit)
    }

    fun <T> multiSetData(datas: Map<String, T>) {
        val jsonMap = datas.mapValues { (_, value) -> gson.toJson(value) }
        template.opsForValue().multiSet(jsonMap)
    }

//    fun <T> addToSortedSet(key: String, value: T, score: Float) {
//        val jsonValue = gson.toJson(value)
//        template.opsForZSet().add(key, jsonValue, score)
//    }

    fun <T> rangeByScore(key: String, minScore: Float, maxScore: Float, clazz: Class<T>): Set<T> {
        val jsonValues = template.opsForZSet().rangeByScore(key, minScore.toDouble(), maxScore.toDouble())
        return jsonValues?.mapTo(HashSet()) { gson.fromJson(it, clazz) } ?: emptySet()
    }

    fun <T> getTopNFromSortedSet(key: String, n: Int, clazz: Class<T>): List<T> {
        val jsonValues = template.opsForZSet().reverseRange(key, 0, (n - 1).toLong())
        return jsonValues?.map { gson.fromJson(it, clazz) } ?: emptyList()
    }

    fun <T> addToListLeft(key: String, value: T) {
        template.opsForList().leftPush(key, gson.toJson(value))
    }

    fun <T> addToListRight(key: String, value: T) {
        template.opsForList().rightPush(key, gson.toJson(value))
    }

    fun <T> getAllList(key: String, clazz: Class<T>): List<T> {
        val jsonValues = template.opsForList().range(key, 0, -1)
        return jsonValues?.map { gson.fromJson(it, clazz) } ?: emptyList()
    }

    fun <T> removeFromList(key: String, value: T) {
        template.opsForList().remove(key, 1, gson.toJson(value))
    }

    fun <T> putInHash(key: String, field: String, value: T) {
        template.opsForHash<String, String>().put(key, field, gson.toJson(value))
    }

    fun <T> getFromHash(key: String, field: String, clazz: Class<T>): T? {
        val result = template.opsForHash<String, String>().get(key, field)
        return result?.let { gson.fromJson(it, clazz) }
    }

    fun <T> putAllInHash(key: String, entries: Map<String, T>) {
        val mappedEntries = entries.mapValues { (_, value) -> gson.toJson(value) }
        template.opsForHash<String, String>().putAll(key, mappedEntries)
    }

    fun <T> getHashAsObject(key: String, clazz: Class<T>): T? {
        val entries = template.opsForHash<String, String>().entries(key)
        return if (entries.isNullOrEmpty()) null else ObjectMapper().convertValue(entries, clazz)
    }

    fun <T> putObjectAsHash(key: String, obj: T) {
        val map = ObjectMapper().convertValue(obj, object : TypeReference<Map<String, Any>>() {})
        template.opsForHash<String, Any>().putAll(key, map)
    }

    fun removeFromHash(key: String, field: String) {
        template.opsForHash<String, String>().delete(key, field)
    }

    fun setExpireAt(key: String, expireTime: LocalDateTime) {
        val secondsUntilExpire = Duration.between(LocalDateTime.now(), expireTime).seconds
        if (secondsUntilExpire > 0) {
            template.expire(key, secondsUntilExpire, TimeUnit.SECONDS)
        }
    }

    fun getTTL(key: String): Long? = template.getExpire(key, TimeUnit.SECONDS)

    fun getRemainingTTL(key: String): Duration? {
        val seconds = template.getExpire(key, TimeUnit.SECONDS)
        return if (seconds == null || seconds < 0) null else Duration.ofSeconds(seconds)
    }

    fun getExpireTime(key: String): LocalDateTime? {
        val seconds = template.getExpire(key, TimeUnit.SECONDS)
        return if (seconds == null || seconds < 0) null else LocalDateTime.now().plusSeconds(seconds)
    }
}
