import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import { persistSession } from "@/lib/server/session"
import type { SessionUser, UserSession } from "@/lib/types"
import { registerSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const payload = registerSchema.parse(await request.json())
    const session = await gatewayRequest<UserSession>("/api/user/auth/register", {
      method: "POST",
      body: payload
    })

    await persistSession(session)

    const user: SessionUser = {
      userId: session.userId,
      openId: session.openId,
      name: session.name,
      phone: session.phone
    }

    return NextResponse.json(buildSuccessResponse(user, requestId, "注册成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "注册参数不合法",
      dependencyMessage: "注册服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
