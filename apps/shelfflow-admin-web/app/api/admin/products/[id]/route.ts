import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import { productSchema } from "@/lib/validation"

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

export async function PUT(
  request: Request,
  context: { params: Promise<{ id: string }> }
) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    const payload = productSchema.parse(await request.json())

    await gatewayRequest(`/api/admin/products/${id}`, {
      method: "PUT",
      body: payload,
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(null, requestId, "商品更新成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "商品参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(
  request: Request,
  context: { params: Promise<{ id: string }> }
) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const { id } = await context.params
    await gatewayRequest(`/api/admin/products/${id}`, {
      method: "DELETE",
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(null, requestId, "商品已删除"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.DEPENDENCY_ERROR, 502, "商品删除失败")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
