export const APP_ROUTES = {
  home: "/",
  login: "/login",
  register: "/register",
  forgotPassword: "/forgot-password",
  products: "/products",
  cart: "/cart",
  orders: "/orders",
  account: "/account"
} as const

export const USER_SESSION_COOKIE_NAME = "shelfflow_user_session"
export const USER_TOKEN_COOKIE_NAME = "shelfflow_user_token"
export const SESSION_MAX_AGE_SECONDS = 60 * 60 * 24 * 7
export const DEFAULT_PAGE_SIZE = 12
export const HOME_FEATURED_PAGE_SIZE = 6
export const ORDER_PAGE_SIZE = 10
export const MAX_CART_ITEM_QUANTITY = 99
export const ORDER_REMARK_MAX_LENGTH = 100
export const PICKUP_CONTACT_DETAIL_MAX_LENGTH = 120
export const PICKUP_CONTACT_LABEL_MAX_LENGTH = 16
export const CART_CHANGED_EVENT_NAME = "shelfflow:cart-changed"
export const SUCCESS_TOAST_DISMISS_MS = 3000
export const VERIFICATION_CODE_RESEND_SECONDS = 60
export const ONE_SECOND_MS = 1000
