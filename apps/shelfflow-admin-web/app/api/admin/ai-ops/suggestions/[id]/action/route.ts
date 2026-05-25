import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function POST(request: Request, context: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const payload = await request.json()
    await gatewayRequest<null>(`/api/admin/ai-ops/suggestions/${encodeURIComponent(id)}/action`, {
      method: "POST",
      body: payload,
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "建议处理成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "AI 运营建议处理失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
