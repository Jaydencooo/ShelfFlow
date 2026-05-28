import { headers } from "next/headers"

import { AppError, type ErrorCode, errorCodes } from "@shelfflow/shared"

import { getServerEnv } from "@/lib/env"
import type { ApiEnvelope } from "@/lib/types"

interface GatewayRequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
  body?: unknown
  accessToken?: string | null
}

type GatewayEnvelope<T> = ApiEnvelope<T> | {
  code?: string
  message?: string
  data?: unknown
  details?: unknown
}

const gatewayErrorStatusByCode: Partial<Record<ErrorCode, number>> = {
  [errorCodes.VALIDATION_ERROR]: 400,
  [errorCodes.UNAUTHORIZED]: 401,
  [errorCodes.FORBIDDEN]: 403,
  [errorCodes.NOT_FOUND]: 404,
  [errorCodes.CONFLICT]: 409,
  [errorCodes.RATE_LIMITED]: 429,
  [errorCodes.INTERNAL_ERROR]: 500,
  [errorCodes.DEPENDENCY_ERROR]: 502
}

function isErrorCode(code: unknown): code is ErrorCode {
  return typeof code === "string" && Object.values(errorCodes).includes(code as ErrorCode)
}

function getGatewayErrorDetails(payload: GatewayEnvelope<unknown> | null) {
  return payload && "details" in payload ? payload.details : undefined
}

function isSuccessEnvelope<T>(payload: GatewayEnvelope<T> | null): payload is ApiEnvelope<T> {
  return Boolean(payload && payload.code === "ok" && "data" in payload)
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

  let payload: GatewayEnvelope<T> | null = null

  try {
    payload = (await response.json()) as GatewayEnvelope<T>
  } catch {
    payload = null
  }

  if (!response.ok || !isSuccessEnvelope(payload)) {
    const code = isErrorCode(payload?.code) ? payload.code : errorCodes.DEPENDENCY_ERROR
    const statusCode = response.status >= 400
      ? response.status
      : gatewayErrorStatusByCode[code] ?? 502

    throw new AppError(
      code,
      statusCode,
      payload?.message || "上游服务请求失败",
      getGatewayErrorDetails(payload)
    )
  }

  return payload.data
}
