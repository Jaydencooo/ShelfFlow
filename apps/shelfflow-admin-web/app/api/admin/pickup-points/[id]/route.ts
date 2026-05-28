import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { PickupPoint } from "@/lib/types"
import { pickupPointSchema } from "@/lib/validation"

async function requireAccessToken() {
  const accessToken = await readAccessToken()
  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }
  return accessToken
}

export async function PUT(request: Request, context: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const payload = pickupPointSchema.parse(await request.json())
    const pickupPoint = await gatewayRequest<PickupPoint>(`/api/admin/pickup-points/${encodeURIComponent(id)}`, {
      method: "PUT",
      body: payload,
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(pickupPoint, requestId, "自提点更新成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "自提点更新失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(request: Request, context: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    await gatewayRequest<null>(`/api/admin/pickup-points/${encodeURIComponent(id)}`, {
      method: "DELETE",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "自提点已停用"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "自提点停用失败")
    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
