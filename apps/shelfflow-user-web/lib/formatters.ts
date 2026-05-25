const currencyFormatter = new Intl.NumberFormat("zh-CN", {
  style: "currency",
  currency: "CNY",
  minimumFractionDigits: 2
})

const dateTimeFormatter = new Intl.DateTimeFormat("zh-CN", {
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit"
})

export function formatCurrency(value: number | string | null | undefined) {
  if (value === null || value === undefined || value === "") {
    return currencyFormatter.format(0)
  }

  const normalized = typeof value === "number" ? value : Number(value)
  return currencyFormatter.format(Number.isFinite(normalized) ? normalized : 0)
}

export function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "-"
  }

  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? value : dateTimeFormatter.format(parsed)
}

export function formatDaysToExpire(daysToExpire: number | null | undefined) {
  if (daysToExpire === null || daysToExpire === undefined) {
    return "有效期未知"
  }

  if (daysToExpire <= 0) {
    return "今日到期"
  }

  if (daysToExpire <= 3) {
    return `${daysToExpire} 天内到期`
  }

  return `${daysToExpire} 天后到期`
}

export function formatOrderStatusLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    pending_payment: "待支付",
    to_prepare: "待备货",
    preparing: "备货中",
    ready_for_pickup: "待自提",
    completed: "已完成",
    cancelled: "已取消",
    refunded: "已退款"
  }

  return value ? (labels[value] ?? value) : "-"
}

export function formatOrderPayStatusLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    unpaid: "未支付",
    paid: "已支付",
    refunded: "已退款"
  }

  return value ? (labels[value] ?? value) : "-"
}
