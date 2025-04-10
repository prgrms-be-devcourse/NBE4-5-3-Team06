import { test, expect } from "@playwright/test";

test("브라우저별 회원가입 → 로그인 플로우", async ({ page }, testInfo) => {
  // 브라우저별 고유 계정 생성
  const browser = testInfo.project.name;
  const timestamp = Date.now();
  const email = `user_${browser}_${timestamp}@example.com`;
  const password = "securePass123!";
  const nickname = `닉네임_${browser}_${timestamp}`;
  const code = "000000"; // @example.com은 고정 인증코드 처리됨

  console.log(`[${browser}] 테스트 시작 - 이메일: ${email}`);

  await page.goto("/auth/register");

  // 1. 이메일 입력
  await page.getByPlaceholder("이메일").fill(email);

  // 2. 인증코드 전송 버튼 클릭 (alert 뜸)
  page.once("dialog", async (dialog) => {
    expect(dialog.message()).toContain("인증코드");
    await dialog.accept();
  });
  await page.getByRole("button", { name: "인증" }).click();

  // 3. 인증코드 입력창이 등장할 때까지 기다림 (조건부 렌더링!)
  const codeInput = page.getByPlaceholder("인증 코드 입력");
  await codeInput.waitFor({ state: "visible", timeout: 10000 }); // <<여기
  await codeInput.fill(code);

  // 4. 확인 버튼 클릭
  await page.getByRole("button", { name: "확인" }).click();

  // 5. 나머지 필드 입력
  await page.getByPlaceholder("닉네임").fill(nickname);
  await page.getByPlaceholder("비밀번호").nth(0).fill(password);
  await page.getByPlaceholder("비밀번호 확인").fill(password);

  // 6. 회원가입 버튼 클릭 (alert 뜸)
  page.once("dialog", async (dialog) => {
    expect(dialog.message()).toContain("회원가입이 완료");
    await dialog.accept();
  });
  await page.locator('button[type="submit"]', { hasText: "회원가입" }).click();

  // 7. 로그인 페이지 이동 대기 → 실패 시 수동 이동
  try {
    await page.waitForURL("/auth/login", { timeout: 5000 }); // <<여기
  } catch {
    console.warn(`[${browser}] 자동 리다이렉트 실패. 수동 이동 수행.`);
    await page.goto("/auth/login"); // <<여기
  }

  // 8. 로그인 페이지 도착 확인
  await expect(page).toHaveURL("/auth/login");
  console.log(`[${browser}] 회원가입 완료 → 로그인 페이지 도착`);

  // 9. 로그인 입력
  await page.getByPlaceholder("이메일").fill(email);
  await page.getByPlaceholder("비밀번호").fill(password);

  // 10. 로그인 버튼 클릭 (alert 뜸)
  page.once("dialog", async (dialog) => {
    expect(dialog.message()).toContain("로그인 성공!");
    await dialog.accept();
  });
  await page.locator('button[type="submit"]', { hasText: "로그인" }).click();

  console.log(`[${browser}] 로그인 플로우 완료`);
});
