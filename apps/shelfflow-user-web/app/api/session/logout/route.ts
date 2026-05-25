import { NextResponse } from "next/server"

import { buildSuccessResponse } from "@shelfflow/shared"

import { clearSession } from "@/lib/server/session"

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()
  await clearSession()
  return NextResponse.json(buildSuccessResponse(null, requestId, "退出成功"))
}
