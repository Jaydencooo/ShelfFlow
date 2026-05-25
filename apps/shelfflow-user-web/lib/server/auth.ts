import { AppError, errorCodes } from "@shelfflow/shared"

import { readAccessToken } from "@/lib/server/session"

export async function requireUserAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}
