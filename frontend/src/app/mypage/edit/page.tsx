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

  useEffect(() => {
    const localToken = getAccessToken();
    const { userUUID } = getUserInfo();

    // ✅ 콘솔로 세션 확인
    console.log("✅ session:", session);
    console.log("✅ accessToken:", session?.accessToken);

    // 전통 로그인 사용자
    if (userUUID) {
      axios
        .get(`http://localhost:8080/api/auth/users/${userUUID}`, {
          headers: { Authorization: `Bearer ${localToken}` },
        })
        .then((res) => {
          const user = res.data.data;
          setNickname(user.nickname);
          setEmail(user.email);
          setProfileImage(user.profileImage);
          setPreviewImage(user.profileImage || "/default-profile.png");
        })
        .catch(() => {
          alert("❌ 사용자 정보 불러오기 실패");
        });
    }
    // 구글 로그인 사용자
    else if (session?.user?.email && session?.accessToken) {
      axios
        .get(
          `http://localhost:8080/api/auth/users/email?email=${session.user.email}`,
          {
            headers: {
              Authorization: `Bearer ${session.accessToken}`,
            },
          }
        )
        .then((res) => {
          const user = res.data.data;
          setNickname(user.nickname);
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
      setPreviewImage(reader.result as string);
      setProfileImage(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleSave = async () => {
    if (!nickname.trim()) return alert("닉네임을 입력해주세요.");
    if (!email.trim()) return alert("이메일을 입력해주세요.");

    let token = getAccessToken();
    let { userUUID } = getUserInfo();

    // 구글 로그인 사용자라면 세션에서 가져오기
    if (!token && session?.accessToken) {
      token = session.accessToken as string;
    }

    if (!userUUID && session?.user?.email && token) {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/auth/users/email?email=${encodeURIComponent(session.user.email)}`,
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
        `http://localhost:8080/api/auth/users/${userUUID}`,
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
      router.push("/mypage");
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
          <input
            type="file"
            accept="image/*"
            hidden
            onChange={handleImageChange}
          />
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
