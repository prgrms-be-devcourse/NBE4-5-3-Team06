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
      if (account && user?.email) {
        const res = await fetch("http://35.203.149.35:8080/api/auth/token", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: user.email }),
        });

        const data = await res.json();
        token.accessToken = data.data.token;
        token.user = {
          email: user.email,
          nickname: data.data.nickname,
          userUUID: data.data.userUUID,
          profileImage: data.data.profileImage,
        };
      }
      return token;
    },

    // 세션 확장
    async session({ session, token }) {
      session.accessToken = token.accessToken;
      session.user = {
        ...session.user,
        ...token.user,
      };
      return session;
    },
  },
});

export { handler as GET, handler as POST };
