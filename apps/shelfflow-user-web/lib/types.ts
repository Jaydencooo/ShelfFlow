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

export type UserOrderStatus =
  | "pending_payment"
  | "to_prepare"
  | "preparing"
  | "ready_for_pickup"
  | "completed"
  | "cancelled"
  | "refunded"

export type UserOrderPayStatus = "unpaid" | "paid" | "refunded"
export type OrderEventType = "submitted" | "paid" | "cancelled" | "fulfillment_updated"
export type OrderEventActorType = "user" | "admin" | "system"

export interface UserSession {
  userId: string
  openId: string
  account?: string
  name?: string
  phone?: string
  email?: string
  token: string
}

export interface SessionUser {
  userId: string
  openId: string
  account?: string
  name?: string
  phone?: string
  email?: string
}

export interface UserLoginRequest {
  account: string
  password: string
}

export interface UserRegisterRequest {
  account: string
  name: string
  password: string
  confirmPassword: string
  verificationCode: string
}

export interface UserPasswordResetRequest {
  account: string
  newPassword: string
  confirmPassword: string
  verificationCode: string
}

export interface UserPasswordChangeRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface UserVerificationCodeRequest {
  account: string
  purpose: "register" | "reset_password" | "change_phone" | "change_email"
}

export interface UserVerificationCodeResponse {
  target: string
  purpose: "register" | "reset_password" | "change_phone" | "change_email"
  expiresInSeconds: number
  debugCode?: string
}

export interface UserProfileUpdateRequest {
  name: string
  phone?: string
  email?: string
  phoneVerificationCode?: string
  emailVerificationCode?: string
}

export interface UserPickupContact {
  id: string
  consignee: string
  phone: string
  label?: string
  detail?: string
  defaultContact: boolean
}

export interface UserPickupContactRequest {
  consignee: string
  phone: string
  label?: string
  detail?: string
  defaultContact?: boolean
}

export interface PickupPoint {
  id: string
  name: string
  address: string
  contactName?: string
  contactPhone?: string
  serviceTime?: string
  sort?: number
  enabled?: boolean
}

export interface UserCatalogCategory {
  id: string
  name: string
  sort?: number
}

export interface UserCatalogProduct {
  id: string
  name: string
  categoryId: string
  categoryName: string
  image?: string
  description?: string
  listPrice: number
  currentPrice: number
  recommendedBatchId?: string
  nearestExpiryDate?: string
  daysToExpire?: number
  availableQuantity: number
}

export interface UserCatalogProductSpec {
  name: string
  values: string[]
}

export interface UserCatalogProductDetail extends UserCatalogProduct {
  specs: UserCatalogProductSpec[]
}

export interface UserCartItem {
  id: string
  productId: string
  batchId?: string
  name: string
  image?: string
  productSpec?: string
  quantity: number
  unitPrice: number
  lineAmount: number
  availableQuantity: number
  nearestExpiryDate?: string
}

export interface UserOrderItem {
  productId: string
  batchId?: string
  name: string
  image?: string
  productSpec?: string
  quantity: number
  unitPrice: number
  lineAmount: number
}

export interface UserOrderSummary {
  id: string
  orderNumber: string
  status: UserOrderStatus
  payStatus: UserOrderPayStatus
  totalAmount: number
  remark?: string
  pickupCode?: string
  orderTime: string
  pickupDeadline?: string
  itemCount: number
  items: UserOrderItem[]
}

export interface UserOrderSubmitResult {
  id: string
  orderNumber: string
  status: UserOrderStatus
  payStatus: UserOrderPayStatus
  totalAmount: number
  itemCount: number
  pickupCode?: string
  orderTime: string
  pickupDeadline?: string
}

export interface UserOrderSubmitRequest {
  remark?: string
  pickupContactId?: string
  pickupPointId?: string
  cartItemIds?: string[]
}

export interface UserOrderCancelRequest {
  cancelReason?: string
}

export interface UserOrderEvent {
  id: string
  eventType: OrderEventType
  actorType: OrderEventActorType
  actorId?: string
  fromStatus?: UserOrderStatus
  toStatus?: UserOrderStatus
  fromPayStatus?: UserOrderPayStatus
  toPayStatus?: UserOrderPayStatus
  note?: string
  eventTime: string
}

export interface UserOrderDetail extends UserOrderSummary {
  phone?: string
  pickupPoint?: string
  consignee?: string
  checkoutTime?: string
  cancelTime?: string
  cancelReason?: string
  events?: UserOrderEvent[]
}

export interface UserCatalogProductQuery {
  page: number
  pageSize: number
  keyword?: string
  categoryId?: string
  sortBy?: "updatedAt" | "price" | "daysToExpire"
  sortOrder?: "asc" | "desc"
}

export interface UserOrderQuery {
  page: number
  pageSize: number
  status?: UserOrderStatus
  sortBy?: "orderTime" | "amount"
  sortOrder?: "asc" | "desc"
}
