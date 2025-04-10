/**
 * 다중 사용자가 동시에 경매 목록 조회 API 테스트
 */

// k6에서 HTTP 요청을 만들기 위해 HTTP 모듈을 가져옴
import http from 'k6/http';

// 응답 검증과 지연 추가를 위해 k6에서 check와 sleep 유틸리티를 가져옴
import { check, sleep } from 'k6';

// 가상 사용자 간 공유 데이터를 처리하기 위해 k6/data에서 SharedArray를 가져옴 (이 스크립트에서는 사용되지 않음)
import { SharedArray } from 'k6/data';

// 실패율을 추적하기 위해 k6/metrics에서 Rate를 가져와 사용자 정의 메트릭을 정의
import { Rate } from 'k6/metrics';

// 'failed_requests'라는 사용자 정의 메트릭을 정의하여 실패한 HTTP 요청의 비율을 추적
const failureRate = new Rate('failed_requests');

// 부하 테스트 설정을 'options' 객체로 정의
export const options = {
  // 부하 테스트의 단계들을 정의하며, 각 단계의 지속 시간과 목표 가상 사용자(VU) 수를 지정
  stages: [
    // 30초 동안 가상 사용자를 50명까지 점진적으로 증가
    { duration: '30s', target: 50 },
    // 1분 동안 가상 사용자를 100명까지 점진적으로 증가
    { duration: '1m', target: 100 },
    // 30초 동안 가상 사용자를 200명까지 점진적으로 증가
    { duration: '30s', target: 200 },
    // 1분 동안 가상 사용자 200명을 유지 (지속 부하 단계)
    { duration: '1m', target: 200 },
    // 30초 동안 가상 사용자를 0명으로 점진적으로 감소
    { duration: '30s', target: 0 },
  ],
  // 테스트 성공/실패를 판단하기 위한 성능 임계값을 정의
  thresholds: {
    // HTTP 요청의 95%가 500밀리초 이내에 완료되어야 함
    http_req_duration: ['p(95)<500'],
    // 요청 실패율이 10% 미만이어야 함
    failed_requests: ['rate<0.1'],
  },
};

// 스트레스 테스트 설정을 정의 (필요 시 options 대신 사용 가능)
export const stressOptions = {
  // 시스템 한계를 시험하기 위해 더 많은 사용자를 대상으로 스트레스 테스트 단계를 정의
  stages: [
    // 1분 동안 가상 사용자를 500명까지 점진적으로 증가
    { duration: '1m', target: 500 },
    // 2분 동안 가상 사용자를 1000명까지 점진적으로 증가
    { duration: '2m', target: 1000 },
    // 1분 동안 가상 사용자를 2000명까지 점진적으로 증가
    { duration: '1m', target: 2000 },
    // 1분 동안 가상 사용자를 0명으로 점진적으로 감소
    { duration: '1m', target: 0 },
  ],
  // 스트레스 테스트를 위한 임계값 정의, 더 높은 지연 시간과 실패율 허용
  thresholds: {
    // HTTP 요청의 95%가 1000밀리초(1초) 이내에 완료되어야 함
    http_req_duration: ['p(95)<1000'],
    // 요청 실패율이 20% 미만이어야 함
    failed_requests: ['rate<0.2'],
  },
};

// 각 가상 사용자가 실행할 기본 함수를 정의
export default function () {
  // 테스트할 API의 기본 URL을 설정 (실제 서버 주소로 교체 필요)
  const baseUrl = 'http://localhost:8080';

  // 경매 목록을 조회하기 위해 HTTP GET 요청을 보냄
  const response = http.get(`${baseUrl}/api/auctions`);

  // 응답을 검증하기 위해 check 함수 사용
  const checkRes = check(response, {
    // HTTP 상태 코드가 200(성공)인지 확인
    'status is 200': (r) => r.status === 200,
    // 응답이 유효한 JSON인지 확인
    'response is valid JSON': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch (e) {
        console.log(`Invalid JSON: ${r.body}`);
        return false;
      }
    },
    // 응답에 필요한 필드가 있는지 확인 (실제 API 응답 구조에 맞게 조정)
    'response has valid structure': (r) => {
      const body = JSON.parse(r.body);
      // 여기서는 일반적인 성공 응답만 확인 (실제 응답 구조에 맞게 조정 필요)
      return body !== null && typeof body === 'object';
    },
  });

  // 검증이 실패하면 실패율 메트릭을 증가시킴
  if (!checkRes) {
    failureRate.add(1); // 사용자 정의 메트릭에 실패 기록
    // 디버깅을 위해 실패한 요청의 상태 코드와 본문을 로그로 출력
    console.log(`Failed request: ${response.status}, ${response.body}`);
  }

  // 사용자 행동을 시뮬레이션하기 위해 1~4초 사이의 무작위 대기 시간 추가
  sleep(Math.random() * 3 + 1);
}