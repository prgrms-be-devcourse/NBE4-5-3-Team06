"use client";

import { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import { getAccessToken, getUserInfo } from "@/lib/api/auth";
import { useSession } from "next-auth/react";

export default function MyPageEdit() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [profileImage, setProfileImage] = useState("");
  const [previewImage, setPreviewImage] = useState("/default-profile.png");
  const { data: session } = useSession();
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
console.log("✅ 닉네임 확인:", session?.user?.name); // 조현우
console.log("✅ 이메일 확인:", session?.user?.email); // gusdndlek12@gmail.com

useEffect(() => {
  const localToken = getAccessToken();
  const { userUUID } = getUserInfo();
  const sessionToken = session?.accessToken;
  const email = session?.user?.email;
  
  // 구글 사용자 정보가 있으면 닉네임을 설정
  const nicknameFromSession = session?.user?.nickname || session?.user?.name;

  if (userUUID && localToken) {
    axios
      .get(`${API_BASE_URL}/auth/users/${userUUID}`, {
        headers: { Authorization: `Bearer ${localToken}` },
      })
      .then((res) => {
        const user = res.data.data;
        setNickname(user.nickname || nicknameFromSession); // 닉네임이 없다면 session에서 가져오기
        setEmail(user.email);
        setProfileImage(user.profileImage);
        setPreviewImage(user.profileImage || "/default-profile.png");
      })
      .catch(() => {
        alert("❌ 사용자 정보 불러오기 실패");
      });
  } else if (email && sessionToken) {
    axios
      .get(`${API_BASE_URL}/auth/users/email?email=${email}`, {
        headers: { Authorization: `Bearer ${sessionToken}` },
      })
      .then((res) => {
        const user = res.data.data;
        setNickname(user.nickname || nicknameFromSession); // 닉네임이 없다면 session에서 가져오기
        setEmail(user.email);
        setProfileImage(user.profileImage);
        setPreviewImage(user.profileImage || "/default-profile.png");
      })
      .catch(() => {
        alert("❌ 구글 사용자 정보 불러오기 실패");
      });
  }
}, [session]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onloadend = () => {
      const result = reader.result as string;
      setPreviewImage(result);
      setProfileImage(result);
    };
    reader.readAsDataURL(file);
  };

  const handleSave = async () => {
    if (!nickname.trim()) return alert("닉네임을 입력해주세요.");
    if (!email.trim()) return alert("이메일을 입력해주세요.");

    let token = getAccessToken() || session?.accessToken as string;
    let userUUID = getUserInfo()?.userUUID;

    if (!userUUID && session?.user?.email && token) {
      try {
        const res = await axios.get(
          `${API_BASE_URL}/auth/users/email?email=${encodeURIComponent(session.user.email)}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        userUUID = res.data.data.userUUID;
      } catch (e) {
        console.error("❌ UUID 조회 실패", e);
        return alert("UUID 조회 실패");
      }
    }

    if (!userUUID || !token) {
      return alert("사용자 인증 정보가 없습니다.");
    }

    try {
      await axios.put(
        `${API_BASE_URL}/auth/users/${userUUID}`,
        {
          nickname,
          email,
          password: password || undefined,
          profileImage,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      alert("✅ 저장되었습니다.");
      router.replace("/mypage");
    } catch (err: any) {
      console.error("❌ 수정 실패:", err);
      alert("❌ 수정 실패: " + (err.response?.data?.msg || err.message));
    }
  };

  return (
    <div className="max-w-xl mx-auto p-6">
      <h2 className="text-2xl font-bold mb-6">프로필 수정</h2>
      <div className="flex flex-col items-center gap-4 mb-6">
        <img
          src={previewImage}
          alt="프로필"
          className="w-24 h-24 rounded-full object-cover border"
        />
        <label className="px-4 py-2 bg-gray-200 rounded cursor-pointer hover:bg-gray-300">
          이미지 변경
          <input type="file" accept="image/*" hidden onChange={handleImageChange} />
        </label>
      </div>

      <label className="block mb-2 font-medium">닉네임</label>
      <input
        type="text"
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
        className="border px-4 py-2 rounded w-full mb-4"
      />

      <label className="block mb-2 font-medium">이메일</label>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        className="border px-4 py-2 rounded w-full mb-4"
      />

      <div className="flex justify-end gap-2">
        <button
          onClick={() => router.back()}
          className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200"
        >
          취소
        </button>
        <button
          onClick={handleSave}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          저장
        </button>
      </div>
    </div>
  );
}
