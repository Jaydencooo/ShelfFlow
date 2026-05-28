import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { PickupPoint } from "@/lib/types"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const pickupPoints = await gatewayRequest<PickupPoint[]>("/api/user/pickup-points")
    return NextResponse.json(buildSuccessResponse(pickupPoints, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "自提点服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
