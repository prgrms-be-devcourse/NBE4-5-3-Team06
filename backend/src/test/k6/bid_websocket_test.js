/**
 * WebSocket 입찰 시스템 테스트
 * 다중 사용자가 여러개의 경매에서 입찰하는 상황에 대한 부하 테스트
 * (하나의 경매에 다중 사용자의 입찰에 대한 부하 테스트는 아니다.)
 */

import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import ws from 'k6/ws';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import http from 'k6/http';

// 메트릭 설정
const failureRate = new Rate('failed_bids');
const successRate = new Rate('successful_bids');

// 부하 테스트 설정
export const options = {
  stages: [
    { duration: '30s', target: 30 },   // 30초 동안 점진적으로 30명 증가
    { duration: '1m', target: 50 },    // 1분 동안 점진적으로 50명으로 증가
    { duration: '1m', target: 100 },   // 1분 동안 점진적으로 100명으로 증가
    { duration: '1m', target: 100 },   // 1분 동안 100명 유지 (부하 테스트)
    { duration: '30s', target: 0 },    // 30초 동안 점진적으로 0명으로 감소
  ],
  thresholds: {
    failed_bids: ['rate<0.1'],        // 실패율 10% 미만
    successful_bids: ['rate>0.9'],    // 성공률 90% 이상
  },
};

// 스트레스 테스트 설정 (필요시 options를 이것으로 교체)
export const stressOptions = {
  stages: [
    { duration: '1m', target: 200 },   // 1분 동안 점진적으로 200명 증가
    { duration: '1m', target: 500 },   // 1분 동안 점진적으로 500명으로 증가
    { duration: '30s', target: 0 },    // 30초 동안 점진적으로 0명으로 감소
  ],
  thresholds: {
    failed_bids: ['rate<0.2'],        // 실패율 20% 미만
    successful_bids: ['rate>0.8'],    // 성공률 80% 이상
  },
};

// 스트레스 테스트 설정 사용
// export const options = stressOptions;

// 실제 토큰 발급 함수 - 로그인 API 호출
function getToken() {
  const baseUrl = 'http://localhost:8080';
  
  // 테스트용 계정 정보 배열
  const testAccounts = [
    {
      email: 'jounghyeondaum@gmail.com',
      password: '123123123'
    },
    {
      email: 'jounghyeon123@gmail.com',
      password: '123123123'
    }
    /*
    사용자 계정 수를 임의로 늘려야 합니다.
     */
  ];
  
  // VU ID에 따라 계정 선택 (짝수/홀수 VU ID로 구분)
  const accountIndex = __VU % testAccounts.length;
  const selectedAccount = testAccounts[accountIndex];
  
  console.log(`VU ${__VU}: 계정 ${selectedAccount.email} 사용`);
  
  // 테스트용 계정 정보
  const loginPayload = JSON.stringify({
    email: selectedAccount.email,
    password: selectedAccount.password
  });
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  // 올바른 로그인 API 엔드포인트 사용
  const loginResponse = http.post(`${baseUrl}/api/auth/login`, loginPayload, params);
  
  if (loginResponse.status === 200) {
    try {
      const body = JSON.parse(loginResponse.body);
      console.log('로그인 응답:', JSON.stringify(body).substring(0, 200));
      
      if (body.data && body.data.token) {
        console.log(`VU ${__VU}: 로그인 성공: 토큰 발급 완료`);
        return body.data.token;
      } else {
        console.error('토큰이 응답에 없음:', body);
      }
    } catch (e) {
      console.error('로그인 응답 파싱 실패:', e, loginResponse.body);
    }
  } else {
    console.error('로그인 실패:', loginResponse.status, loginResponse.body);
  }
  
  console.warn(`VU ${__VU}: 더미 토큰 사용 (실제 인증 불가)`);
  return "dummy_token_" + uuidv4();
}

// 기본 더미 경매 ID 목록 (초기화 단계에 HTTP 요청을 하지 않도록)
const dummyAuctionIds = [1, 2, 3];

// 경매 ID 가져오기 (실제 환경에서는 활성화된 경매 API를 호출해서 가져와야 함)
function getAuctionIds() {
  const baseUrl = 'http://localhost:8080';
  const response = http.get(`${baseUrl}/api/auctions`);
  
  if (response.status === 200) {
    try {
      const body = JSON.parse(response.body);
      // 전체 응답 내용 출력 (디버깅 용도)
      console.log('전체 API 응답:', response.body);
      
      // RsData 응답 구조 처리
      if (body.data && Array.isArray(body.data)) {
        // 모든 경매 정보 출력
        body.data.forEach(auction => {
          console.log(`경매 정보: ID=${auction.auctionId}, 상태=${auction.status}, 현재가=${auction.currentBid}`);
        });
        
        // 상태가 ONGOING인 경매만 필터링 및 관련 정보 반환
        const ongoingAuctions = body.data
          .filter(auction => auction.status === 'ONGOING')
          .map(auction => ({
            auctionId: auction.auctionId,
            currentBid: parseInt(auction.currentBid) || 300000 // 숫자로 변환, 변환 실패 시 300000 사용
          }));
          
        console.log('사용할 경매 목록:', JSON.stringify(ongoingAuctions));
        return ongoingAuctions.length > 0 ? ongoingAuctions : [{auctionId: 1, currentBid: 300000}];
      }
    } catch (e) {
      console.error('Failed to parse auction list:', e);
    }
  } else {
    console.error('API 요청 실패:', response.status, response.body);
  }
  
  console.log('더미 경매 ID 사용');
  return [{auctionId: 1, currentBid: 300000}]; // 가장 최근 로그의 현재 입찰가로 설정
}

