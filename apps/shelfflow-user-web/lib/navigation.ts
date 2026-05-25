import { APP_ROUTES } from "@/lib/constants"

export function buildLoginRedirectPath(nextPath?: string) {
  if (!nextPath) {
    return APP_ROUTES.login
  }

  const safePath = resolveSafeNextPath(nextPath)
  return safePath ? `${APP_ROUTES.login}?next=${encodeURIComponent(safePath)}` : APP_ROUTES.login
}

export function buildRegisterRedirectPath(nextPath?: string) {
  if (!nextPath) {
    return APP_ROUTES.register
  }

  const safePath = resolveSafeNextPath(nextPath)
  return safePath ? `${APP_ROUTES.register}?next=${encodeURIComponent(safePath)}` : APP_ROUTES.register
}

export function resolveSafeNextPath(candidate: string | null | undefined) {
  if (!candidate) {
    return null
  }

  if (!candidate.startsWith("/") || candidate.startsWith("//")) {
    return null
  }

  return candidate
}
