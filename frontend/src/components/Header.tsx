"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { useSession, signOut } from "next-auth/react";

export function Header() {
  const router = useRouter();
  const { data: session, status } = useSession();
  const [isLocalLoggedIn, setIsLocalLoggedIn] = useState(false);

  // 전통 로그인 (localStorage) 상태 확인
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    setIsLocalLoggedIn(!!token);
  }, []);

  // 두 방식 중 하나라도 로그인되어 있으면 로그인 상태로 간주
  const isLoggedIn = isLocalLoggedIn || status === "authenticated";

  const handleLogout = async () => {
    try {
      // 전통 로그인 관련 로그아웃 처리
      const token = localStorage.getItem("accessToken");
      if (token) {
        const res = await fetch("http://localhost:8080/api/auth/logout", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`, // 백틱 사용
          },
        });
        if (!res.ok) {
          throw new Error("로그아웃 요청 실패");
        }
        localStorage.removeItem("accessToken");
        localStorage.removeItem("nickname");
        localStorage.removeItem("userUUID");
      }
      // NextAuth 로그아웃 처리 (구글 로그인 등)
      await signOut({ callbackUrl: "/" });
      router.push("/");
    } catch (error) {
      console.error("로그아웃 실패:", error);
      alert("로그아웃 실패");
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center justify-between">
        <Link href="/" className="flex items-center space-x-2">
          <span className="font-bold text-xl">NBE</span>
        </Link>

        <nav className="flex items-center gap-4">
          {isLoggedIn ? (
            <>
              <Link href="/mypage">
                <Button variant="outline">마이페이지</Button>
              </Link>
              <Button variant="outline" onClick={handleLogout}>
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Link href="/auth/login">
                <Button variant="ghost">로그인</Button>
              </Link>
              <Link href="/auth/register">
                <Button>회원가입</Button>
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
