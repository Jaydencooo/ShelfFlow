import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminOperationLog, PaginatedResult } from "@/lib/types"
import { operationLogQuerySchema } from "@/lib/validation"

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()
  const requestUrl = new URL(request.url)

  try {
    const accessToken = await requireAccessToken()
    const query = operationLogQuerySchema.parse(Object.fromEntries(requestUrl.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize)
    })
    if (query.module) searchParams.set("module", query.module)
    if (query.action) searchParams.set("action", query.action)

    const operationLogs = await gatewayRequest<PaginatedResult<AdminOperationLog>>(`/api/admin/operation-logs/page?${searchParams.toString()}`, { accessToken })
    return NextResponse.json(buildSuccessResponse(operationLogs, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "操作日志分页查询失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
