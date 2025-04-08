import { NextResponse } from 'next/server';

const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
const GOOGLE_CLIENT_SECRET = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SECRET;
const REDIRECT_URI = `${process.env.REDIRECT_URI}/api/auth/callback/google`;

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const code = searchParams.get('code');
    const error = searchParams.get('error');

    if (error) {
      console.error('Google OAuth error:', error);
      return NextResponse.redirect(new URL('/auth/login?error=auth_failed', request.url));
    }

    if (!code) {
      return NextResponse.redirect(new URL('/auth/login?error=no_code', request.url));
    }

    if (!GOOGLE_CLIENT_ID || !GOOGLE_CLIENT_SECRET) {
      console.error('Missing Google OAuth credentials');
      return NextResponse.redirect(new URL('/auth/login?error=auth_failed', request.url));
    }

    // 구글 OAuth 토큰 요청
    const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        code,
        client_id: GOOGLE_CLIENT_ID,
        client_secret: GOOGLE_CLIENT_SECRET,
        redirect_uri: REDIRECT_URI,
        grant_type: 'authorization_code',
      }).toString(),
    });

    if (!tokenResponse.ok) {
      const errorData = await tokenResponse.json();
      console.error('Token error:', errorData);
      return NextResponse.redirect(new URL('/auth/login?error=token_failed', request.url));
    }

    const tokenData = await tokenResponse.json();

    // 구글 사용자 정보 요청
    const userInfoResponse = await fetch('https://www.googleapis.com/oauth2/v2/userinfo', {
      headers: {
        Authorization: `Bearer ${tokenData.access_token}`,
      },
    });

    if (!userInfoResponse.ok) {
      const errorData = await userInfoResponse.json();
      console.error('User info error:', errorData);
      return NextResponse.redirect(new URL('/auth/login?error=user_info_failed', request.url));
    }

    const userInfo = await userInfoResponse.json();

    // 여기서 사용자 정보를 데이터베이스에 저장하거나 업데이트하는 로직을 추가할 수 있습니다.
    // 예: const user = await prisma.user.upsert({...})

    // 로그인 성공 시 메인 페이지로 리다이렉트
    const response = NextResponse.redirect(new URL('/', request.url));
    
    // 사용자 정보를 쿠키에 저장
    response.cookies.set('user', JSON.stringify({
      email: userInfo.email,
      name: userInfo.name,
      picture: userInfo.picture,
    }), {
      httpOnly: true,
      secure: true,
      sameSite: 'none',
      path: '/',
    });

    return response;
  } catch (error) {
    console.error('Google login error:', error);
    return NextResponse.redirect(new URL('/auth/login?error=auth_failed', request.url));
  }
} 