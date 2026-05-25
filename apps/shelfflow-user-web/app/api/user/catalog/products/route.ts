import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import type { PaginatedResult, UserCatalogProduct } from "@/lib/types"
import { catalogQuerySchema } from "@/lib/validation"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const url = new URL(request.url)
    const query = catalogQuerySchema.parse(Object.fromEntries(url.searchParams.entries()))
    const searchParams = new URLSearchParams({
      page: String(query.page),
      pageSize: String(query.pageSize),
      ...(query.keyword ? { keyword: query.keyword } : {}),
      ...(query.categoryId ? { categoryId: query.categoryId } : {}),
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    const pageResult = await gatewayRequest<PaginatedResult<UserCatalogProduct>>(`/api/user/catalog/products?${searchParams.toString()}`)
    return NextResponse.json(buildSuccessResponse(pageResult, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "商品查询参数不合法",
      dependencyMessage: "商品目录查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
