import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import { updateCartItemQuantitySchema } from "@/lib/validation"

export async function DELETE(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    await gatewayRequest(`/api/user/cart/items/${id}`, {
      method: "DELETE",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "删除购物车项成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "删除购物车项失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function PATCH(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const payload = updateCartItemQuantitySchema.parse(await request.json())
    await gatewayRequest(`/api/user/cart/items/${id}`, {
      method: "PATCH",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "更新购物车数量成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "购物车数量不合法",
      dependencyMessage: "更新购物车数量失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
