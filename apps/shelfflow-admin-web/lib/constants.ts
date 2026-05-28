export const SESSION_COOKIE_NAME = "shelfflow_admin_session"
export const TOKEN_COOKIE_NAME = "shelfflow_admin_token"
export const SESSION_MAX_AGE_SECONDS = 60 * 60 * 2
export const DEFAULT_PAGE_SIZE = 20
export const MAX_QUERY_PAGE_SIZE = 100
export const DASHBOARD_ROUTES = {
  login: "/login",
  dashboard: "/dashboard",
  overview: "/dashboard/overview",
  products: "/dashboard/products",
  batches: "/dashboard/batches",
  pricing: "/dashboard/pricing",
  pickupPoints: "/dashboard/pickup-points",
  orderMonitor: "/dashboard/order-monitor",
  orders: "/dashboard/orders",
  lossStatistics: "/dashboard/loss-statistics",
  operationLogs: "/dashboard/operation-logs",
  aiAssistant: "/dashboard/ai-assistant"
} as const

export const DASHBOARD_DEFAULT_ROUTE = DASHBOARD_ROUTES.overview
