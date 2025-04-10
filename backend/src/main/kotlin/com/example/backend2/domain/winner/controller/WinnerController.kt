package com.example.backend2.domain.winner.controller

import com.example.backend2.domain.winner.dto.WinnerCheckResponse
import com.example.backend2.domain.winner.service.WinnerService
import com.example.backend2.global.dto.RsData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auctions")
class WinnerController(
    private val winnerService: WinnerService,
) {
    // 낙찰 내역 조회 컨트롤러
    @GetMapping("/{userUUID}/winner")
    fun getWinnerList(
        @PathVariable userUUID: String,
    ): ResponseEntity<RsData<List<WinnerCheckResponse>>> {
        val response = winnerService.getWinnerList(userUUID)
        return ResponseEntity.status(response.getStatusCode()).body(response)
    }
}
