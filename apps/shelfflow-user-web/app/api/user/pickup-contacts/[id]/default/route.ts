import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserPickupContact } from "@/lib/types"

export async function PATCH(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const contact = await gatewayRequest<UserPickupContact>(`/api/user/pickup-contacts/${id}/default`, {
      method: "PATCH",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(contact, requestId, "默认联系人设置成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "默认联系人设置失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
