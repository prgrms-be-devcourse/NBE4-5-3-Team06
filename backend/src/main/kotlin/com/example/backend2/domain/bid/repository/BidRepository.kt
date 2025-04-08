package com.example.backend2.domain.bid.repository

import com.example.backend2.domain.auction.entity.Auction
import com.example.backend2.domain.bid.entity.Bid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BidRepository : JpaRepository<Bid, Long> {
    fun findAllByAuctionOrderByBidTimeDesc(auction: Auction): List<Bid>
}
