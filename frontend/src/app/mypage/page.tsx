"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { getAccessToken } from "@/lib/api/auth";

interface User {
  nickname: string;
  email: string;
  profileImage?: string;
}

interface Auction {
  auctionId: number;
  productName: string;
  description?: string;
  winningBid: number;
  winTime: string;
  imageUrl?: string;
}

export default function MyPage() {
  const { userUUID } = useParams();
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [auctions, setAuctions] = useState<Auction[]>([]);
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
  const { data: session, status } = useSession();

  useEffect(() => {
    const fetchUserData = async () => {
      let token = getAccessToken();
      let uuid = userUUID || localStorage.getItem("userUUID");

      if (!uuid && session?.user?.email && session?.accessToken) {
        token = session.accessToken;
        try {
          const res = await fetch(
            `${API_BASE_URL}/auth/users/email?email=${session.user.email}`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
          );
          const data = await res.json();
          uuid = data.data.userUUID;
        } catch (err) {
          console.error("UUID 조회 실패", err);
          return;
        }
      }

      if (!uuid || !token) return;

      const headers = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      };

      // 사용자 정보
      fetch(`${API_BASE_URL}/auth/users/${uuid}`, { headers })
        .then((res) => (res.ok ? res.json() : null))
        .then((data) => data?.data && setUser(data.data))
        .catch(console.error);

      // 낙찰 경매 목록
      fetch(`${API_BASE_URL}/auctions/${uuid}/winner`, { headers })
        .then((res) => (res.ok ? res.json() : null))
        .then((data) => Array.isArray(data?.data) && setAuctions(data.data))
        .catch(console.error);
    };

    if (status === "authenticated" || getAccessToken()) {
      fetchUserData();
    }
  }, [userUUID, session, status, router]);

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 프로필 정보 */}
      <div className="flex items-center gap-6 p-4 border rounded-lg shadow">
        <div className="w-20 h-20 bg-gray-300 rounded-full overflow-hidden">
          <img
            src={user?.profileImage || "/default-profile.png"}
            alt="Profile"
            className="w-full h-full object-cover"
          />
        </div>
        <div>
          <p className="text-lg font-semibold">{user?.nickname || "닉네임"}</p>
          <p className="text-gray-600">{user?.email || "email@example.com"}</p>
        </div>
        <button
          className="ml-auto px-3 py-2 bg-blue-500 text-white rounded"
          onClick={() => router.push("/mypage/edit")}
        >
          수정
        </button>
      </div>

      {/* 낙찰 받은 경매 목록 */}
      <h2 className="text-xl font-bold mt-6">낙찰 받은 경매</h2>
      <div className="flex flex-col gap-4 mt-4">
        {auctions.length > 0 ? (
          auctions.map((auction) => (
            <div key={auction.auctionId} className="relative flex border rounded-lg p-4 shadow gap-4">
              <div className="w-60 h-40 bg-gray-200 overflow-hidden rounded-lg flex-shrink-0">
                <img
                  src={auction.imageUrl || "/default-image.jpg"}
                  alt={auction.productName}
                  className="w-full h-full object-cover"
                  onError={(e) => (e.currentTarget.src = "/default-image.jpg")}
                />
              </div>
              <div className="flex flex-col justify-center flex-1 relative">
                <p className="absolute right-2 top-2 text-red-500 text-sm font-semibold">결제 대기중</p>
                <p className="text-lg font-semibold">{auction.productName}</p>
                <p className="text-sm text-gray-600">{auction.description || "설명 없음"}</p>
                <p className="text-gray-500 text-sm">{new Date(auction.winTime).toLocaleString()}</p>
                <p className="text-blue-500 font-bold">
                  낙찰가: ₩{auction.winningBid.toLocaleString()}원
                </p>
              </div>
            </div>
          ))
        ) : (
          <p className="text-gray-500 mt-4">낙찰 받은 경매가 없습니다.</p>
        )}
      </div>
    </div>
  );
}
