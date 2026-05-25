import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminPricingRule } from "@/lib/types"
import { z } from "zod"

const statusSchema = z.enum(["enabled", "disabled"])

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> }
) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const payload = await request.json()
    const status = statusSchema.parse(payload.status)
    const rule = await gatewayRequest<AdminPricingRule>(`/api/admin/pricing-rules/${id}/status`, {
      method: "POST",
      body: { status },
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(rule, requestId, "定价规则状态更新成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "定价规则状态参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
