package com.example.backend2.domain.winner.service

import com.example.backend2.domain.winner.dto.WinnerCheckResponse
import com.example.backend2.domain.winner.repository.WinnerRepository
import com.example.backend2.global.dto.RsData
import com.example.backend2.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WinnerService(
    private val winnerRepository: WinnerRepository,
) {
    // 사용자의 낙찰 내역 조회
    @Transactional(readOnly = true)
    fun getWinnerList(userUUID: String): RsData<List<WinnerCheckResponse>> {
        val winners = winnerRepository.findByUserUserUUID(userUUID)

        // 낙찰자가 존재하지 않을 경우 예외 처리
        if (winners.isEmpty()) {
            throw ServiceException("404", "낙찰자가 존재하지 않습니다.")
        }

        // 낙찰자 목록을 WinnerCheckResponse 로 변환
        val response = winners.map { WinnerCheckResponse.from(it) }

        return RsData("200", "낙찰 내역 조회가 완료되었습니다.", response)
    }
}
