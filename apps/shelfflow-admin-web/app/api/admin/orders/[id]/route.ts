import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminOrderDetail } from "@/lib/types"

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

export async function GET(
  request: Request,
  context: { params: Promise<{ id: string }> }
) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const order = await gatewayRequest<AdminOrderDetail>(`/api/admin/orders/${id}`, {
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(order, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "订单查询失败")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
