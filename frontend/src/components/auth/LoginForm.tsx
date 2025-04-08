"use client";

import React, { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

interface LoginFormProps {
  onSubmit: (email: string, password: string) => void;
  error?: string;
}

export const LoginForm: React.FC<LoginFormProps> = ({ onSubmit, error }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();
  const searchParams = useSearchParams();
  const authError = searchParams.get('error');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(email, password);
  };

  const handleGoogleLogin = () => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    const redirectUri = encodeURIComponent("http://localhost:3000/api/auth/callback/google");
    const scope = encodeURIComponent('email profile openid');
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}&access_type=offline&prompt=consent&include_granted_scopes=true`;
    
    window.location.href = authUrl;
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-6">
      <div className="bg-white shadow-lg rounded-lg p-8 max-w-sm w-full text-center">
        <h1 className="text-3xl font-bold text-gray-800 mb-6">로그인</h1>
        {authError && (
          <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-md">
            {authError === 'auth_failed' && '인증에 실패했습니다. 다시 시도해주세요.'}
            {authError === 'no_code' && '인증 코드를 받지 못했습니다. 다시 시도해주세요.'}
            {authError === 'token_failed' && '토큰 발급에 실패했습니다. 다시 시도해주세요.'}
            {authError === 'user_info_failed' && '사용자 정보를 가져오는데 실패했습니다. 다시 시도해주세요.'}
          </div>
        )}
        <form onSubmit={handleSubmit} className="flex flex-col space-y-4">
          <button
            type="button"
            onClick={handleGoogleLogin}
            className="w-full p-3 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition flex items-center justify-center space-x-2"
          >
            <img
              src="https://www.google.com/favicon.ico"
              alt="Google"
              className="w-5 h-5"
            />
            <span>구글 로그인</span>
          </button>
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white text-gray-500">또는</span>
            </div>
          </div>
          <div className="text-left">
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700"
            >
              이메일
            </label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="이메일"
              autoComplete="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full p-3 border rounded-md focus:ring-2 focus:ring-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="text-left">
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700"
            >
              비밀번호
            </label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="비밀번호"
              autoComplete="current-password"  
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full p-3 border rounded-md focus:ring-2 focus:ring-blue-500 focus:outline-none"
              required
            />
          </div>
          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit"
            className="w-full p-3 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition"
          >
            로그인
          </button>
          <p className="text-sm text-gray-500">
            회원이 아니신가요?{' '}
            <span
              onClick={() => router.push('/auth/register')}
              className="text-blue-500 hover:underline cursor-pointer"
            >
              회원가입
            </span>
          </p>
        </form>
      </div>
    </div>
  );
};
