import { AppError, errorCodes } from "@shelfflow/shared"
import { ZodError } from "zod"

export function mapRouteError(
  error: unknown,
  fallback: {
    validationMessage?: string
    dependencyMessage?: string
    unauthorizedMessage?: string
    notFoundMessage?: string
    conflictMessage?: string
  }
) {
  if (error instanceof AppError) {
    return error
  }

  if (error instanceof ZodError) {
    return new AppError(
      errorCodes.VALIDATION_ERROR,
      400,
      fallback.validationMessage ?? "请求参数不合法"
    )
  }

  if (error instanceof TypeError) {
    return new AppError(
      errorCodes.DEPENDENCY_ERROR,
      502,
      fallback.dependencyMessage ?? "上游服务暂时不可用"
    )
  }

  return new AppError(
    errorCodes.INTERNAL_ERROR,
    500,
    fallback.dependencyMessage ?? "请求处理失败"
  )
}
