import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserOrderDetail } from "@/lib/types"

export async function POST(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const detail = await gatewayRequest<UserOrderDetail>(`/api/user/orders/${id}/pay`, {
      method: "POST",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(detail, requestId, "支付成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "支付订单失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
