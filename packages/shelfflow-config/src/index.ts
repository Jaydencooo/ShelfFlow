import { z } from "zod"

export const appEnvironmentSchema = z.enum(["local", "development", "test", "production"])

export const baseServiceConfigSchema = z.object({
  APP_NAME: z.string().min(1),
  APP_PORT: z.coerce.number().int().min(1).max(65535),
  APP_ENV: appEnvironmentSchema,
  LOG_LEVEL: z.enum(["fatal", "error", "warn", "info", "debug", "trace"]),
  API_PREFIX: z.string().min(1),
  CORS_ALLOWED_ORIGINS: z.string().min(1)
})

export const authConfigSchema = baseServiceConfigSchema.extend({
  JWT_SECRET: z.string().min(32),
  JWT_EXPIRES_IN: z.string().min(2),
  LEGACY_BACKEND_BASE_URL: z.string().url()
})

export const adminServiceConfigSchema = baseServiceConfigSchema.extend({
  LEGACY_BACKEND_BASE_URL: z.string().url(),
  FORWARD_AUTH_HEADER: z.enum(["true", "false"]).default("true")
})

export const gatewayConfigSchema = baseServiceConfigSchema.extend({
  AUTH_SERVICE_BASE_URL: z.string().url(),
  ADMIN_SERVICE_BASE_URL: z.string().url(),
  USER_SERVICE_BASE_URL: z.string().url()
})

export function parseNodeConfig<T extends z.ZodRawShape>(
  schema: z.ZodObject<T>,
  source: NodeJS.ProcessEnv = process.env
): z.infer<z.ZodObject<T>> {
  return schema.parse(source)
}

export function parseCsvEnv(value: string): string[] {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean)
}
