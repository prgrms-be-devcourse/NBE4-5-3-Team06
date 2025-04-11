// tests/e2e/auction-flow.spec.ts
import { test, expect } from "@playwright/test";

let createdAuctionTitle = ""; // 사용자 입찰 시 사용할 제목

const adminEmail = "admin@example.com";
const adminPassword = "adminpassword";
const verificationCode = "000000";

const toKSTISOString = (date: Date) => {
    const offset = 9 * 60 * 60 * 1000;
    const kst = new Date(date.getTime() + offset);
    return kst.toISOString().slice(0, 16);
};

test.describe.serial("🧪 관리자 → 사용자 입찰 전체 플로우", () => {
    test("1. 관리자 로그인 후 경매 등록", async ({ page }) => {
        page.on("request", (request) => {
            console.log("요청) ", request.method(), request.url());
        });

        page.on("response", async (response) => {
            if (response.url().includes("/api/admin/auctions")) {
                try {
                    const data = await response.json();
                    console.log("경매등록 응답) ", data);
                } catch (e) {
                    console.log("응답 JSON 파싱 실패", e);
                }
            }
        });


        await page.goto("/auth/login");
        await page.getByPlaceholder("이메일").fill(adminEmail);
        await page.getByPlaceholder("비밀번호").fill(adminPassword);

        page.once("dialog", async (dialog) => await dialog.accept());
        await page.locator('button[type="submit"]', { hasText: "로그인" }).click();
        await expect(page).toHaveURL("/", { timeout: 5000 });

        await page.goto("/admin/auctions");

        const now = Date.now();
        const startTime = toKSTISOString(new Date(now - 10 * 1000));
        const endTime = toKSTISOString(new Date(now + 60 * 10000));

        createdAuctionTitle = `E2E 경매 ${now}`;

        await page.getByPlaceholder("상품명 입력").fill(createdAuctionTitle);
        await page.getByPlaceholder("시작 가격 입력").fill("10000");
        await page.getByPlaceholder("최소 입찰가 입력").fill("1000");
        await page.locator('input[type="datetime-local"]').nth(0).fill(startTime);
        await page.locator('input[type="datetime-local"]').nth(1).fill(endTime);
        await page.getByPlaceholder("이미지 URL 입력").fill("https://image.idus.com/image/files/dd57353cba0f4d978394c62de6d7fe26.jpg");
        await page.getByPlaceholder("상품 설명 입력").fill("E2E 테스트 상품");

        page.once("dialog", async (dialog) => {
            expect(dialog.message()).toContain("등록되었습니다");
            await dialog.accept();
        });
        await page.getByRole("button", { name: "경매 등록하기" }).click();

        await page.goto("/admin/auctions/list");
        await expect(page.getByText(createdAuctionTitle)).toBeVisible();
    });

    for (let i = 1; i <= 2; i++) {
        test(`2-${i}. 사용자${i} 회원가입 → 인증 → 로그인 → 입찰`, async ({ page }, testInfo) => {
            const browser = testInfo.project.name.replace(/\s/g, "_");
            const timestamp = Date.now();
            const email = `user_${browser}_${timestamp}@example.com`;
            const password = "securePass123!";
            const nickname = `u${i}_${browser}`;

            console.log(`[${browser}] 테스트 시작 - 이메일: ${email}`);

            // 요청 응답 로깅
            page.on("request", (request) => {
                console.log("요청) ", request.method(), request.url());
            });

            page.on("response", async (response) => {
                const url = response.url();
                if (["/signup", "/login", "/bid"].some((path) => url.includes(path))) {
                    try {
                        const json = await response.json();
                        console.log(`[${url.split("/api")[1]} 응답]`, json);
                    } catch (e) {
                        console.log("응답 JSON 파싱 실패", e);
                    }
                }
            });

            page.on("response", async (response) => {
                const url = response.url();
                if (url.includes("/send-code")) {
                    const status = response.status();
                    console.log(`[send-code 응답] status=${status}`);
                    if (!response.ok()) {
                        const body = await response.text();
                        console.log("send-code 실패 응답:", body);
                    }
                }
            });


            await page.goto("/auth/register");
            await page.getByPlaceholder("이메일").fill(email);
            page.once("dialog", async (dialog) => await dialog.accept());
            await page.getByRole("button", { name: "인증" }).click();

            const codeInput = page.getByPlaceholder("인증 코드 입력");
            // await expect(codeInput).toBeVisible({ timeout: 10000 });
            await codeInput.waitFor({ state: "visible", timeout: 15000 });
            await codeInput.fill(verificationCode);
            await page.getByRole("button", { name: "확인" }).click();

            await page.getByPlaceholder("닉네임").fill(nickname);
            await page.getByPlaceholder("비밀번호").nth(0).fill(password);
            await page.getByPlaceholder("비밀번호 확인").fill(password);


            // 회원가입 버튼 클릭
            page.once("dialog", async (dialog) => {
                expect(dialog.message()).toContain("회원가입이 완료");
                await dialog.accept();
            });
            await page.locator('button[type="submit"]', { hasText: "회원가입" }).click();

            await page.waitForTimeout(1000); // DB 반영 기다림

            // 로그인 시도 전 값 확인
            console.log("로그인 시도 계정:", email, password);

            await page.waitForTimeout(1000);
            await page.goto("/auth/login");
            await page.getByPlaceholder("이메일").fill(email);
            await page.getByPlaceholder("비밀번호").fill(password);

            page.once("dialog", async (dialog) => await dialog.accept());
            await page.locator('button[type="submit"]', { hasText: "로그인" }).click();

            await page.waitForTimeout(1000);

            await expect(page).toHaveURL("/", { timeout: 5000 });

            await page.goto("/");
            const enterBtn = page.getByRole("button", { name: "경매 참여하기" }).first();
            await expect(enterBtn).toBeVisible();
            await enterBtn.click();

            const bidBtn = page.getByRole("button", { name: /입찰하기/ });
            await expect(bidBtn).toBeEnabled();
            await bidBtn.click();

            await page.waitForTimeout(500);

            await page.goto("/mypage");
            await expect(page.locator('p.text-lg', { hasText: nickname })).toBeVisible();
            await page.getByRole("button", { name: "로그아웃" }).click();
            await page.waitForTimeout(500); // 로그아웃 후 세션 정리 여유 시간
            console.log("사용자 로그아웃 후 테스트 정상 종료됨");
        });
    }
});
