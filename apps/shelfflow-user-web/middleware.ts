import type { NextRequest } from "next/server"
import { NextResponse } from "next/server"

import { APP_ROUTES, USER_SESSION_COOKIE_NAME } from "@/lib/constants"
import { resolveSafeNextPath } from "@/lib/navigation"

const publicAuthRoutes = [APP_ROUTES.login, APP_ROUTES.register, APP_ROUTES.forgotPassword] as const

export function middleware(request: NextRequest) {
  const hasSession = Boolean(request.cookies.get(USER_SESSION_COOKIE_NAME)?.value)
  const nextPath = `${request.nextUrl.pathname}${request.nextUrl.search}`

  if (
    (
      request.nextUrl.pathname.startsWith(APP_ROUTES.cart) ||
      request.nextUrl.pathname.startsWith(APP_ROUTES.orders) ||
      request.nextUrl.pathname.startsWith(APP_ROUTES.account)
    ) &&
    !hasSession
  ) {
    const loginUrl = new URL(APP_ROUTES.login, request.url)
    loginUrl.searchParams.set("next", nextPath)
    return NextResponse.redirect(loginUrl)
  }

  if (publicAuthRoutes.some((route) => route === request.nextUrl.pathname) && hasSession) {
    const safeNextPath = resolveSafeNextPath(request.nextUrl.searchParams.get("next"))
    return NextResponse.redirect(new URL(safeNextPath ?? APP_ROUTES.products, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/account/:path*", "/cart/:path*", "/orders/:path*", "/login", "/register", "/forgot-password"]
}
