import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminPricingRule } from "@/lib/types"

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

export async function POST(
  request: Request,
  context: { params: Promise<{ batchId: string }> }
) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { batchId } = await context.params
    const rule = await gatewayRequest<AdminPricingRule>(`/api/admin/pricing-rules/suggestions/${batchId}/accept`, {
      method: "POST",
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(rule, requestId, "AI 定价建议已采纳"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "AI 定价建议采纳失败")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
