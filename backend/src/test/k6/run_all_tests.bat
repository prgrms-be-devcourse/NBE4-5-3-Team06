@echo off
echo 경매 시스템 부하 테스트 시작 중...
echo.

echo 1. 경매 목록 조회 API 부하 테스트 실행
k6 run auction_list_test.js --out json=results/auction_list_load.json

echo.
echo 2. 입찰 시스템 (WebSocket) 부하 테스트 실행
k6 run bid_websocket_test.js --out json=results/bid_websocket_load.json

echo.
echo 3. 경매 종료 프로세스 부하 테스트 실행
k6 run auction_close_test.js --out json=results/auction_close_load.json

echo.
echo 모든 부하 테스트 완료!
echo 결과는 results 디렉토리에 저장됨

REM 스트레스 테스트를 실행하려면 아래 주석을 해제하고 실행하세요
REM echo.
REM echo === 스트레스 테스트 시작 ===
REM echo.
REM echo 1. 경매 목록 조회 API 스트레스 테스트 실행
REM k6 run auction_list_test.js -e K6_OPTIONS=stressOptions --out json=results/auction_list_stress.json
REM echo.
REM echo 2. 입찰 시스템 (WebSocket) 스트레스 테스트 실행
REM k6 run bid_websocket_test.js -e K6_OPTIONS=stressOptions --out json=results/bid_websocket_stress.json
REM echo.
REM echo 3. 경매 종료 프로세스 스트레스 테스트 실행
REM k6 run auction_close_test.js -e K6_OPTIONS=stressOptions --out json=results/auction_close_stress.json
REM echo.
REM echo 모든 스트레스 테스트 완료! 