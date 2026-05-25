import { z } from "zod"

const serverEnvSchema = z.object({
  SHELFFLOW_GATEWAY_BASE_URL: z.string().url().default("http://127.0.0.1:4010"),
  NODE_ENV: z.enum(["development", "production", "test"]).default("development")
})

export type ServerEnv = z.infer<typeof serverEnvSchema>

let cachedServerEnv: ServerEnv | null = null

export function getServerEnv(): ServerEnv {
  if (cachedServerEnv) {
    return cachedServerEnv
  }

  cachedServerEnv = serverEnvSchema.parse({
    SHELFFLOW_GATEWAY_BASE_URL: process.env.SHELFFLOW_GATEWAY_BASE_URL,
    NODE_ENV: process.env.NODE_ENV
  })

  return cachedServerEnv
}
