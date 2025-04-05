package com.example.backend2.domain.auction.controller

import com.example.backend2.domain.auction.dto.AuctionAdminResponse
import com.example.backend2.domain.auction.dto.AuctionCreateResponse
import com.example.backend2.domain.auction.dto.AuctionRequest
import com.example.backend2.domain.auction.service.AuctionService
import com.example.backend2.global.dto.RsData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/auctions")
class AdminAuctionController(
    private val auctionService: AuctionService,
) {
    @PostMapping
    fun createAuction(
        @RequestBody request: AuctionRequest,
    ): ResponseEntity<RsData<AuctionCreateResponse>> {
        val response = auctionService.createAuction(request)
        val rsData = RsData("200", "경매가 성공적으로 등록되었습니다.", response)
        return ResponseEntity.status(response.getStatusCode()).body(response)
    }

    @GetMapping
    fun getAllAuctions(): ResponseEntity<RsData<List<AuctionAdminResponse>>> {
        val response = auctionService.getAdminAllAuctions()
        val rsData = RsData("200", "전체 조회가 완료되었습니다.", response)
        return ResponseEntity.ok(rsData)
    }
}
