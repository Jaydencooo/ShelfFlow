"use client"

import { CircleDollarSign, RefreshCw, XCircle } from "lucide-react"

import { formatCurrency, formatDateTime, formatOrderPayStatusLabel, formatOrderStatusLabel } from "@/lib/formatters"
import type { UserOrderDetail } from "@/lib/types"
import { Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

function orderStatusTone(status: UserOrderDetail["status"]) {
  if (status === "completed") return "success"
  if (status === "cancelled") return "danger"
  if (status === "pending_payment") return "warning"
  return "info"
}

function formatEventTypeLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    submitted: "提交订单",
    paid: "支付订单",
    cancelled: "取消订单",
    fulfillment_updated: "履约更新"
  }
  return value ? (labels[value] ?? value) : "-"
}

function formatActorTypeLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    user: "用户",
    admin: "管理端",
    system: "系统"
  }
  return value ? (labels[value] ?? value) : "-"
}

export function OrderDetailCard({
  order,
  onPay,
  onCancel,
  onRefresh,
  title = "订单详情",
  description = "展示当前订单明细和用户侧可执行动作。"
}: {
  order: UserOrderDetail
  onPay: () => Promise<void>
  onCancel: () => Promise<void>
  onRefresh: () => Promise<void>
  title?: string
  description?: string
}) {
  return (
    <Panel className="p-6">
      <SectionTitle title={title} description={description} />
      <div className="mt-6 space-y-5">
        <div className="flex flex-wrap items-center gap-2">
          <StatusBadge tone={orderStatusTone(order.status)}>{formatOrderStatusLabel(order.status)}</StatusBadge>
          <StatusBadge tone={order.payStatus === "paid" ? "success" : "warning"}>{formatOrderPayStatusLabel(order.payStatus)}</StatusBadge>
        </div>
        <div className="grid gap-3 rounded-2xl bg-slate-50 p-4 text-sm text-slate-600">
          <div className="flex items-center justify-between"><span>订单号</span><span>{order.orderNumber}</span></div>
          <div className="flex items-center justify-between"><span>下单时间</span><span>{formatDateTime(order.orderTime)}</span></div>
          <div className="flex items-center justify-between"><span>取货码</span><span>{order.pickupCode || "-"}</span></div>
          <div className="flex items-center justify-between"><span>取货点</span><span>{order.pickupPoint || "-"}</span></div>
          <div className="flex items-center justify-between"><span>订单金额</span><span className="font-semibold text-slate-900">{formatCurrency(order.totalAmount)}</span></div>
        </div>
        <div className="space-y-3">
          {order.items.map((item) => (
            <div className="rounded-2xl border border-slate-100 p-4" key={`${order.id}-${item.productId}-${item.batchId}`}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="font-medium text-slate-900">{item.name}</div>
                  <div className="mt-1 text-sm text-slate-500">{item.productSpec || "默认规格"}</div>
                </div>
                <div className="text-right text-sm text-slate-600">
                  <div>x{item.quantity}</div>
                  <div className="mt-1 font-semibold text-slate-900">{formatCurrency(item.lineAmount)}</div>
                </div>
              </div>
            </div>
          ))}
        </div>
        {order.events && order.events.length > 0 ? (
          <div className="rounded-2xl border border-slate-100 p-4">
            <div className="text-sm font-semibold text-slate-950">订单轨迹</div>
            <div className="mt-4 space-y-4">
              {order.events.map((event) => (
                <div className="relative pl-5" key={event.id}>
                  <span className="absolute left-0 top-1.5 h-2.5 w-2.5 rounded-full bg-emerald-500" />
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="text-sm font-medium text-slate-900">{formatEventTypeLabel(event.eventType)}</div>
                    <div className="text-xs text-slate-500">{formatDateTime(event.eventTime)}</div>
                  </div>
                  <div className="mt-1 text-xs text-slate-500">{formatActorTypeLabel(event.actorType)} · {event.note || "无备注"}</div>
                  {(event.fromStatus || event.toStatus || event.fromPayStatus || event.toPayStatus) ? (
                    <div className="mt-2 text-xs text-slate-400">
                      {event.fromStatus ? formatOrderStatusLabel(event.fromStatus) : "-"} → {event.toStatus ? formatOrderStatusLabel(event.toStatus) : "-"}
                      {" · "}
                      {event.fromPayStatus ? formatOrderPayStatusLabel(event.fromPayStatus) : "-"} → {event.toPayStatus ? formatOrderPayStatusLabel(event.toPayStatus) : "-"}
                    </div>
                  ) : null}
                </div>
              ))}
            </div>
          </div>
        ) : null}
        <div className="flex flex-wrap gap-3">
          {order.status === "pending_payment" ? (
            <button
              data-testid={`order-pay-${order.id}`}
              className="inline-flex items-center rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700"
              onClick={() => void onPay()}
              type="button"
            >
              <CircleDollarSign className="mr-2 h-4 w-4" />
              支付订单
            </button>
          ) : null}
          {(order.status === "pending_payment" || order.status === "to_prepare") ? (
            <button
              data-testid={`order-cancel-${order.id}`}
              className="inline-flex items-center rounded-xl border border-rose-200 px-4 py-3 text-sm font-semibold text-rose-700 transition hover:bg-rose-50"
              onClick={() => void onCancel()}
              type="button"
            >
              <XCircle className="mr-2 h-4 w-4" />
              取消订单
            </button>
          ) : null}
          <button
            data-testid={`order-refresh-${order.id}`}
            className="inline-flex items-center rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
            onClick={() => void onRefresh()}
            type="button"
          >
            <RefreshCw className="mr-2 h-4 w-4" />
            刷新状态
          </button>
        </div>
      </div>
    </Panel>
  )
}
