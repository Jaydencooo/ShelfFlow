import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import type { UserCatalogProductDetail } from "@/lib/types"

export async function GET(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const { id } = await params
    const product = await gatewayRequest<UserCatalogProductDetail>(`/api/user/catalog/products/${id}`)
    return NextResponse.json(buildSuccessResponse(product, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "商品详情查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
