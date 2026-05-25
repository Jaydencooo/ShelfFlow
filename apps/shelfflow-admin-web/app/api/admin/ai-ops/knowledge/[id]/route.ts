import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminAiKnowledge } from "@/lib/types"
import { aiKnowledgeSchema } from "@/lib/validation"

interface RouteContext {
  params: Promise<{ id: string }>
}

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function PUT(request: Request, context: RouteContext) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const payload = aiKnowledgeSchema.parse(await request.json())
    const knowledge = await gatewayRequest<AdminAiKnowledge>(`/api/admin/ai-ops/knowledge/${id}`, {
      method: "PUT",
      body: payload,
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(knowledge, requestId, "知识条目更新成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "知识条目参数不合法")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(request: Request, context: RouteContext) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    await gatewayRequest<null>(`/api/admin/ai-ops/knowledge/${id}`, {
      method: "DELETE",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "知识条目删除成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "知识条目删除失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
