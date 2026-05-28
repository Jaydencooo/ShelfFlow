"use client"

import type {
  ApiEnvelope,
  PaginatedResult,
  PickupPoint,
  SessionUser,
  UserCartItem,
  UserCatalogCategory,
  UserCatalogProduct,
  UserCatalogProductDetail,
  UserCatalogProductQuery,
  UserLoginRequest,
  UserPasswordChangeRequest,
  UserPasswordResetRequest,
  UserPickupContact,
  UserPickupContactRequest,
  UserProfileUpdateRequest,
  UserOrderDetail,
  UserOrderCancelRequest,
  UserOrderQuery,
  UserOrderSubmitRequest,
  UserOrderSubmitResult,
  UserOrderSummary,
  UserRegisterRequest,
  UserVerificationCodeRequest,
  UserVerificationCodeResponse
} from "@/lib/types"

export class ApiClientError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string
  ) {
    super(message)
    this.name = "ApiClientError"
  }
}

export function isUnauthorizedError(error: unknown) {
  return error instanceof ApiClientError && (error.status === 401 || error.code === "unauthorized")
}

function isSuccessEnvelope<T>(payload: ApiEnvelope<T> | { code?: string; message?: string } | null): payload is ApiEnvelope<T> {
  return Boolean(payload && "code" in payload && payload.code === "ok" && "data" in payload)
}

async function parseEnvelope<T>(response: Response): Promise<T> {
  let payload: ApiEnvelope<T> | { code?: string; message?: string } | null = null

  try {
    payload = (await response.json()) as ApiEnvelope<T> | { code?: string; message?: string }
  } catch {
    payload = null
  }

  if (!response.ok || !isSuccessEnvelope<T>(payload)) {
    throw new ApiClientError(payload?.message || "请求失败", response.status, payload?.code)
  }

  return payload.data
}

function buildQuery<T extends Record<string, string | number | undefined>>(params: T) {
  const searchParams = new URLSearchParams()

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value))
    }
  }

  return searchParams.toString()
}

export async function loginRequest(payload: UserLoginRequest) {
  const response = await fetch("/api/user/auth/login", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<SessionUser>(response)
}

export async function registerRequest(payload: UserRegisterRequest) {
  const response = await fetch("/api/user/auth/register", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<SessionUser>(response)
}

export async function sendVerificationCodeRequest(payload: UserVerificationCodeRequest) {
  const response = await fetch("/api/user/auth/verification-code", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<UserVerificationCodeResponse>(response)
}

export async function resetPasswordRequest(payload: UserPasswordResetRequest) {
  const response = await fetch("/api/user/auth/password/reset", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<null>(response)
}

export async function changePasswordRequest(payload: UserPasswordChangeRequest) {
  const response = await fetch("/api/user/auth/password/change", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<null>(response)
}

export async function logoutRequest() {
  const response = await fetch("/api/session/logout", { method: "POST" })
  return parseEnvelope<null>(response)
}

export async function getSessionRequest() {
  const response = await fetch("/api/user/auth/me", { cache: "no-store" })
  return parseEnvelope<SessionUser>(response)
}

export async function updateProfileRequest(payload: UserProfileUpdateRequest) {
  const response = await fetch("/api/user/auth/me", {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<SessionUser>(response)
}

export async function getPickupContacts() {
  const response = await fetch("/api/user/pickup-contacts", { cache: "no-store" })
  return parseEnvelope<UserPickupContact[]>(response)
}

export async function getPickupPoints() {
  const response = await fetch("/api/user/pickup-points", { cache: "no-store" })
  return parseEnvelope<PickupPoint[]>(response)
}

export async function createPickupContact(payload: UserPickupContactRequest) {
  const response = await fetch("/api/user/pickup-contacts", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<UserPickupContact>(response)
}

export async function updatePickupContact(id: string, payload: UserPickupContactRequest) {
  const response = await fetch(`/api/user/pickup-contacts/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<UserPickupContact>(response)
}

export async function setDefaultPickupContact(id: string) {
  const response = await fetch(`/api/user/pickup-contacts/${id}/default`, { method: "PATCH" })
  return parseEnvelope<UserPickupContact>(response)
}

export async function deletePickupContact(id: string) {
  const response = await fetch(`/api/user/pickup-contacts/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function getCategories() {
  const response = await fetch("/api/user/catalog/categories", { cache: "no-store" })
  return parseEnvelope<UserCatalogCategory[]>(response)
}

export async function getProducts(query: UserCatalogProductQuery) {
  const response = await fetch(`/api/user/catalog/products?${buildQuery({
    page: query.page,
    pageSize: query.pageSize,
    keyword: query.keyword,
    categoryId: query.categoryId,
    sortBy: query.sortBy,
    sortOrder: query.sortOrder
  })}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<UserCatalogProduct>>(response)
}

export async function getProductDetail(id: string) {
  const response = await fetch(`/api/user/catalog/products/${id}`, { cache: "no-store" })
  return parseEnvelope<UserCatalogProductDetail>(response)
}

export async function getCartItems() {
  const response = await fetch("/api/user/cart/items", { cache: "no-store" })
  return parseEnvelope<UserCartItem[]>(response)
}

export async function addCartItem(payload: { productId: string; quantity: number }) {
  const response = await fetch("/api/user/cart/items", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<null>(response)
}

export async function removeCartItem(id: string) {
  const response = await fetch(`/api/user/cart/items/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function clearCartItems() {
  const response = await fetch("/api/user/cart/items", { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function updateCartItemQuantity(id: string, quantity: number) {
  const response = await fetch(`/api/user/cart/items/${id}`, {
    method: "PATCH",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ quantity })
  })
  return parseEnvelope<null>(response)
}

export async function submitOrder(payload: UserOrderSubmitRequest) {
  const response = await fetch("/api/user/orders", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<UserOrderSubmitResult>(response)
}

export async function getOrders(query: UserOrderQuery) {
  const response = await fetch(`/api/user/orders?${buildQuery({
    page: query.page,
    pageSize: query.pageSize,
    status: query.status,
    sortBy: query.sortBy,
    sortOrder: query.sortOrder
  })}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<UserOrderSummary>>(response)
}

export async function getOrderDetail(id: string) {
  const response = await fetch(`/api/user/orders/${id}`, { cache: "no-store" })
  return parseEnvelope<UserOrderDetail>(response)
}

export async function cancelOrder(id: string, payload: UserOrderCancelRequest = {}) {
  const response = await fetch(`/api/user/orders/${id}`, {
    method: "DELETE",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<null>(response)
}

export async function payOrder(id: string) {
  const response = await fetch(`/api/user/orders/${id}/pay`, { method: "POST" })
  return parseEnvelope<UserOrderDetail>(response)
}
