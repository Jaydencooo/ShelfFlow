import { z } from "zod"

import { DEFAULT_PAGE_SIZE, ORDER_PAGE_SIZE } from "@/lib/constants"

export const loginSchema = z.object({
  openId: z.string().trim().min(4, "账号至少 4 个字符").max(64, "账号不能超过 64 个字符"),
  password: z.string().trim().min(8, "密码至少 8 个字符").max(32, "密码不能超过 32 个字符")
})

export const registerSchema = z.object({
  openId: z.string().trim().min(4, "账号至少 4 个字符").max(64, "账号不能超过 64 个字符"),
  name: z.string().trim().min(2, "昵称至少 2 个字符").max(32, "昵称不能超过 32 个字符"),
  phone: z.string().trim().regex(/^1\d{10}$/, "手机号格式不正确"),
  password: z.string().trim().min(8, "密码至少 8 个字符").max(32, "密码不能超过 32 个字符").regex(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@#$%^&*!._-]+$/, "密码至少包含字母和数字，且只允许常见安全字符")
})

export const resetPasswordSchema = z.object({
  openId: z.string().trim().min(4, "账号至少 4 个字符").max(64, "账号不能超过 64 个字符"),
  phone: z.string().trim().regex(/^1\d{10}$/, "手机号格式不正确"),
  newPassword: z.string().trim().min(8, "新密码至少 8 个字符").max(32, "新密码不能超过 32 个字符").regex(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@#$%^&*!._-]+$/, "新密码至少包含字母和数字，且只允许常见安全字符")
})

export const profileUpdateSchema = z.object({
  name: z.string().trim().min(2, "昵称至少 2 个字符").max(32, "昵称不能超过 32 个字符"),
  phone: z.string().trim().regex(/^1\d{10}$/, "手机号格式不正确")
})

export const pickupContactSchema = z.object({
  consignee: z.string().trim().min(2, "联系人至少 2 个字符").max(32, "联系人不能超过 32 个字符"),
  phone: z.string().trim().regex(/^1\d{10}$/, "手机号格式不正确"),
  label: z.string().trim().max(16, "标签不能超过 16 个字符").optional().or(z.literal("")),
  detail: z.string().trim().max(120, "备注不能超过 120 个字符").optional().or(z.literal("")),
  defaultContact: z.boolean().optional()
})

export const catalogQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(50).default(DEFAULT_PAGE_SIZE),
  keyword: z.string().trim().max(50).optional(),
  categoryId: z.string().trim().optional(),
  sortBy: z.enum(["updatedAt", "price", "daysToExpire"]).default("updatedAt"),
  sortOrder: z.enum(["asc", "desc"]).default("desc")
})

export const addCartItemSchema = z.object({
  productId: z.string().trim().min(1, "商品 ID 不能为空"),
  quantity: z.coerce.number().int().min(1).max(99)
})

export const updateCartItemQuantitySchema = z.object({
  quantity: z.coerce.number().int().min(1).max(99)
})

export const submitOrderSchema = z.object({
  remark: z.string().trim().max(100, "备注不能超过 100 个字符").optional().or(z.literal("")),
  pickupContactId: z.string().trim().optional().or(z.literal(""))
})

export const cancelOrderSchema = z.object({
  cancelReason: z.string().trim().max(100, "取消原因不能超过 100 个字符").optional().or(z.literal(""))
})

export const orderQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(50).default(ORDER_PAGE_SIZE),
  status: z.enum(["pending_payment", "to_prepare", "preparing", "ready_for_pickup", "completed", "cancelled", "refunded"]).optional(),
  sortBy: z.enum(["orderTime", "amount"]).default("orderTime"),
  sortOrder: z.enum(["asc", "desc"]).default("desc")
})

export type LoginFormValues = z.infer<typeof loginSchema>
