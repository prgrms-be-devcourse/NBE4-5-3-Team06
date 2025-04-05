package com.example.backend2.domain.auction.controller

import com.example.backend2.domain.auction.dto.AuctionCheckResponse
import com.example.backend2.domain.auction.dto.AuctionDetailResponse
import com.example.backend2.domain.auction.service.AuctionService
import com.example.backend2.global.dto.RsData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auctions")
class AuctionController(
    private val auctionService: AuctionService,
) {
    @GetMapping
    fun getAllAuctions(): ResponseEntity<RsData<List<AuctionCheckResponse>>> {
        val response = auctionService.getAllAuctions()
        val rsData = RsData("200", "전체 조회가 완료되었습니다.", response)
        return ResponseEntity.ok(rsData)
    }

    // Explain: FE 에서 FINISHED 상태로 변경시 요청할 엔드포인트 경로
    @PostMapping("/{auctionId}/close")
    fun closeAuction(
        @PathVariable auctionId: Long,
    ) {
        auctionService.closeAuction(auctionId)
    }

    // 특정 경매 상세 조회 컨트롤러
    @GetMapping("/{auctionId}")
    fun getAuctionDetail(
        @PathVariable auctionId: Long,
    ): ResponseEntity<RsData<AuctionDetailResponse>> {
        val response = auctionService.getAuctionDetail(auctionId)
        val rsData = RsData("200", "경매가 성공적으로 조회되었습니다.", response)
        return ResponseEntity.ok(rsData)
    }
} 
