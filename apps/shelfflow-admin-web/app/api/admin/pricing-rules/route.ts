import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminPricingRule, PaginatedResult } from "@/lib/types"
import { pricingRuleQuerySchema, pricingRuleSchema } from "@/lib/validation"

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
    const query = pricingRuleQuerySchema.parse(Object.fromEntries(url.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
      ...(query.keyword ? { keyword: query.keyword } : {}),
      ...(query.status ? { status: query.status } : {})
    })

    const pageResult = await gatewayRequest<PaginatedResult<AdminPricingRule>>(`/api/admin/pricing-rules?${searchParams.toString()}`, {
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(pageResult, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "定价规则查询参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const payload = pricingRuleSchema.parse(await request.json())
    const rule = await gatewayRequest<AdminPricingRule>("/api/admin/pricing-rules", {
      method: "POST",
      body: payload,
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(rule, requestId, "定价规则创建成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "定价规则参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
