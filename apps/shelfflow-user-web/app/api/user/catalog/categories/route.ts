import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import type { UserCatalogCategory } from "@/lib/types"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const categories = await gatewayRequest<UserCatalogCategory[]>("/api/user/catalog/categories")
    return NextResponse.json(buildSuccessResponse(categories, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "类目查询失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
