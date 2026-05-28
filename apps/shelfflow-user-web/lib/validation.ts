import { z } from "zod"

import { DEFAULT_PAGE_SIZE, ORDER_PAGE_SIZE } from "@/lib/constants"

export const accountSchema = z.string().trim().min(5, "请输入手机号或邮箱").max(100, "账号不能超过 100 个字符").refine(
  (value) => /^1\d{10}$/.test(value) || /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value),
  "请输入有效的手机号或邮箱"
)

const passwordSchema = z.string().trim().min(8, "密码至少 8 个字符").max(32, "密码不能超过 32 个字符").regex(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@#$%^&*!._-]+$/, "密码至少包含字母和数字，且只允许常见安全字符")

export const loginSchema = z.object({
  account: accountSchema,
  password: passwordSchema
})

export const registerSchema = z.object({
  account: accountSchema,
  name: z.string().trim().min(2, "昵称至少 2 个字符").max(32, "昵称不能超过 32 个字符"),
  password: passwordSchema,
  confirmPassword: passwordSchema,
  verificationCode: z.string().trim().regex(/^\d{4,10}$/, "验证码格式不正确")
}).refine((value) => value.password === value.confirmPassword, {
  message: "两次输入的密码不一致",
  path: ["confirmPassword"]
})

export const resetPasswordSchema = z.object({
  account: accountSchema,
  newPassword: passwordSchema,
  confirmPassword: passwordSchema,
  verificationCode: z.string().trim().regex(/^\d{4,10}$/, "验证码格式不正确")
}).refine((value) => value.newPassword === value.confirmPassword, {
  message: "两次输入的密码不一致",
  path: ["confirmPassword"]
})

export const changePasswordSchema = z.object({
  currentPassword: passwordSchema,
  newPassword: passwordSchema,
  confirmPassword: passwordSchema
}).refine((value) => value.newPassword === value.confirmPassword, {
  message: "两次输入的密码不一致",
  path: ["confirmPassword"]
}).refine((value) => value.currentPassword !== value.newPassword, {
  message: "新密码不能与当前密码相同",
  path: ["newPassword"]
})

export const profileUpdateSchema = z.object({
  name: z.string().trim().min(2, "昵称至少 2 个字符").max(32, "昵称不能超过 32 个字符"),
  phone: z.string().trim().regex(/^1\d{10}$/, "手机号格式不正确").optional().or(z.literal("")),
  email: z.string().trim().email("邮箱格式不正确").max(100, "邮箱不能超过 100 个字符").optional().or(z.literal("")),
  phoneVerificationCode: z.string().trim().regex(/^\d{4,10}$/, "手机号验证码格式不正确").optional().or(z.literal("")),
  emailVerificationCode: z.string().trim().regex(/^\d{4,10}$/, "邮箱验证码格式不正确").optional().or(z.literal(""))
})

export const verificationCodeRequestSchema = z.object({
  account: accountSchema,
  purpose: z.enum(["register", "reset_password", "change_phone", "change_email"])
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
  pickupContactId: z.string().trim().optional().or(z.literal("")),
  pickupPointId: z.string().trim().optional().or(z.literal("")),
  cartItemIds: z.array(z.string().trim().min(1, "购物车商品 ID 不能为空")).min(1, "请选择要结算的商品").max(100, "单次结算商品过多").optional()
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
