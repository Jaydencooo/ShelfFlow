import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { persistSession } from "@/lib/server/session"
import type { AdminSession, SessionUser } from "@/lib/types"
import { loginSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const payload = loginSchema.parse(await request.json())
    const session = await gatewayRequest<AdminSession>("/api/admin/auth/login", {
      method: "POST",
      body: payload
    })

    await persistSession(session)

    const user: SessionUser = {
      userId: session.userId,
      username: session.username,
      displayName: session.displayName,
      roles: session.roles,
      permissions: session.permissions
    }

    return NextResponse.json(buildSuccessResponse(user, requestId, "登录成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "登录参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), {
      status: appError.statusCode
    })
  }
}
