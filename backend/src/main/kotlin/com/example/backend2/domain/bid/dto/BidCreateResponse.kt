package com.example.backend2.domain.bid.dto

import com.example.backend2.domain.bid.entity.Bid
import java.time.LocalDateTime

data class BidCreateResponse(
    val auctionId: Long,
    val userUUID: String,
    val title: String,
    val bidAmount: Int,
    val bidTime: LocalDateTime,
    val nickname: String,
) {
    companion object {
        fun from(bid: Bid): BidCreateResponse {
            val auction = bid.auction
            val product = auction?.product
            val user = bid.user

            val base =
                BidCreateResponse(
                    auctionId = auction?.auctionId ?: -1L, // auction이 null이면 -1L로 기본값 설정
                    userUUID = user?.userUUID ?: "unknown-uuid", // user가 null이거나 UUID가 nul이면 기본 문자열로 대체
                    // product 혹은 productName이 null이면 "제목 없음"으로 대체
                    title = product?.productName ?: "제목 없음",
                    bidAmount = bid.amount ?: 0, // 입찰 금액이 null이면 0으로 대체
                    bidTime = bid.bidTime ?: LocalDateTime.now(), // 입찰 시간이 null이면 현재 시간으로 대체
                    nickname = user?.nickname ?: "익명", // 닉네임이null이면 "익명"으로 대체
                )

            return base
        }
    }
}
