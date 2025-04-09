// src/app/api/auth/[...nextauth]/route.ts
import NextAuth from "next-auth";
import GoogleProvider from "next-auth/providers/google";

// 디버깅 로그 (환경변수 제대로 불러오는지 확인)
console.log("✅ NEXT_PUBLIC_GOOGLE_CLIENT_ID:", process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID);
console.log("✅ NEXT_PUBLIC_GOOGLE_CLIENT_SECRET:", process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SECRET);
console.log("✅ NEXTAUTH_URL:", process.env.NEXTAUTH_URL);

const handler = NextAuth({
  providers: [
    GoogleProvider({
        clientId: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID!,
        clientSecret: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SECRET!,
    }),
  ],
  callbacks: {
    async session({ session}) {
      // 세션을 확장하거나 커스터마이징 가능
    
      return session;
    },
  },
});

// GET / POST 요청을 NextAuth handler로 연결
export { handler as GET, handler as POST };
