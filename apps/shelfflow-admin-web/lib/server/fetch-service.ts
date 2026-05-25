import { headers } from "next/headers"

import { AppError, errorCodes } from "@shelfflow/shared"

import { getServerEnv } from "@/lib/env"
import type { ApiEnvelope } from "@/lib/types"

interface GatewayRequestOptions {
  method?: "GET" | "POST" | "PUT" | "DELETE"
  body?: unknown
  accessToken?: string | null
}

function buildGatewayUrl(path: string): URL {
  const { SHELFFLOW_GATEWAY_BASE_URL } = getServerEnv()
  const normalizedBase = SHELFFLOW_GATEWAY_BASE_URL.endsWith("/")
    ? SHELFFLOW_GATEWAY_BASE_URL
    : `${SHELFFLOW_GATEWAY_BASE_URL}/`

  return new URL(path.startsWith("/") ? path.slice(1) : path, normalizedBase)
}

export async function gatewayRequest<T>(path: string, options: GatewayRequestOptions = {}): Promise<T> {
  const incomingHeaders = await headers()
  const requestId = incomingHeaders.get("x-request-id") ?? crypto.randomUUID()
  const response = await fetch(buildGatewayUrl(path), {
    method: options.method ?? "GET",
    headers: {
      "content-type": "application/json",
      "x-request-id": requestId,
      ...(options.accessToken ? { authorization: `Bearer ${options.accessToken}` } : {})
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
    cache: "no-store"
  })

  let payload: ApiEnvelope<T> | null

  try {
    payload = (await response.json()) as ApiEnvelope<T>
  } catch {
    payload = null
  }

  if (!response.ok || !payload || payload.code !== "ok") {
    throw new AppError(
      errorCodes.DEPENDENCY_ERROR,
      response.status >= 400 ? response.status : 502,
      payload?.message || "上游服务请求失败"
    )
  }

  return payload.data
}
