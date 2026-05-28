import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { requireUserAccessToken } from "@/lib/server/auth"
import { mapRouteError } from "@/lib/server/api-errors"
import { gatewayRequest } from "@/lib/server/fetch-service"
import { changePasswordSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const payload = changePasswordSchema.parse(await request.json())
    await gatewayRequest<null>("/api/user/auth/password/change", {
      method: "POST",
      accessToken,
      body: payload
    })

    return NextResponse.json(buildSuccessResponse(null, requestId, "密码修改成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "密码修改参数不合法",
      dependencyMessage: "密码修改失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
