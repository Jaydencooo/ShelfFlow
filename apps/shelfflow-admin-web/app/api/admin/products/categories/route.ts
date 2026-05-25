import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { AdminProductCategory } from "@/lib/types"
import { productCategorySchema } from "@/lib/validation"

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const categories = await gatewayRequest<AdminProductCategory[]>("/api/admin/products/categories", { accessToken })

    return NextResponse.json(buildSuccessResponse(categories, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.INTERNAL_ERROR, 500, "商品分类加载失败")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const payload = productCategorySchema.parse(await request.json())
    const category = await gatewayRequest<AdminProductCategory>("/api/admin/products/categories", {
      method: "POST",
      body: payload,
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(category, requestId, "分类创建成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "分类参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
