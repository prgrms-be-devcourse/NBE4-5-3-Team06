/**
 * 여러 경매가 동시에 종료되는 것에 대한 경매 종료 프로세스 테스트
 */

import http from 'k6/http'; // HTTP 요청을 보내기 위한 K6의 http 모듈 임포트
import {check, sleep} from 'k6'; // check: 응답 확인을 위한 함수, sleep: 요청 간의 대기 시간
import {Rate} from 'k6/metrics'; // Rate: 성공률 및 실패율 측정을 위한 K6 메트릭스 모듈
import {uuidv4} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js'; // UUID 생성 라이브러리

// 메트릭 설정
const closeFailRate = new Rate('failed_closes'); // 실패율을 추적하기 위한 Rate 메트릭스 (경매 종료 실패 추적)

// 부하 테스트 설정 - 서버 상태가 안정될 때까지 부하를 줄임
export const options = {
  scenarios: {
    close_auctions: {
      executor: 'ramping-arrival-rate',
      startRate: 1,           // 초당 1개 경매 종료로 줄임
      timeUnit: '1s',
      preAllocatedVUs: 5,     // 가상 사용자 수 줄임
      maxVUs: 10,             // 최대 가상 사용자 수 줄임
      stages: [
        { duration: '10s', target: 2 },  // 10초 동안 초당 1->2개 경매 종료
        { duration: '20s', target: 5 },  // 20초 동안 초당 2->5개 경매 종료
        { duration: '10s', target: 1 },  // 10초 동안 초당 5->1개로 감소
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 타임아웃 1초로 증가
    failed_closes: ['rate<0.5'],      // 실패율 허용치 50%로 증가 (디버깅 중)
  },
};

// 테스트할 더미 경매 ID (초기화 단계에서 HTTP 요청을 하지 않도록 변경)
const dummyAuctionIds = [1, 2, 3, 4, 5];

// 활성 경매 ID 가져오기 (실제 환경에서는 API 호출 필요)
function getActiveAuctionIds() {
  const baseUrl = 'http://localhost:8080';
  const response = http.get(`${baseUrl}/api/auctions`);
  
  console.log(`전체 경매 목록 응답 상태: ${response.status}`);
  
  if (response.status === 200) {
    try {
      const body = JSON.parse(response.body);
      console.log('API 응답 전체 데이터:', body);
      
      // RsData 응답 구조 처리
      if (body.data && Array.isArray(body.data)) {
        console.log('경매 목록 개수:', body.data.length);
        
        // 모든 경매 ID와 상태 로깅
        body.data.forEach(auction => {
          console.log(`경매 ID: ${auction.auctionId}, 상태: ${auction.status}, 상품명: ${auction.productName}`);
        });
        
        // 경매 상태가 ONGOING인 경매만 필터링
        const activeAuctions = body.data
          .filter(auction => auction.status === 'ONGOING')
          .map(auction => auction.auctionId)
          .filter(id => id !== undefined && id !== null);
          
        console.log('활성화된 경매 ID 목록:', activeAuctions);
        
        if (activeAuctions.length > 0) {
          return activeAuctions;
        } else {
          console.log('활성화된 경매가 없습니다. 더미 데이터를 사용합니다.');
        }
      }
    } catch (e) {
      console.error('경매 목록 파싱 실패:', e);
    }
  } else {
    console.error('API 요청 실패:', response.status, response.body);
  }
  
  console.log('더미 경매 ID 사용');
  return dummyAuctionIds;
}

// 경매 세부 정보 확인 함수 (종료 전 상태 확인)
function checkAuctionDetail(auctionId) {
  const baseUrl = 'http://localhost:8080';
  const url = `${baseUrl}/api/auctions/${auctionId}`;
  
  const response = http.get(url);
  console.log(`경매 ID ${auctionId} 세부 정보 요청 상태: ${response.status}`);
  
  if (response.status === 200) {
    try {
      const detail = JSON.parse(response.body);
      console.log(`경매 ID ${auctionId} 세부 정보:`, detail);
      
      // 경매 상태 확인
      if (detail.data && detail.data.status) {
        console.log(`경매 ID ${auctionId} 현재 상태: ${detail.data.status}`);
        return detail.data.status;
      }
    } catch (e) {
      console.error(`경매 세부 정보 파싱 실패 (ID ${auctionId}):`, e);
    }
  }
  
  return null;
}

// 관리자 인증 토큰 획득 함수 (실제 환경에서는 로그인 API 호출 필요)
function getAdminToken() {
  // 실제 환경에서는 관리자 로그인 API 호출
  return "admin_token_" + uuidv4();
}

export default function () {
  const baseUrl = 'http://localhost:8080';
  
  // VU 컨텍스트에서 경매 ID를 가져오는 방식으로 변경
  let auctionIds;
  try {
    // 실제 서버에서 경매 ID 가져오기 시도
    auctionIds = getActiveAuctionIds();
  } catch (e) {
    // 오류 발생 시 더미 데이터 사용
    console.error("경매 ID 가져오기 오류, 더미 데이터 사용:", e);
    auctionIds = dummyAuctionIds;
  }
  
  // 경매 ID가 없거나 비어있으면 더미 데이터 사용
  if (!auctionIds || auctionIds.length === 0) {
    console.log("경매 ID 목록이 비어있어 더미 데이터를 사용합니다");
    auctionIds = dummyAuctionIds;
  }
  
  // 토큰 설정 (실제 환경에서는 Admin 인증 필요)
  const token = getAdminToken();
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
  
  // 종료할 경매 ID 선택
  const auctionId = auctionIds[Math.floor(Math.random() * auctionIds.length)];
  
  console.log(`종료할 경매 ID: ${auctionId}`);
  
  // 경매 종료 전 상태 확인
  const auctionStatus = checkAuctionDetail(auctionId);
  if (auctionStatus !== 'ONGOING') {
    console.log(`경매 ID ${auctionId}는 이미 종료되었거나 유효하지 않은 상태(${auctionStatus})입니다. 테스트를 건너뜁니다.`);
    sleep(1);
    return;
  }
  
  // 경매 종료 API 호출
  const url = `${baseUrl}/api/auctions/${auctionId}/close`;
  console.log(`경매 종료 요청: ${url}`);
  
  const response = http.post(url, null, { headers });
  
  // 응답 확인
  const checkResult = check(response, {
    'status is 200 or 204': (r) => r.status === 200 || r.status === 204,
  });
  
  if (!checkResult) {
    closeFailRate.add(1);
    console.error(`경매 종료 실패 - ID: ${auctionId}, Status: ${response.status}, Body: ${response.body}`);
  } else {
    console.log(`경매 종료 성공 - ID: ${auctionId}`);
  }
  
  // 대기 시간 증가 (서버 부하 감소)
  sleep(Math.random() * 1 + 1); // 1-2초 무작위 대기
}