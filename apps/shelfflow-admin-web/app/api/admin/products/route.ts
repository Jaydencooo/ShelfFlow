import { NextResponse } from "next/server"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { readAccessToken } from "@/lib/server/session"
import type { PaginatedResult, ProductRecord } from "@/lib/types"
import { productQuerySchema, productSchema } from "@/lib/validation"

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
    const url = new URL(request.url)
    const query = productQuerySchema.parse(Object.fromEntries(url.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
      ...(query.keyword ? { keyword: query.keyword } : {}),
      ...(query.categoryId ? { categoryId: query.categoryId } : {}),
      ...(query.status !== undefined ? { status: String(query.status) } : {})
    })

    const pageResult = await gatewayRequest<PaginatedResult<ProductRecord>>(`/api/admin/products?${searchParams.toString()}`, {
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(pageResult, requestId))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "商品查询参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireAccessToken()
    const payload = productSchema.parse(await request.json())

    await gatewayRequest("/api/admin/products", {
      method: "POST",
      body: payload,
      accessToken
    })

    return NextResponse.json(buildSuccessResponse(null, requestId, "商品创建成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.VALIDATION_ERROR, 400, "商品参数不合法")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
