import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { readSession } from "@/lib/server/session"
import type { SessionUser } from "@/lib/types"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()
  const session = await readSession()

  if (!session) {
    const error = new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
    return NextResponse.json(buildErrorResponse(error, requestId), { status: 401 })
  }

  const user: SessionUser = {
    userId: session.userId,
    username: session.username,
    displayName: session.displayName,
    roles: session.roles,
    permissions: session.permissions
  }

  return NextResponse.json(buildSuccessResponse(user, requestId))
}
