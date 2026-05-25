import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminOrderDetail } from "@/lib/types"
import { adminOrderStatusSchema } from "@/lib/validation"

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
    const orderStatus = adminOrderStatusSchema.parse(payload.orderStatus)
    const order = await gatewayRequest<AdminOrderDetail>(`/api/admin/orders/${id}/status`, {
      method: "POST",
      body: { orderStatus },
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(order, requestId, "订单状态更新成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "订单状态参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
