import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import type { UserPickupContact } from "@/lib/types"
import { pickupContactSchema } from "@/lib/validation"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const contacts = await gatewayRequest<UserPickupContact[]>("/api/user/pickup-contacts", { accessToken })
    return NextResponse.json(buildSuccessResponse(contacts, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "自提联系人服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const payload = pickupContactSchema.parse(await request.json())
    const contact = await gatewayRequest<UserPickupContact>("/api/user/pickup-contacts", {
      method: "POST",
      accessToken,
      body: payload
    })
    return NextResponse.json(buildSuccessResponse(contact, requestId, "自提联系人创建成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "自提联系人参数不合法",
      dependencyMessage: "自提联系人创建失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
