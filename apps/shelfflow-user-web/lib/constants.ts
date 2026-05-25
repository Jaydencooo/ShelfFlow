export const APP_ROUTES = {
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
export const ORDER_PAGE_SIZE = 10
export const MAX_CART_ITEM_QUANTITY = 99
export const ORDER_REMARK_MAX_LENGTH = 100
export const PICKUP_CONTACT_DETAIL_MAX_LENGTH = 120
export const PICKUP_CONTACT_LABEL_MAX_LENGTH = 16
