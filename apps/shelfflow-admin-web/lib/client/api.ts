"use client"

import type {
  AdminAiKnowledge,
  AdminAiKnowledgeQuery,
  AdminAiKnowledgeUpsert,
  AdminAiOpsChatMessage,
  AdminAiOpsChatResponse,
  AdminAiOpsSuggestionAction,
  AdminAiOpsSuggestion,
  AdminOperationLog,
  AdminOperationLogQuery,
  AdminProductCategory,
  AdminProductCategoryUpsert,
  AdminLoginRequest,
  AdminLossStatsOverview,
  AdminOrderDetail,
  AdminOrderPickupVerifyRequest,
  AdminOrderQuery,
  AdminOrderStatus,
  AdminOrderSummary,
  AdminPricingRule,
  AdminPricingRuleQuery,
  AdminPricingRuleUpsert,
  AdminPricingSuggestion,
  ApiEnvelope,
  InventoryBatchQuery,
  InventoryBatchRecord,
  InventoryBatchUpsert,
  PaginatedResult,
  PickupPoint,
  PickupPointUpsert,
  ProductQuery,
  ProductRecord,
  ProductUpsert,
  SessionUser
} from "@/lib/types"

export class ApiClientError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
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
  let payload: ApiEnvelope<T> | { code?: string; message?: string } | null

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

function buildQuery(params: Iterable<[string, string | number | undefined]> | Record<string, string | number | undefined>) {
  const searchParams = new URLSearchParams()

  const entries = Symbol.iterator in Object(params)
    ? Array.from(params as Iterable<[string, string | number | undefined]>)
    : Object.entries(params)

  for (const [key, value] of entries) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value))
    }
  }

  return searchParams
}

