// src/types/next-auth.d.ts
import NextAuth, { DefaultSession, DefaultUser } from "next-auth";

declare module "next-auth" {
  interface Session {
    accessToken?: string;
    user: {
      name?: string | null;
      email?: string | null;
      image?: string | null;
      userUUID?: string;
      nickname?: string;
      profileImage?: string;
    };
  }

  interface User extends DefaultUser {
    accessToken?: string;
    userUUID?: string;
    nickname?: string;
    profileImage?: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string;
    user?: {
      name?: string | null;
      email?: string | null;
      image?: string | null;
      userUUID?: string;
      nickname?: string;
      profileImage?: string;
    };
  }
}
