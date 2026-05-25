import { z } from "zod"

const productStatusSchema = z.enum(["active", "inactive"])
export const batchStatusSchema = z.enum(["draft", "active", "paused", "sold_out", "expired"])
const pricingStatusSchema = z.enum(["pending", "active", "stale", "disabled"])
const pricingRuleStatusSchema = z.enum(["enabled", "disabled"])
const sortOrderSchema = z.enum(["asc", "desc"])
export const adminOrderStatusSchema = z.enum([
  "pending_payment",
  "to_prepare",
  "preparing",
  "ready_for_pickup",
  "completed",
  "cancelled",
  "refunded"
])
const adminOrderPayStatusSchema = z.enum(["unpaid", "paid", "refunded"])

export const loginSchema = z.object({
  username: z.string().trim().min(3, "请输入至少 3 位账号"),
  password: z.string().min(6, "请输入至少 6 位密码")
})

export const productSchema = z.object({
  name: z.string().trim().min(2, "请输入商品名称").max(32, "商品名称不能超过 32 个字符"),
  categoryId: z.string().trim().min(1, "请选择商品分类").max(64, "商品分类不能超过 64 位"),
  price: z.coerce.number().positive("请输入大于 0 的售价"),
  description: z.string().trim().max(255, "描述不能超过 255 个字符").optional().or(z.literal("")),
  image: z.string().trim().max(255, "图片地址不能超过 255 个字符").optional().or(z.literal("")),
  status: productStatusSchema.default("active"),
  shelfLifeDays: z.coerce.number().int().min(1, "保质期必须大于 0").max(3650, "保质期不能超过 3650 天").default(7)
})

export const productCategorySchema = z.object({
  name: z.string().trim().min(1, "分类名称不能为空").max(32, "分类名称不能超过 32 个字符")
})

export const batchSchema = z.object({
  productId: z.string().trim().min(1, "请选择商品"),
  batchCode: z.string().trim().min(4, "批次号至少 4 位").max(64, "批次号不能超过 64 位"),
  productionDate: z.string().min(1, "请选择生产时间"),
  expiryDate: z.string().min(1, "请选择过期时间"),
  stockQuantity: z.coerce.number().int().positive("入库数量必须大于 0"),
  lockedQuantity: z.coerce.number().int().min(0).default(0),
  soldQuantity: z.coerce.number().int().min(0).default(0),
  basePrice: z.coerce.number().positive("基准价必须大于 0"),
  batchStatus: batchStatusSchema.default("active"),
  pricingStatus: pricingStatusSchema.default("active")
}).superRefine((value, context) => {
  const production = new Date(value.productionDate).getTime()
  const expiration = new Date(value.expiryDate).getTime()

  if (!Number.isNaN(production) && !Number.isNaN(expiration) && expiration <= production) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      message: "过期时间必须晚于生产时间",
      path: ["expiryDate"]
    })
  }
})

export const productQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  keyword: z.string().trim().max(128).optional(),
  categoryId: z.string().trim().max(64).optional(),
  status: productStatusSchema.optional(),
  sortBy: z.string().trim().max(64).default("updatedAt"),
  sortOrder: sortOrderSchema.default("desc")
})

export const batchQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  keyword: z.string().trim().max(128).optional(),
  categoryId: z.string().trim().max(64).optional(),
  batchStatus: batchStatusSchema.optional(),
  pricingStatus: pricingStatusSchema.optional(),
  expiryDaysMin: z.coerce.number().int().min(0).optional(),
  expiryDaysMax: z.coerce.number().int().min(0).optional(),
  sortBy: z.string().trim().max(64).default("updatedAt"),
  sortOrder: sortOrderSchema.default("desc")
})

export const adminOrderQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  keyword: z.string().trim().max(128).optional(),
  status: adminOrderStatusSchema.optional(),
  payStatus: adminOrderPayStatusSchema.optional(),
  sortBy: z.string().trim().max(64).default("orderTime"),
  sortOrder: sortOrderSchema.default("desc")
})

export const pricingRuleSchema = z.object({
  name: z.string().trim().min(2, "请输入规则名称").max(64, "规则名称不能超过 64 个字符"),
  minDaysToExpire: z.coerce.number().int().min(0, "最小天数不能小于 0").max(3650, "天数不能超过 3650"),
  maxDaysToExpire: z.coerce.number().int().min(0, "最大天数不能小于 0").max(3650, "天数不能超过 3650"),
  discountRate: z.coerce.number().min(0.01, "折扣率不能小于 0.01").max(1, "折扣率不能大于 1"),
  priority: z.coerce.number().int().min(0, "优先级不能小于 0").max(10000, "优先级不能超过 10000"),
  status: pricingRuleStatusSchema.default("enabled")
}).superRefine((value, context) => {
  if (value.minDaysToExpire > value.maxDaysToExpire) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      message: "最小天数不能大于最大天数",
      path: ["maxDaysToExpire"]
    })
  }
})

export const pricingRuleQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  keyword: z.string().trim().max(128).optional(),
  status: pricingRuleStatusSchema.optional(),
  sortBy: z.string().trim().max(64).default("updatedAt"),
  sortOrder: sortOrderSchema.default("desc")
})

export const aiKnowledgeSchema = z.object({
  title: z.string().trim().min(1, "标题不能为空").max(80, "标题不能超过 80 个字符"),
  category: z.string().trim().min(1, "分类不能为空").max(32, "分类不能超过 32 个字符"),
  content: z.string().trim().min(1, "内容不能为空").max(4000, "内容不能超过 4000 个字符")
})

export const aiKnowledgeQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(10),
  keyword: z.string().trim().max(128).optional(),
  category: z.string().trim().max(32).optional(),
  sortBy: z.string().trim().max(64).default("updatedAt"),
  sortOrder: sortOrderSchema.default("desc")
})

export const aiChatSchema = z.object({
  sessionId: z.string().trim().max(64, "会话 ID 不能超过 64 位").optional(),
  message: z.string().trim().min(1, "问题不能为空").max(1000, "问题不能超过 1000 个字符")
})

export type LoginFormValues = z.infer<typeof loginSchema>
export type ProductFormValues = z.infer<typeof productSchema>
export type ProductCategoryFormValues = z.infer<typeof productCategorySchema>
export type BatchFormValues = z.infer<typeof batchSchema>
export type PricingRuleFormValues = z.infer<typeof pricingRuleSchema>
export type AiKnowledgeFormValues = z.infer<typeof aiKnowledgeSchema>
