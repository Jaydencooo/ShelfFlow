import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserOrderDetail } from "@/lib/types"
import { cancelOrderSchema } from "@/lib/validation"

export async function GET(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const detail = await gatewayRequest<UserOrderDetail>(`/api/user/orders/${id}`, { accessToken })
    return NextResponse.json(buildSuccessResponse(detail, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "订单详情查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const payload = cancelOrderSchema.parse(await request.json().catch(() => ({})))
    await gatewayRequest(`/api/user/orders/${id}`, {
      method: "DELETE",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "取消订单成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "取消订单失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
