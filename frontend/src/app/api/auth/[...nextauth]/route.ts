// src/app/api/auth/[...nextauth]/route.ts
// src/app/api/auth/[...nextauth]/route.ts
import NextAuth from "next-auth";
import GoogleProvider from "next-auth/providers/google";

const handler = NextAuth({
  providers: [
    GoogleProvider({
      clientId: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID!,
      clientSecret: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SECRET!,
    }),
  ],
  callbacks: {
    // JWT 생성 시 사용자 정보 넣기
    async jwt({ token, account, user }) {
      const email = user?.email || token?.email;
    
      // ✅ 이 시점에서는 CustomOAuth2UserService가 사용자 등록을 마친 뒤!
      if (!token.accessToken && email) {
        try {
          const res = await fetch("http://localhost:8080/api/auth/token", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email }),
          });
    
          const data = await res.json();
          console.log("📦 백엔드 응답:", data);
          if (data?.data?.token) {
            token.accessToken = data.data.token;
            token.userUUID = data.data.userUUID;
            token.user = {
              name: data.data.nickname,
              email: data.data.email,
              image: data.data.profileImage,
              userUUID: data.data.userUUID,
            };
          } else {
            console.warn("❗️백엔드 응답 이상:", data);
          }
        } catch (e) {
          console.error("❌ 백엔드 토큰 요청 실패:", e);
        }
      }
    
      return token;
    },

    // 세션 확장
    async session({ session ,token}) {
      
      console.log("📦 세션 확장: token", token);
  
  session.accessToken = token.accessToken;   // ✅ JWT 토큰 전달
  session.user = token.user;   

  return session;
    },
  },
});

export { handler as GET, handler as POST };

