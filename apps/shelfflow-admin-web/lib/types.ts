export interface ApiEnvelope<T> {
  code: "ok"
  message: string
  data: T
  requestId: string
  timestamp: string
}

export interface PaginatedResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminSession {
  userId: string
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
  token: string
}

export interface SessionUser {
  userId: string
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
}

export type ProductStatus = "active" | "inactive"
export type BatchStatus = "draft" | "active" | "paused" | "sold_out" | "expired"
export type PricingStatus = "pending" | "active" | "stale" | "disabled"
export type PricingRuleStatus = "enabled" | "disabled"
export type AdminOrderStatus =
  | "pending_payment"
  | "to_prepare"
  | "preparing"
  | "ready_for_pickup"
  | "completed"
  | "cancelled"
  | "refunded"
export type AdminOrderPayStatus = "unpaid" | "paid" | "refunded"
export type OrderEventType = "submitted" | "paid" | "cancelled" | "fulfillment_updated"
export type OrderEventActorType = "user" | "admin" | "system"

export interface ProductSpec {
  id?: number
  productId?: number
  name: string
  value: string
}

export interface AdminProductCategory {
  id: string
  name: string
  sort?: number
  productCount?: number
}

export interface AdminProductCategoryUpsert {
  name: string
  sort?: number
}

export interface ProductRecord {
  id: string
  name: string
  categoryId: string
  categoryName?: string
  price: number
  image?: string
  description?: string
  status: ProductStatus
  shelfLifeDays?: number
  specs?: ProductSpec[]
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

export interface AdminOrderItem {
  productId: string
  batchId: string
  name: string
  image?: string
  productSpec?: string
  quantity: number
  unitPrice: number
  lineAmount: number
}

export interface AdminOrderSummary {
  id: string
  orderNumber: string
  userId: string
  userName?: string
  consignee?: string
  phone?: string
  status: AdminOrderStatus
  payStatus: AdminOrderPayStatus
  totalAmount: number
  pickupCode?: string
  orderTime: string
  checkoutTime?: string
  pickupDeadline?: string
  itemCount: number
}

export interface AdminOrderDetail extends AdminOrderSummary {
  remark?: string
  pickupPoint?: string
  cancelTime?: string
  cancelReason?: string
  items: AdminOrderItem[]
  events?: AdminOrderEvent[]
}

export interface AdminOrderEvent {
  id: string
  eventType: OrderEventType
  actorType: OrderEventActorType
  actorId?: string
  fromStatus?: AdminOrderStatus
  toStatus?: AdminOrderStatus
  fromPayStatus?: AdminOrderPayStatus
  toPayStatus?: AdminOrderPayStatus
  note?: string
  eventTime: string
}

export interface AdminPricingRule {
  id: string
  name: string
  minDaysToExpire: number
  maxDaysToExpire: number
  discountRate: number
  priority: number
  status: PricingRuleStatus
  createTime?: string
  updateTime?: string
}

export interface AdminPricingSuggestion {
  id: string
  batchId: string
  batchCode: string
  productId: string
  productName: string
  daysToExpire: number
  availableStock: number
  currentPrice: number
  suggestedPrice: number
  suggestedDiscountRate: number
  confidence: string
  reason: string
}

export interface AdminLossStatsCategory {
  categoryId: string
  categoryName: string
  batchCount: number
  expiringSoonBatchCount: number
  soldOutBatchCount: number
  expiredStockQuantity: number
  estimatedLossAmount: number
  lossRate: number
}

export interface AdminLossStatsSuggestion {
  id: string
  batchId: string
  batchCode: string
  productId: string
  productName: string
  categoryName: string
  daysToExpire: number
  availableStock: number
  estimatedLossAmount: number
  priority: "高" | "中" | "低"
  suggestion: string
  action: string
}

export interface AdminLossStatsOverview {
  totalBatchCount: number
  expiredBatchCount: number
  expiredStockQuantity: number
  expiringSoonBatchCount: number
  expiringSoonStockQuantity: number
  soldOutBatchCount: number
  saleableStockQuantity: number
  estimatedLossAmount: number
  lossRate: number
  revenueAmount: number
  estimatedCostAmount: number
  grossProfitAmount: number
  operatingExpenseAmount: number
  netProfitAmount: number
  lossCostAmount: number
  grossMarginRate: number
  netProfitRate: number
  orderCount: number
  paidOrderCount: number
  soldItemQuantity: number
  averageOrderAmount: number
  categoryStats: AdminLossStatsCategory[]
  suggestions: AdminLossStatsSuggestion[]
}

export interface AdminAiKnowledge {
  id: string
  title: string
  category: string
  content: string
  createTime?: string
  updateTime?: string
}

export interface AdminAiKnowledgeUpsert {
  title: string
  category: string
  content: string
}

export interface AdminAiKnowledgeQuery {
  page: number
  pageSize: number
  keyword?: string
  category?: string
  sortBy?: string
  sortOrder?: "asc" | "desc"
}

export interface AdminAiOpsSuggestion {
  id: string
  type: string
  priority: "HIGH" | "MEDIUM" | "LOW"
  title: string
  content: string
  productId: string
  productName: string
  batchId: string
  batchCode: string
  daysToExpire: number
  availableQuantity: number
  suggestedAction: string
  status?: "pending" | "executed" | "ignored"
}

export interface AdminAiOpsChatResponse {
  sessionId: string
  provider: string
  model: string
  answer: string
  references: string[]
}

export interface AdminAiOpsChatMessage {
  id: string
  sessionId: string
  role: "user" | "assistant"
  content: string
  provider?: string
  model?: string
  references: string[]
  createTime: string
}

export interface ProductUpsert {
  name: string
  categoryId: string
  price: number
  description?: string
  image?: string
  status?: ProductStatus
  shelfLifeDays?: number
  flavors?: ProductSpec[]
}

export interface InventoryBatchUpsert {
  productId: string
  batchCode: string
  productionDate: string
  expiryDate: string
  stockQuantity: number
  lockedQuantity?: number
  soldQuantity?: number
  basePrice: number
  batchStatus?: BatchStatus
  pricingStatus?: PricingStatus
}

export interface AdminPricingRuleUpsert {
  name: string
  minDaysToExpire: number
  maxDaysToExpire: number
  discountRate: number
  priority: number
  status?: PricingRuleStatus
}

export interface ProductQuery {
  page: number
  pageSize: number
  keyword?: string
  categoryId?: string
  status?: ProductStatus
}

export interface InventoryBatchQuery {
  page: number
  pageSize: number
  keyword?: string
  categoryId?: string
  batchStatus?: BatchStatus
  pricingStatus?: PricingStatus
  expiryDaysMin?: number
  expiryDaysMax?: number
}

export interface AdminOrderQuery {
  page: number
  pageSize: number
  keyword?: string
  status?: AdminOrderStatus
  payStatus?: AdminOrderPayStatus
  sortBy?: string
  sortOrder?: "asc" | "desc"
}

export interface AdminPricingRuleQuery {
  page: number
  pageSize: number
  keyword?: string
  status?: PricingRuleStatus
  sortBy?: string
  sortOrder?: "asc" | "desc"
}
