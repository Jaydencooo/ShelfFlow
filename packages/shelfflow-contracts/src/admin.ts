import { z } from "zod"

const sortableFields = ["createdAt", "updatedAt", "expiryDate", "currentPrice"] as const

export const batchStatusSchema = z.enum(["draft", "active", "paused", "sold_out", "expired"])
export const pricingStatusSchema = z.enum(["pending", "active", "stale", "disabled"])
export const sortOrderSchema = z.enum(["asc", "desc"])
export const sortableFieldSchema = z.enum(sortableFields)

export const paginationQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  sortBy: sortableFieldSchema.default("updatedAt"),
  sortOrder: sortOrderSchema.default("desc")
})

export const adminLoginRequestSchema = z.object({
  username: z.string().trim().min(3).max(64),
  password: z.string().min(8).max(128)
})

export const adminSessionSchema = z.object({
  userId: z.string().min(1),
  username: z.string().min(1),
  displayName: z.string().min(1),
  roles: z.array(z.string()).default([]),
  permissions: z.array(z.string()).default([]),
  token: z.string().min(1),
  expiresAt: z.string().datetime().optional()
})

export const productUpsertSchema = z.object({
  id: z.string().min(1).optional(),
  name: z.string().trim().min(1).max(128),
  categoryId: z.string().min(1).max(64),
  price: z.coerce.number().positive(),
  image: z.string().url().optional().or(z.literal("")),
  description: z.string().trim().max(1024).optional().default(""),
  status: z.enum(["inactive", "active"]).default("active"),
  shelfLifeDays: z.coerce.number().int().min(1).max(3650),
  specs: z.array(z.object({
    name: z.string().trim().min(1).max(64),
    value: z.string().trim().min(1).max(256)
  })).default([])
})

export const inventoryBatchUpsertSchema = z.object({
  id: z.string().min(1).optional(),
  productId: z.string().min(1).max(64),
  batchCode: z.string().trim().min(1).max(64),
  productionDate: z.string().min(1),
  expiryDate: z.string().min(1),
  stockQuantity: z.coerce.number().int().min(1),
  basePrice: z.coerce.number().positive(),
  currentPrice: z.coerce.number().positive().optional(),
  batchStatus: batchStatusSchema.default("draft"),
  pricingStatus: pricingStatusSchema.default("pending")
}).superRefine((value, context) => {
  if (new Date(value.productionDate).getTime() > new Date(value.expiryDate).getTime()) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      message: "productionDate must be earlier than or equal to expiryDate",
      path: ["productionDate"]
    })
  }
})

export const inventoryBatchStatusUpdateSchema = z.object({
  batchStatus: batchStatusSchema
})

export const inventoryBatchQuerySchema = paginationQuerySchema.extend({
  keyword: z.string().trim().max(128).optional(),
  categoryId: z.string().max(64).optional(),
  batchStatus: batchStatusSchema.optional(),
  pricingStatus: pricingStatusSchema.optional(),
  expiryDaysMin: z.coerce.number().int().min(0).optional(),
  expiryDaysMax: z.coerce.number().int().min(0).optional()
}).superRefine((value, context) => {
  if (
    value.expiryDaysMin !== undefined &&
    value.expiryDaysMax !== undefined &&
    value.expiryDaysMin > value.expiryDaysMax
  ) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      message: "expiryDaysMin cannot be greater than expiryDaysMax",
      path: ["expiryDaysMin"]
    })
  }
})

export const productQuerySchema = paginationQuerySchema.extend({
  keyword: z.string().trim().max(128).optional(),
  categoryId: z.string().max(64).optional(),
  status: z.enum(["inactive", "active"]).optional()
})

export interface PaginatedResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface ProductRecord {
  id: string
  name: string
  categoryId: string
  categoryName?: string
  price: number
  image?: string
  description?: string
  status: "active" | "inactive"
  shelfLifeDays: number
  specs: Array<{ name: string; value: string }>
}

export interface InventoryBatchRecord {
  id: string
  productId: string
  productName: string
  categoryId: string
  batchCode: string
  productionDate: string
  expiryDate: string
  shelfLifeDays: number
  availableStock: number
  lockedStock: number
  soldStock: number
  wasteStock: number
  basePrice: number
  currentPrice: number
  batchStatus: BatchStatus
  pricingStatus: PricingStatus
}

export type AdminLoginRequest = z.infer<typeof adminLoginRequestSchema>
export type AdminSession = z.infer<typeof adminSessionSchema>
export type ProductUpsert = z.infer<typeof productUpsertSchema>
export type ProductQuery = z.infer<typeof productQuerySchema>
export type InventoryBatchUpsert = z.infer<typeof inventoryBatchUpsertSchema>
export type InventoryBatchQuery = z.infer<typeof inventoryBatchQuerySchema>
export type InventoryBatchStatusUpdate = z.infer<typeof inventoryBatchStatusUpdateSchema>
export type BatchStatus = z.infer<typeof batchStatusSchema>
export type PricingStatus = z.infer<typeof pricingStatusSchema>
