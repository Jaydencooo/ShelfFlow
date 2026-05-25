import { cookies } from "next/headers"

import { SESSION_COOKIE_NAME, SESSION_MAX_AGE_SECONDS, TOKEN_COOKIE_NAME } from "@/lib/constants"
import type { AdminSession } from "@/lib/types"

export async function readSession(): Promise<AdminSession | null> {
  const cookieStore = await cookies()
  const rawValue = cookieStore.get(SESSION_COOKIE_NAME)?.value
  if (!rawValue) {
    return null
  }

  try {
    const payload = JSON.parse(rawValue)
    if (
      typeof payload?.userId === "string" &&
      typeof payload?.username === "string" &&
      typeof payload?.displayName === "string" &&
      Array.isArray(payload?.roles) &&
      Array.isArray(payload?.permissions) &&
      typeof payload?.token === "string"
    ) {
      return payload as AdminSession
    }

    return null
  } catch {
    return null
  }
}

export async function readAccessToken(): Promise<string | null> {
  const cookieStore = await cookies()
  return cookieStore.get(TOKEN_COOKIE_NAME)?.value ?? null
}

export async function persistSession(session: AdminSession): Promise<void> {
  const cookieStore = await cookies()
  const cookieOptions = {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: SESSION_MAX_AGE_SECONDS
  }

  cookieStore.set(SESSION_COOKIE_NAME, JSON.stringify(session), cookieOptions)
  cookieStore.set(TOKEN_COOKIE_NAME, session.token, cookieOptions)
}

export async function clearSession(): Promise<void> {
  const cookieStore = await cookies()
  cookieStore.delete(SESSION_COOKIE_NAME)
  cookieStore.delete(TOKEN_COOKIE_NAME)
}
