import type { NextRequest } from "next/server"
import { NextResponse } from "next/server"

import { APP_ROUTES, USER_SESSION_COOKIE_NAME } from "@/lib/constants"

const protectedRoutes = [APP_ROUTES.cart, APP_ROUTES.orders, APP_ROUTES.account] as const

export function middleware(request: NextRequest) {
  const hasSession = Boolean(request.cookies.get(USER_SESSION_COOKIE_NAME)?.value)
  const nextPath = `${request.nextUrl.pathname}${request.nextUrl.search}`

  if (protectedRoutes.some((route) => request.nextUrl.pathname.startsWith(route)) && !hasSession) {
    const loginUrl = new URL(APP_ROUTES.login, request.url)
    loginUrl.searchParams.set("next", nextPath)
    return NextResponse.redirect(loginUrl)
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/account/:path*", "/cart/:path*", "/orders/:path*", "/login", "/register", "/forgot-password"]
}
