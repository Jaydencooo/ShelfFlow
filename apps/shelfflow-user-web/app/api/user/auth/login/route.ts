import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import { persistSession } from "@/lib/server/session"
import type { SessionUser, UserSession } from "@/lib/types"
import { loginSchema } from "@/lib/validation"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const rawPayload = (await request.json()) as Record<string, unknown>
    const payload = loginSchema.parse({
      account: typeof rawPayload.account === "string" ? rawPayload.account : rawPayload.openId,
      password: rawPayload.password
    })
    const session = await gatewayRequest<UserSession>("/api/user/auth/login", {
      method: "POST",
      body: {
        ...payload,
        openId: payload.account
      }
    })

    await persistSession(session)

    const user: SessionUser = {
      userId: session.userId,
      openId: session.openId,
      account: session.account,
      name: session.name,
      phone: session.phone,
      email: session.email
    }

    return NextResponse.json(buildSuccessResponse(user, requestId, "登录成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "登录参数不合法",
      dependencyMessage: "登录服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
