// src/types/next-auth.d.ts
import NextAuth, { DefaultSession } from "next-auth";
import { JWT } from "next-auth/jwt";

declare module "next-auth" {
  interface Session {
    accessToken?: string;
    user?: {
      name?: string  ;
      email?: string  ;
      image?: string  ;
      userUUID?: string;
      nickname?: string;
      profileImage?: string;
    };
  }

  interface User {
    accessToken?: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string;
    user?: {
      name?: string ;
      email?: string ;
      image?: string ;
      userUUID?: string;
      nickname?: string;
      profileImage?: string;
    };
  }
}
