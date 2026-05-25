import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserCartItem } from "@/lib/types"
import { addCartItemSchema } from "@/lib/validation"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const items = await gatewayRequest<UserCartItem[]>("/api/user/cart/items", { accessToken })
    return NextResponse.json(buildSuccessResponse(items, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "购物车查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const payload = addCartItemSchema.parse(await request.json())
    await gatewayRequest("/api/user/cart/items", {
      method: "POST",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "加入购物车成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "购物车参数不合法",
      dependencyMessage: "加入购物车失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    await gatewayRequest("/api/user/cart/items", {
      method: "DELETE",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "清空购物车成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "清空购物车失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
