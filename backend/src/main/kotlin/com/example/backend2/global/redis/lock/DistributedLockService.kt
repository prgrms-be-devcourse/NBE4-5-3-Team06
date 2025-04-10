package com.example.backend2.global.redis.lock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class DistributedLockService(
    private val redissonClient: RedissonClient,
) {
    fun withLock(
        lockKey: String,
        timeout: Long,
        unit: TimeUnit,
        operation: () -> Unit,
    ) {
        val lock: RLock = redissonClient.getLock(lockKey)
        try {
            if (lock.tryLock(timeout, unit)) {
                operation()
            } else {
                throw IllegalStateException("Lock acquisition failed")
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock()
            }
        }
    }

    fun <T> withLock(
        lockKey: String,
        timeout: Long,
        unit: TimeUnit,
        operation: () -> T,
    ): T {
        val lock: RLock = redissonClient.getLock(lockKey)
        try {
            if (lock.tryLock(timeout, unit)) {
                return operation()
            } else {
                throw IllegalStateException("Lock acquisition failed")
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock()
            }
        }
    }
} 
