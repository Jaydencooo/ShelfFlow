import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import type { UserVerificationCodeResponse } from "@/lib/types"
import { verificationCodeRequestSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const payload = verificationCodeRequestSchema.parse(await request.json())
    const result = await gatewayRequest<UserVerificationCodeResponse>("/api/user/auth/verification-code", {
      method: "POST",
      body: {
        ...payload,
        openId: payload.account
      }
    })

    return NextResponse.json(buildSuccessResponse(result, requestId, "验证码已发送"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "验证码请求参数不合法",
      dependencyMessage: "验证码服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
