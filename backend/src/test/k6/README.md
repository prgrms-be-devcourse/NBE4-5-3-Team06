# 경매 시스템 부하 및 스트레스 테스트

이 디렉토리는 K6를 사용하여 경매 시스템의 부하 및 스트레스 테스트를 수행하는 스크립트를 포함하고 있습니다.

## 테스트 대상

1. **경매 목록 조회 API**: 다수 사용자가 동시에 경매 목록을 조회하는 시나리오
2. **입찰 시스템(WebSocket)**: 다수 사용자가 동시에 입찰하는 시나리오
3. **경매 종료 프로세스**: 다수의 경매가 동시에 종료되는 시나리오

## 사전 준비

### K6 설치

- **Windows**: [Chocolatey](https://chocolatey.org/)를 사용하여 설치
  ```
  choco install k6
  ```

- **macOS**: [Homebrew](https://brew.sh/)를 사용하여 설치
  ```
  brew install k6
  ```

- **Linux**: [공식 패키지](https://k6.io/docs/getting-started/installation/#linux)를 사용하여 설치
  ```
  sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
  echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update
  sudo apt-get install k6
  ```

### 테스트 실행 준비

1. 백엔드 서버가 실행 중인지 확인합니다.
2. 필요한 경우 각 테스트 스크립트의 `baseUrl` 변수를 실제 서버 URL로 수정합니다.
3. 실제 환경에서 테스트할 경우 토큰 발급 함수를 실제 로그인 API를 호출하도록 수정해야 합니다.

## 테스트 실행 방법

### 모든 테스트 실행 (윈도우)

```
.\run_all_tests.bat
```

### 모든 테스트 실행 (Linux/macOS)

```
chmod +x run_all_tests.sh
./run_all_tests.sh
```

### 개별 테스트 실행

```
k6 run auction_list_test.js    # 경매 목록 조회 API 테스트
k6 run bid_websocket_test.js   # 입찰 시스템 테스트
k6 run auction_close_test.js   # 경매 종료 프로세스 테스트
```

### 스트레스 테스트 실행

각 테스트 스크립트에는 `stressOptions`라는 변수가 있습니다. 이를 사용하여 스트레스 테스트를 실행할 수 있습니다:

```
k6 run auction_list_test.js -e K6_OPTIONS=stressOptions
```

## 테스트 결과 해석

테스트 결과는 콘솔에 출력되며, `--out` 옵션을 사용하여 JSON이나 CSV 파일로 저장할 수 있습니다:

```
k6 run auction_list_test.js --out json=results/auction_list_results.json
```

더 자세한 시각화를 위해 InfluxDB와 Grafana를 설정하여 실시간 모니터링을 할 수 있습니다:

```
k6 run auction_list_test.js --out influxdb=http://localhost:8086/k6
```

## 주요 지표

- **http_req_duration**: HTTP 요청 지속 시간 (ms)
- **failed_requests/failed_bids/failed_closes**: 실패한 요청 비율
- **ws_session_duration**: WebSocket 세션 지속 시간
- **iterations**: 완료된 반복 횟수

## 사용자 정의 지표

- **failed_requests**: 요청 실패율
- **successful_bids**: 성공적인 입찰 비율
- **failed_bids**: 입찰 실패율
- **failed_closes**: 경매 종료 실패율 