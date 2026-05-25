import { NextResponse } from "next/server"

import { buildErrorResponse, buildSuccessResponse } from "@shelfflow/shared"

import { gatewayRequest } from "@/lib/server/fetch-service"
import { mapRouteError } from "@/lib/server/api-errors"
import { requireUserAccessToken } from "@/lib/server/auth"
import { persistSession } from "@/lib/server/session"
import type { SessionUser, UserSession } from "@/lib/types"
import { profileUpdateSchema } from "@/lib/validation"

export async function GET(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const session = await gatewayRequest<UserSession>("/api/user/auth/me", { accessToken })

    const user: SessionUser = {
      userId: session.userId,
      openId: session.openId,
      name: session.name,
      phone: session.phone
    }

    return NextResponse.json(buildSuccessResponse(user, requestId))
  } catch (error) {
    const appError = mapRouteError(error, {
      dependencyMessage: "用户会话服务暂时不可用"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}

export async function PUT(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    const accessToken = await requireUserAccessToken()
    const payload = profileUpdateSchema.parse(await request.json())
    const session = await gatewayRequest<UserSession>("/api/user/auth/me", {
      method: "PUT",
      accessToken,
      body: payload
    })

    await persistSession(session)

    const user: SessionUser = {
      userId: session.userId,
      openId: session.openId,
      name: session.name,
      phone: session.phone
    }

    return NextResponse.json(buildSuccessResponse(user, requestId, "资料更新成功"))
  } catch (error) {
    const appError = mapRouteError(error, {
      validationMessage: "用户资料参数不合法",
      dependencyMessage: "用户资料更新失败"
    })

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
