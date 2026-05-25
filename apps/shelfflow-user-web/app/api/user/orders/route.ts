import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { PaginatedResult, UserOrderSubmitResult, UserOrderSummary } from "@/lib/types"
import { orderQuerySchema, submitOrderSchema } from "@/lib/validation"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const url = new URL(request.url)
    const query = orderQuerySchema.parse(Object.fromEntries(url.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
      ...(query.status ? { status: query.status } : {}),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    const pageResult = await gatewayRequest<PaginatedResult<UserOrderSummary>>(`/api/user/orders?${searchParams.toString()}`, {
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(pageResult, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "订单查询参数不合法",
      dependencyMessage: "订单列表查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const payload = submitOrderSchema.parse(await request.json())
    const order = await gatewayRequest<UserOrderSubmitResult>("/api/user/orders", {
      method: "POST",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(order, requestId, "下单成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "下单参数不合法",
      dependencyMessage: "下单失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
