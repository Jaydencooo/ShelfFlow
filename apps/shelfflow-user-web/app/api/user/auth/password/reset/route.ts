import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import { resetPasswordSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const payload = resetPasswordSchema.parse(await request.json())
    await gatewayRequest<null>("/api/user/auth/password/reset", {
      method: "POST",
      body: payload
    })

    return NextResponse.json(buildSuccessResponse(null, requestId, "密码重置成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "密码重置参数不合法",
      dependencyMessage: "密码重置服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