// 특정 경매의 최신 입찰가 조회
function getCurrentBidAmount(auctionId) {
  const baseUrl = 'http://localhost:8080';
  const response = http.get(`${baseUrl}/api/auctions/${auctionId}`);
  
  if (response.status === 200) {
    try {
      const body = JSON.parse(response.body);
      if (body.data && body.data.currentBid) {
        const currentBid = parseInt(body.data.currentBid);
        console.log(`경매 ID ${auctionId}의 최신 입찰가: ${currentBid}원`);
        return currentBid;
      }
    } catch (e) {
      console.error(`경매 ID ${auctionId}의 최신 입찰가 조회 실패:`, e);
    }
  } else {
    console.error(`경매 ID ${auctionId} 조회 실패:`, response.status, response.body);
  }
  
  // 기본값 반환 (API 호출 실패 시)
  return 300000;
}

export default function () {
  const baseUrl = 'ws://localhost:8080';
  
  // VU 컨텍스트에서 경매 정보 조회
  let auctions;
  try {
    auctions = getAuctionIds();
  } catch (e) {
    console.error('Error fetching auction IDs, using dummy data:', e);
    auctions = [{auctionId: 1, currentBid: 300000}]; // 기본값 설정
  }
  
  // 경매 정보가 없거나 비어있으면 더미 데이터 사용
  if (!auctions || auctions.length === 0) {
    console.log("Empty auction IDs, using dummy data");
    auctions = [{auctionId: 1, currentBid: 300000}];
  }
  
  // 테스트할 경매 무작위 선택
  const auction = auctions[Math.floor(Math.random() * auctions.length)];
  const auctionId = auction.auctionId;
  const token = getToken();
  const nickname = `user_${__VU}_${__ITER}`;
  
  console.log(`입찰할 경매 ID: ${auctionId}, 초기 입찰가: ${auction.currentBid}`);
  
  // 입찰 직전에 해당 경매의 최신 입찰가 조회
  const latestBidAmount = getCurrentBidAmount(auctionId);
  
  // 현재 입찰가 + 작은 증가액 설정 (5,000 ~ 15,000원 사이 랜덤 증가)
  const bidIncrease = 5000 + Math.floor(Math.random() * 10000);
  const bidAmount = latestBidAmount + bidIncrease;
  
  console.log(`입찰 금액 설정: ${bidAmount}원 (현재가 ${latestBidAmount} + ${bidIncrease}원)`);
  
  // 토큰을 URL 파라미터로 전달 (Token Parameter 추가)
  const wsUrlWithToken = `${baseUrl}/ws/websocket?token=${encodeURIComponent(token)}`;
  
  const res = ws.connect(wsUrlWithToken, {}, function (socket) {
    socket.on('open', () => {
      console.log(`VU ${__VU}: connected to WebSocket`);
      
      // STOMP 프로토콜 연결 (Spring의 기본 WebSocket 설정과 맞춰야 함)
      socket.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000');
      
      // 경매 subscribe
      socket.send(`SUBSCRIBE\nid:sub-${auctionId}\ndestination:/sub/auction/${auctionId}\n\n\u0000`);
      
      // 입찰 메시지 전송
      const bidMessage = JSON.stringify({
        auctionId: auctionId,
        token: token,
        amount: bidAmount,
        nickname: nickname
      });
      
      socket.send(`SEND\ndestination:/app/auction/bid\ncontent-length:${bidMessage.length}\n\n${bidMessage}\u0000`);
      
      // 응답 대기
      socket.setTimeout(function() {
        socket.close();
      }, 5000); // 5초 후 연결 종료
    });
    
    // 메시지 수신
    socket.on('message', (data) => {
      console.log(`VU ${__VU}: received message: ${data}`);
      try {
        // STOMP 프레임 파싱 (실제 응답 구조에 맞게 수정 필요)
        if (data.toString().includes('입찰 성공')) {
          successRate.add(1);
        } else {
          failureRate.add(1);
        }
      } catch (e) {
        console.error(`VU ${__VU}: Error parsing message: ${e.message}`);
        failureRate.add(1);
      }
    });
    
    // 에러 처리
    socket.on('error', (e) => {
      console.error(`VU ${__VU}: WebSocket error: ${e}`);
      failureRate.add(1);
    });
    
    socket.on('close', () => {
      console.log(`VU ${__VU}: disconnected from WebSocket`);
    });
  });
  
  check(res, { 'WebSocket connected successfully': (r) => r && r.status === 101 });
  
  // 다음 가상 사용자 실행 전 대기
  sleep(Math.random() * 2 + 1); // 1-3초 무작위 대기
} 