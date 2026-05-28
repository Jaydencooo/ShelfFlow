"use client"

import { CheckCircle2, CircleDollarSign, Clock3, MapPin, RefreshCw, TicketCheck, XCircle } from "lucide-react"

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
    admin: "工作人员",
    system: "系统"
  }
  return value ? (labels[value] ?? value) : "-"
}

const orderProgressSteps: Array<{ status: UserOrderDetail["status"]; label: string }> = [
  { status: "pending_payment", label: "待支付" },
  { status: "to_prepare", label: "待备货" },
  { status: "preparing", label: "备货中" },
  { status: "ready_for_pickup", label: "待自提" },
  { status: "completed", label: "已完成" }
]

function resolveProgressIndex(status: UserOrderDetail["status"]) {
  if (status === "cancelled" || status === "refunded") {
    return -1
  }
  return orderProgressSteps.findIndex((step) => step.status === status)
}

export function OrderDetailCard({
  order,
  onPay,
  onCancel,
  onRefresh,
  title = "订单详情",
  description = "查看自提信息、商品清单和订单进度。"
}: {
  order: UserOrderDetail
  onPay: () => Promise<void>
  onCancel: () => Promise<void>
  onRefresh: () => Promise<void>
  title?: string
  description?: string
}) {
  const showPickupCredential = order.status === "ready_for_pickup" && Boolean(order.pickupCode)
  const progressIndex = resolveProgressIndex(order.status)

  return (
    <Panel className="p-6 sm:p-7">
      <SectionTitle title={title} description={description} />
      <div className="mt-6 space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-[24px] border border-slate-900/[0.06] bg-white/78 p-5">
          <div>
            <div className="text-sm text-slate-500">当前状态</div>
            <div className="mt-1 text-2xl font-semibold text-slate-950">{formatOrderStatusLabel(order.status)}</div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge tone={orderStatusTone(order.status)}>{formatOrderStatusLabel(order.status)}</StatusBadge>
            <StatusBadge tone={order.payStatus === "paid" ? "success" : "warning"}>{formatOrderPayStatusLabel(order.payStatus)}</StatusBadge>
          </div>
        </div>
        <div className="grid gap-3 rounded-[24px] border border-slate-900/[0.06] bg-white/78 p-5 sm:grid-cols-5">
          {orderProgressSteps.map((step, index) => {
            const reached = progressIndex >= index
            const current = progressIndex === index
            return (
              <div className="flex items-center gap-2 sm:flex-col sm:items-start" key={step.status}>
                <span className={`flex h-9 w-9 items-center justify-center rounded-full ${reached ? "bg-[#079669] text-white shadow-[0_10px_20px_rgba(7,150,105,0.18)]" : "bg-slate-100 text-slate-400"}`}>
                  {reached ? <CheckCircle2 className="h-4 w-4" /> : <Clock3 className="h-4 w-4" />}
                </span>
                <span className={`text-sm font-semibold ${current ? "text-[#079669]" : reached ? "text-slate-900" : "text-slate-400"}`}>{step.label}</span>
              </div>
            )
          })}
          {progressIndex < 0 ? (
            <div className="sm:col-span-5 rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-700">
              当前订单已{formatOrderStatusLabel(order.status)}，履约流程已停止。
            </div>
          ) : null}
        </div>
        <div className="grid gap-3 rounded-[24px] border border-slate-900/[0.06] bg-slate-50/80 p-5 text-sm text-slate-600 sm:grid-cols-2">
          <div className="flex items-center justify-between"><span>订单号</span><span>{order.orderNumber}</span></div>
          <div className="flex items-center justify-between"><span>下单时间</span><span>{formatDateTime(order.orderTime)}</span></div>
          <div className="flex items-center justify-between"><span>取货码</span><span>{order.pickupCode || "-"}</span></div>
          <div className="flex items-center justify-between"><span>取货点</span><span>{order.pickupPoint || "-"}</span></div>
          <div className="flex items-center justify-between"><span>自提人</span><span>{order.consignee || "-"}</span></div>
          <div className="flex items-center justify-between"><span>联系电话</span><span>{order.phone || "-"}</span></div>
          <div className="flex items-center justify-between"><span>截止时间</span><span>{order.pickupDeadline ? formatDateTime(order.pickupDeadline) : "-"}</span></div>
          <div className="flex items-center justify-between"><span>订单金额</span><span className="text-xl font-semibold text-slate-950">{formatCurrency(order.totalAmount)}</span></div>
        </div>
        {showPickupCredential ? (
          <div className="rounded-[28px] border border-emerald-100 bg-emerald-50/90 p-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm font-semibold text-emerald-800">
                  <TicketCheck className="h-4 w-4" />
                  到店自提凭证
                </div>
                <div className="text-4xl font-bold tracking-widest text-emerald-950">{order.pickupCode}</div>
                <p className="text-sm text-emerald-700">请在自提点向工作人员出示此自提码。</p>
              </div>
              <div className="min-w-[220px] rounded-[22px] bg-white/76 p-4 text-sm text-emerald-900 shadow-sm">
                <div className="flex items-start gap-2">
                  <MapPin className="mt-0.5 h-4 w-4 flex-shrink-0" />
                  <div>
                    <p className="font-medium">自提点</p>
                    <p className="mt-1 text-emerald-800">{order.pickupPoint || "-"}</p>
                    <p className="mt-2 text-xs text-emerald-700">截止时间：{order.pickupDeadline ? formatDateTime(order.pickupDeadline) : "-"}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : null}
        <div className="space-y-3">
          {order.items.map((item) => (
            <div className="rounded-[24px] border border-slate-900/[0.06] bg-white/78 p-4" key={`${order.id}-${item.productId}-${item.batchId}`}>
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
          <div className="rounded-[24px] border border-slate-900/[0.06] bg-white/78 p-5">
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
              className="inline-flex h-12 items-center rounded-2xl bg-[#079669] px-5 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(7,150,105,0.18)] transition hover:bg-[#07845d]"
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
              className="inline-flex h-12 items-center rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-700 transition hover:bg-rose-50"
              onClick={() => void onCancel()}
              type="button"
            >
              <XCircle className="mr-2 h-4 w-4" />
              取消订单
            </button>
          ) : null}
          <button
            data-testid={`order-refresh-${order.id}`}
            className="inline-flex h-12 items-center rounded-2xl border border-slate-900/[0.08] bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
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
