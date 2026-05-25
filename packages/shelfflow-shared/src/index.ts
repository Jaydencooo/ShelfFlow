import pino, { type LoggerOptions } from "pino"

export const REQUEST_ID_HEADER = "x-request-id"

export const errorCodes = {
  VALIDATION_ERROR: "validation_error",
  UNAUTHORIZED: "unauthorized",
  FORBIDDEN: "forbidden",
  NOT_FOUND: "not_found",
  CONFLICT: "conflict",
  RATE_LIMITED: "rate_limited",
  INTERNAL_ERROR: "internal_error",
  DEPENDENCY_ERROR: "dependency_error"
} as const

export type ErrorCode = (typeof errorCodes)[keyof typeof errorCodes]

export interface ApiSuccessResponse<T> {
  code: "ok"
  message: string
  data: T
  requestId: string
  timestamp: string
}

export interface ApiErrorResponse {
  code: ErrorCode
  message: string
  details?: unknown
  requestId: string
  timestamp: string
}

export class AppError extends Error {
  public readonly code: ErrorCode
  public readonly statusCode: number
  public readonly details?: unknown

  public constructor(code: ErrorCode, statusCode: number, message: string, details?: unknown) {
    super(message)
    this.code = code
    this.statusCode = statusCode
    this.details = details
  }
}

export function createLogger(name: string, options?: LoggerOptions) {
  return pino({
    name,
    level: process.env.LOG_LEVEL ?? "info",
    base: undefined,
    redact: {
      paths: ["req.headers.authorization", "password", "token"],
      censor: "[redacted]"
    },
    ...options
  })
}

export function buildSuccessResponse<T>(data: T, requestId: string, message = "OK"): ApiSuccessResponse<T> {
  return {
    code: "ok",
    message,
    data,
    requestId,
    timestamp: new Date().toISOString()
  }
}

export function buildErrorResponse(error: AppError, requestId: string): ApiErrorResponse {
  return {
    code: error.code,
    message: error.message,
    details: error.details,
    requestId,
    timestamp: new Date().toISOString()
  }
}
