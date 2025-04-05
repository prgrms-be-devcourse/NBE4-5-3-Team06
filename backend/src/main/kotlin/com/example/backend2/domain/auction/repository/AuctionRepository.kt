package com.example.backend2.domain.auction.repository

import com.example.backend2.domain.auction.entity.Auction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AuctionRepository : JpaRepository<Auction, Long> {

    // 사용자 - 전체 경매 상품 리스트 조회하는 쿼리
    @Query("SELECT a FROM Auction a JOIN FETCH a.product")
    fun findAllAuctions(): List<Auction>

    // 관리자 - 전체 경매 상품 리스트 조회하는 쿼리
    @Query("SELECT a FROM Auction a JOIN FETCH a.product LEFT JOIN FETCH a.winner")
    fun findAllAuctionsWithProductAndWinner(): List<Auction>

    fun findByAuctionId(auctionId: Long): Auction?
} 