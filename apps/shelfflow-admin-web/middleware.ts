import type { NextRequest } from "next/server"
import { NextResponse } from "next/server"

import { DASHBOARD_DEFAULT_ROUTE, DASHBOARD_ROUTES, SESSION_COOKIE_NAME } from "@/lib/constants"

export function middleware(request: NextRequest) {
  const hasSession = Boolean(request.cookies.get(SESSION_COOKIE_NAME)?.value)

  if (request.nextUrl.pathname.startsWith("/dashboard") && !hasSession) {
    return NextResponse.redirect(new URL(DASHBOARD_ROUTES.login, request.url))
  }

  if (request.nextUrl.pathname === DASHBOARD_ROUTES.login && hasSession) {
    return NextResponse.redirect(new URL(DASHBOARD_DEFAULT_ROUTE, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/dashboard/:path*", "/login"]
}