export async function loginRequest(payload: AdminLoginRequest) {
  const response = await fetch("/api/admin/auth/login", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<SessionUser>(response)
}

export async function logoutRequest() {
  const response = await fetch("/api/session/logout", { method: "POST" })
  return parseEnvelope<null>(response)
}

export async function getSessionRequest() {
  const response = await fetch("/api/admin/auth/me", { cache: "no-store" })
  return parseEnvelope<SessionUser>(response)
}

export async function getProducts(query: ProductQuery) {
  const response = await fetch(`/api/admin/products?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<ProductRecord>>(response)
}

export async function getAdminProductCategories() {
  const response = await fetch("/api/admin/products/categories", { cache: "no-store" })
  return parseEnvelope<AdminProductCategory[]>(response)
}

export async function createAdminProductCategory(payload: AdminProductCategoryUpsert) {
  const response = await fetch("/api/admin/products/categories", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminProductCategory>(response)
}

export async function deleteAdminProductCategory(id: string) {
  const response = await fetch(`/api/admin/products/categories/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function getAdminPickupPoints() {
  const response = await fetch("/api/admin/pickup-points", { cache: "no-store" })
  return parseEnvelope<PickupPoint[]>(response)
}

export async function createAdminPickupPoint(payload: PickupPointUpsert) {
  const response = await fetch("/api/admin/pickup-points", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<PickupPoint>(response)
}

export async function updateAdminPickupPoint(id: string, payload: PickupPointUpsert) {
  const response = await fetch(`/api/admin/pickup-points/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<PickupPoint>(response)
}

export async function deleteAdminPickupPoint(id: string) {
  const response = await fetch(`/api/admin/pickup-points/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function createProduct(payload: ProductUpsert) {
  const response = await fetch("/api/admin/products", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<null>(response)
}

export async function updateProduct(id: string, payload: ProductUpsert) {
  const response = await fetch(`/api/admin/products/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<null>(response)
}

export async function deleteProduct(id: string) {
  const response = await fetch(`/api/admin/products/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function uploadProductImage(file: File) {
  const formData = new FormData()
  formData.set("image", file)

  const response = await fetch("/api/admin/uploads/product-image", {
    method: "POST",
    body: formData
  })

  return parseEnvelope<{ url: string }>(response)
}

export async function getInventoryBatches(query: InventoryBatchQuery) {
  const response = await fetch(`/api/admin/inventory-batches?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<InventoryBatchRecord>>(response)
}

export async function createInventoryBatch(payload: InventoryBatchUpsert) {
  const response = await fetch("/api/admin/inventory-batches", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<null>(response)
}

export async function updateInventoryBatch(id: string, payload: InventoryBatchUpsert) {
  const response = await fetch(`/api/admin/inventory-batches/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<null>(response)
}

export async function updateInventoryBatchStatus(id: string, batchStatus: InventoryBatchRecord["batchStatus"]) {
  const response = await fetch(`/api/admin/inventory-batches/${id}/status`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ batchStatus })
  })

  return parseEnvelope<null>(response)
}

export async function deleteInventoryBatch(id: string) {
  const response = await fetch(`/api/admin/inventory-batches/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function getAdminOrders(query: AdminOrderQuery) {
  const response = await fetch(`/api/admin/orders?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<AdminOrderSummary>>(response)
}

export async function getAdminOrderDetail(id: string) {
  const response = await fetch(`/api/admin/orders/${id}`, { cache: "no-store" })
  return parseEnvelope<AdminOrderDetail>(response)
}

export async function updateAdminOrderStatus(id: string, orderStatus: AdminOrderStatus) {
  const response = await fetch(`/api/admin/orders/${id}/status`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ orderStatus })
  })

  return parseEnvelope<AdminOrderDetail>(response)
}

export async function verifyAdminOrderPickup(id: string, payload: AdminOrderPickupVerifyRequest) {
  const response = await fetch(`/api/admin/orders/${id}/pickup-verification`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminOrderDetail>(response)
}

export async function getAdminPricingRules(query: AdminPricingRuleQuery) {
  const response = await fetch(`/api/admin/pricing-rules?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<AdminPricingRule>>(response)
}

export async function createAdminPricingRule(payload: AdminPricingRuleUpsert) {
  const response = await fetch("/api/admin/pricing-rules", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminPricingRule>(response)
}

export async function updateAdminPricingRule(id: string, payload: AdminPricingRuleUpsert) {
  const response = await fetch(`/api/admin/pricing-rules/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminPricingRule>(response)
}

export async function updateAdminPricingRuleStatus(id: string, status: AdminPricingRule["status"]) {
  const response = await fetch(`/api/admin/pricing-rules/${id}/status`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ status })
  })

  return parseEnvelope<AdminPricingRule>(response)
}

export async function deleteAdminPricingRule(id: string) {
  const response = await fetch(`/api/admin/pricing-rules/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function getAdminPricingSuggestions() {
  const response = await fetch("/api/admin/pricing-rules/suggestions", { cache: "no-store" })
  return parseEnvelope<AdminPricingSuggestion[]>(response)
}

export async function acceptAdminPricingSuggestion(batchId: string) {
  const response = await fetch(`/api/admin/pricing-rules/suggestions/${batchId}/accept`, {
    method: "POST",
    headers: { "content-type": "application/json" }
  })

  return parseEnvelope<AdminPricingRule>(response)
}

export async function getAdminLossStatsOverview() {
  const response = await fetch("/api/admin/loss-stats/overview", { cache: "no-store" })
  return parseEnvelope<AdminLossStatsOverview>(response)
}

export async function getAdminAiOpsSuggestions() {
  const response = await fetch("/api/admin/ai-ops/suggestions", { cache: "no-store" })
  return parseEnvelope<AdminAiOpsSuggestion[]>(response)
}

export async function updateAdminAiOpsSuggestionAction(id: string, payload: { action: "execute" | "ignore"; batchStatus?: string; operationNote?: string }) {
  const response = await fetch(`/api/admin/ai-ops/suggestions/${id}/action`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })
  return parseEnvelope<null>(response)
}

export async function getAdminAiOpsSuggestionActions() {
  const response = await fetch("/api/admin/ai-ops/suggestions/actions", { cache: "no-store" })
  return parseEnvelope<AdminAiOpsSuggestionAction[]>(response)
}

export async function getAdminOperationLogs(limit = 10) {
  const response = await fetch(`/api/admin/operation-logs?${buildQuery({ limit }).toString()}`, { cache: "no-store" })
  return parseEnvelope<AdminOperationLog[]>(response)
}

export async function getAdminOperationLogPage(query: AdminOperationLogQuery) {
  const response = await fetch(`/api/admin/operation-logs/page?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<AdminOperationLog>>(response)
}

export async function getAdminAiKnowledge(query: AdminAiKnowledgeQuery) {
  const response = await fetch(`/api/admin/ai-ops/knowledge?${buildQuery(Object.entries(query)).toString()}`, { cache: "no-store" })
  return parseEnvelope<PaginatedResult<AdminAiKnowledge>>(response)
}

export async function createAdminAiKnowledge(payload: AdminAiKnowledgeUpsert) {
  const response = await fetch("/api/admin/ai-ops/knowledge", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminAiKnowledge>(response)
}

export async function updateAdminAiKnowledge(id: string, payload: AdminAiKnowledgeUpsert) {
  const response = await fetch(`/api/admin/ai-ops/knowledge/${id}`, {
    method: "PUT",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(payload)
  })

  return parseEnvelope<AdminAiKnowledge>(response)
}

export async function deleteAdminAiKnowledge(id: string) {
  const response = await fetch(`/api/admin/ai-ops/knowledge/${id}`, { method: "DELETE" })
  return parseEnvelope<null>(response)
}

export async function getAdminAiChatHistory(sessionId?: string) {
  const response = await fetch(`/api/admin/ai-ops/chat/history?${buildQuery({ sessionId }).toString()}`, { cache: "no-store" })
  return parseEnvelope<AdminAiOpsChatMessage[]>(response)
}

export async function sendAdminAiChatMessage(message: string, sessionId?: string) {
  const response = await fetch("/api/admin/ai-ops/chat", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ sessionId, message })
  })

  return parseEnvelope<AdminAiOpsChatResponse>(response)
}
