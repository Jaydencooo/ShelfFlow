import { cookies } from "next/headers"

import { SESSION_MAX_AGE_SECONDS, USER_SESSION_COOKIE_NAME, USER_TOKEN_COOKIE_NAME } from "@/lib/constants"
import type { UserSession } from "@/lib/types"

export async function readSession(): Promise<UserSession | null> {
  const cookieStore = await cookies()
  const rawValue = cookieStore.get(USER_SESSION_COOKIE_NAME)?.value
  if (!rawValue) {
    return null
  }

  try {
    const payload = JSON.parse(rawValue)
    if (typeof payload?.userId === "string" && typeof payload?.openId === "string" && typeof payload?.token === "string") {
      return payload as UserSession
    }
    return null
  } catch {
    return null
  }
}

export async function readAccessToken(): Promise<string | null> {
  const cookieStore = await cookies()
  return cookieStore.get(USER_TOKEN_COOKIE_NAME)?.value ?? null
}

export async function persistSession(session: UserSession): Promise<void> {
  const cookieStore = await cookies()
  const cookieOptions = {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: SESSION_MAX_AGE_SECONDS
  }

  cookieStore.set(USER_SESSION_COOKIE_NAME, JSON.stringify(session), cookieOptions)
  cookieStore.set(USER_TOKEN_COOKIE_NAME, session.token, cookieOptions)
}

export async function clearSession(): Promise<void> {
  const cookieStore = await cookies()
  cookieStore.delete(USER_SESSION_COOKIE_NAME)
  cookieStore.delete(USER_TOKEN_COOKIE_NAME)
}
