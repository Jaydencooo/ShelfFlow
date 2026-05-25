import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminAiKnowledge, PaginatedResult } from "@/lib/types"
import { aiKnowledgeQuerySchema, aiKnowledgeSchema } from "@/lib/validation"

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
    const url = new URL(request.url)
    const query = aiKnowledgeQuerySchema.parse(Object.fromEntries(url.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
      ...(query.keyword ? { keyword: query.keyword } : {}),
      ...(query.category ? { category: query.category } : {})
    })
    const pageResult = await gatewayRequest<PaginatedResult<AdminAiKnowledge>>(`/api/admin/ai-ops/knowledge?${searchParams.toString()}`, {
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(pageResult, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "知识库查询参数不合法")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const payload = aiKnowledgeSchema.parse(await request.json())
    const knowledge = await gatewayRequest<AdminAiKnowledge>("/api/admin/ai-ops/knowledge", {
      method: "POST",
      body: payload,
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(knowledge, requestId, "知识条目创建成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "知识条目参数不合法")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
