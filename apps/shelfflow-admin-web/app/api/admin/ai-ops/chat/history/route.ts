import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminAiOpsChatMessage } from "@/lib/types"

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { searchParams } = new URL(request.url)
    const sessionId = searchParams.get("sessionId")
    const path = sessionId ? `/api/admin/ai-ops/chat/history?sessionId=${encodeURIComponent(sessionId)}` : "/api/admin/ai-ops/chat/history"
    const messages = await gatewayRequest<AdminAiOpsChatMessage[]>(path, { accessToken })
    return NextResponse.json(buildSuccessResponse(messages, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "AI 问答记录查询失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
