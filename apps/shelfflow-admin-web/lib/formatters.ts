const currencyFormatter = new Intl.NumberFormat("zh-CN", {
  style: "currency",
  currency: "CNY",
  minimumFractionDigits: 2
})

const dateFormatter = new Intl.DateTimeFormat("zh-CN", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit"
})

const dateTimeFormatter = new Intl.DateTimeFormat("zh-CN", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit"
})

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value)
}

export function formatDate(value: string): string {
  return dateFormatter.format(new Date(value))
}

export function formatDateTime(value: string): string {
  return dateTimeFormatter.format(new Date(value))
}

export function calculateDaysLeft(expiryDate: string): number {
  const millisInDay = 24 * 60 * 60 * 1000
  const now = new Date()
  const expiry = new Date(expiryDate)
  return Math.max(0, Math.ceil((expiry.getTime() - now.getTime()) / millisInDay))
}
