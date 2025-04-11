"use client"; // 이 컴포넌트는 클라이언트 전용입니다.

import { SessionProvider } from "next-auth/react";
import { AuthProvider } from "./context/AuthContext";
import { ReactNode } from "react";

interface ClientProvidersProps {
  children: ReactNode;
}

export default function ClientProviders({ children }: ClientProvidersProps) {
  return (
    <SessionProvider>
      <AuthProvider>
        {children}
      </AuthProvider>
    </SessionProvider>
  );
}
