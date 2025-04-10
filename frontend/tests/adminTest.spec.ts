import { test, expect } from "@playwright/test";

test("관리자 경매 등록 플로우", async ({ page }) => {
  const adminEmail = "admin@example.com";
  const adminPassword = "adminpassword";

  // 1. 로그인
  await page.goto("/auth/login");
  await page.getByPlaceholder("이메일").fill(adminEmail);
  await page.getByPlaceholder("비밀번호").fill(adminPassword);

  await Promise.all([
    page.waitForEvent("dialog").then(async (dialog) => {
      expect(dialog.message()).toContain("로그인 성공!");
      await dialog.accept();
    }),

    page.locator('button[type="submit"]', { hasText: "로그인" }).click(),
    page.waitForNavigation({ waitUntil: "networkidle" }),
  ]);

  await page.waitForURL("**/admin/auctions/list", { timeout: 5000 });

  // 2. 경매 등록 페이지 이동 (버튼 클릭)
  await page.getByRole("button", { name: "경매 생성하기" }).click();
  await expect(page).toHaveURL("/admin/auctions"); // << -여기

  // 3. 폼 작성
  const title = `Playwright 테스트 경매 ${Date.now()}`;
  const description = "자동화 테스트용 경매 설명입니다.";
  const startPrice = "10000";
  const minBid = "5000";
  const imageUrl =
    "https://image.idus.com/image/files/dd57353cba0f4d978394c62de6d7fe26.jpg";
  const now = Date.now();
  const startAt = new Date(now - 10 * 1000) // 10초 전
    .toISOString()
    .slice(0, 16);
  const endAt = new Date(now + 1 * 60 * 1000) // 1분 후
    .toISOString()
    .slice(0, 16);

  await page.getByPlaceholder("상품명 입력").fill(title);
  await page.getByPlaceholder("시작 가격 입력").fill(startPrice);
  await page.getByPlaceholder("최소 입찰가 입력").fill(minBid);
  await page.locator('input[type="datetime-local"]').nth(0).fill(startAt);
  await page.locator('input[type="datetime-local"]').nth(1).fill(endAt);
  await page.getByPlaceholder("이미지 URL 입력").fill(imageUrl);
  await page.getByPlaceholder("상품 설명 입력").fill(description);

  // 4. 등록 버튼 클릭 (alert 확인)
  page.once("dialog", async (dialog) => {
    expect(dialog.message()).toContain("경매가 성공적으로 등록되었습니다!");
    await dialog.accept();
  });

  await page.getByRole("button", { name: "경매 등록하기" }).click();

  //   await page.waitForTimeout(1000);

  // 5. 경매 등록 시 '/admin/auctions/list'으로 리다이렉트
  await page.waitForURL("/admin/auctions/list", { timeout: 5000 });

  // 6. 등록된 항목 확인
  await page.waitForTimeout(500);

  await expect(page.getByText(title)).toBeVisible(); // << -여기

  console.log("[ADMIN] 경매 등록 완료");
});
