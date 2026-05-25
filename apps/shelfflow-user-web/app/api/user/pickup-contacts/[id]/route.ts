import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserPickupContact } from "@/lib/types"
import { pickupContactSchema } from "@/lib/validation"

export async function PUT(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    const payload = pickupContactSchema.parse(await request.json())
    const contact = await gatewayRequest<UserPickupContact>(`/api/user/pickup-contacts/${id}`, {
      method: "PUT",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(contact, requestId, "自提联系人更新成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "自提联系人参数不合法",
      dependencyMessage: "自提联系人更新失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function DELETE(request: Request, { params }: { params: Promise<{ id: string }> }) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const { id } = await params
    await gatewayRequest(`/api/user/pickup-contacts/${id}`, {
      method: "DELETE",
      accessToken
    })
    return NextResponse.json(buildSuccessResponse(null, requestId, "自提联系人删除成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "自提联系人删除失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
