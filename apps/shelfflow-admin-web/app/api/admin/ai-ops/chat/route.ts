import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminAiOpsChatResponse } from "@/lib/types"
import { aiChatSchema } from "@/lib/validation"

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const payload = aiChatSchema.parse(await request.json())
    const answer = await gatewayRequest<AdminAiOpsChatResponse>("/api/admin/ai-ops/chat", {
      method: "POST",
      body: payload,
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(answer, requestId, "问答完成"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "问题参数不合法")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
