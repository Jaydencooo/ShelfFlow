"use client"

import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter, useSearchParams } from "next/navigation"

import { APP_ROUTES, ORDER_PAGE_SIZE } from "@/lib/constants"
import { getOrderDetail, getOrders, isUnauthorizedError, payOrder } from "@/lib/client/api"
import { formatCurrency, formatDateTime, formatOrderPayStatusLabel, formatOrderStatusLabel } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserOrderDetail, UserOrderSummary, UserOrderStatus } from "@/lib/types"
import { orderQuerySchema } from "@/lib/validation"
import { EmptyState, InlineError, InlineSuccess, PaginationControls, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"
import { OrderCancelDialog } from "@/components/order/order-cancel-dialog"
import { OrderDetailCard } from "@/components/order/order-detail-card"

function orderStatusTone(status: UserOrderStatus) {
  if (status === "completed") return "success"
  if (status === "cancelled") return "danger"
  if (status === "pending_payment") return "warning"
  return "info"
}

export function OrderHistoryPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const [orders, setOrders] = useState<UserOrderSummary[]>([])
  const [selectedOrder, setSelectedOrder] = useState<UserOrderDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [cancelOrderId, setCancelOrderId] = useState<string | null>(null)

  const query = useMemo(() => {
    const parsed = orderQuerySchema.safeParse({
      page: searchParams.get("page") ?? 1,
      pageSize: ORDER_PAGE_SIZE,
      status: searchParams.get("status") ?? undefined,
      sortBy: searchParams.get("sortBy") ?? undefined,
      sortOrder: searchParams.get("sortOrder") ?? undefined
    })

    if (parsed.success) {
      return parsed.data
    }

    return {
      page: 1,
      pageSize: ORDER_PAGE_SIZE,
      status: undefined,
      sortBy: "orderTime" as const,
      sortOrder: "desc" as const
    }
  }, [searchParams])
  const focusedOrderId = searchParams.get("focus")
  const [total, setTotal] = useState(0)

  const loadOrders = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const page = await getOrders(query)
      setOrders(page.items)
      setTotal(page.total)
      const preferredOrderId = focusedOrderId || page.items[0]?.id
      if (preferredOrderId) {
        setSelectedOrder(await getOrderDetail(preferredOrderId))
      } else {
        setSelectedOrder(null)
      }
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        router.replace(buildLoginRedirectPath(`${pathname}?${searchParams.toString()}`))
        return
      }
      setError(loadError instanceof Error ? loadError.message : "订单加载失败")
    } finally {
      setLoading(false)
    }
  }, [focusedOrderId, pathname, query, router, searchParams])

  useEffect(() => {
    void loadOrders()
  }, [loadOrders])

  function replaceSearch(next: {
    page?: number
    status?: UserOrderStatus | ""
    focus?: string | null
    sortBy?: "orderTime" | "amount"
    sortOrder?: "asc" | "desc"
  }) {
    const nextParams = new URLSearchParams(searchParams.toString())

    if (next.page && next.page > 1) nextParams.set("page", String(next.page))
    else nextParams.delete("page")

    if (next.status) nextParams.set("status", next.status)
    else nextParams.delete("status")

    if (next.focus) nextParams.set("focus", next.focus)
    else nextParams.delete("focus")

    if (next.sortBy) nextParams.set("sortBy", next.sortBy)
    else nextParams.delete("sortBy")

    if (next.sortOrder) nextParams.set("sortOrder", next.sortOrder)
    else nextParams.delete("sortOrder")

    const nextSearch = nextParams.toString()
    router.replace(nextSearch ? `${pathname}?${nextSearch}` : pathname)
  }

  const statusOptions: Array<{ value: UserOrderStatus | ""; label: string }> = useMemo(
    () => [
      { value: "", label: "全部" },
      { value: "pending_payment", label: "待支付" },
      { value: "to_prepare", label: "待备货" },
      { value: "preparing", label: "备货中" },
      { value: "ready_for_pickup", label: "待自提" },
      { value: "completed", label: "已完成" },
      { value: "cancelled", label: "已取消" }
    ],
    []
  )

  async function refreshSelectedOrder() {
    if (!selectedOrder) {
      await loadOrders()
      return
    }

    setSelectedOrder(await getOrderDetail(selectedOrder.id))
    await loadOrders()
  }

  if (loading) {
    return <Panel className="p-8 text-sm text-slate-500">加载订单中...</Panel>
  }

  if (error && orders.length === 0) {
    return <InlineError message={error} />
  }

  if (!loading && orders.length === 0) {
    return <EmptyState title="暂无订单" description="完成下单后，这里会展示真实订单和当前状态流转。" />
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
      <Panel className="p-6">
        <SectionTitle title="订单列表" description="用户订单已接通真实后端，支付和取消操作直接作用于订单状态。" />
        <div className="mt-4 flex flex-wrap gap-2">
          {statusOptions.map((option) => (
            <button
              className={`rounded-full px-4 py-2 text-sm font-medium transition ${query.status === option.value ? "bg-slate-900 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
              key={option.label}
              onClick={() => replaceSearch({ page: 1, status: option.value, focus: focusedOrderId, sortBy: query.sortBy, sortOrder: query.sortOrder })}
              type="button"
            >
              {option.label}
            </button>
          ))}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {[
            { label: "最新下单", sortBy: "orderTime" as const, sortOrder: "desc" as const },
            { label: "最早下单", sortBy: "orderTime" as const, sortOrder: "asc" as const },
            { label: "金额最高", sortBy: "amount" as const, sortOrder: "desc" as const },
            { label: "金额最低", sortBy: "amount" as const, sortOrder: "asc" as const }
          ].map((option) => {
            const isActive = query.sortBy === option.sortBy && query.sortOrder === option.sortOrder
            return (
              <button
                className={`rounded-full px-4 py-2 text-sm font-medium transition ${isActive ? "bg-slate-900 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                key={`${option.sortBy}-${option.sortOrder}`}
                onClick={() =>
                  replaceSearch({
                    page: 1,
                    status: query.status,
                    focus: focusedOrderId,
                    sortBy: option.sortBy,
                    sortOrder: option.sortOrder
                  })
                }
                type="button"
              >
                {option.label}
              </button>
            )
          })}
        </div>
        <div className="mt-6 space-y-3">
          {error ? <InlineError message={error} /> : null}
          {successMessage ? <InlineSuccess message={successMessage} /> : null}
          {orders.map((order) => (
            <div
              className={`rounded-2xl border p-4 transition ${selectedOrder?.id === order.id ? "border-emerald-300 bg-emerald-50/60" : "border-slate-100 hover:border-slate-200 hover:bg-slate-50"}`}
              key={order.id}
            >
              <button
                data-testid={`order-list-select-${order.id}`}
                className="w-full text-left"
                onClick={async () => {
                  setError(null)
                  setSelectedOrder(await getOrderDetail(order.id))
                  replaceSearch({ page: query.page, status: query.status, focus: order.id, sortBy: query.sortBy, sortOrder: query.sortOrder })
                }}
                type="button"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-sm font-semibold text-slate-950">{order.orderNumber}</div>
                    <div className="mt-1 text-xs text-slate-500">{formatDateTime(order.orderTime)}</div>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <StatusBadge tone={orderStatusTone(order.status)}>{formatOrderStatusLabel(order.status)}</StatusBadge>
                    <StatusBadge tone={order.payStatus === "paid" ? "success" : "warning"}>{formatOrderPayStatusLabel(order.payStatus)}</StatusBadge>
                  </div>
                </div>
                <div className="mt-3 flex items-center justify-between text-sm">
                  <span className="text-slate-500">{order.itemCount} 件商品</span>
                  <span className="font-semibold text-slate-900">{formatCurrency(order.totalAmount)}</span>
                </div>
              </button>
              <div className="mt-3 flex justify-end">
                <Link className="text-xs font-medium text-emerald-700 hover:text-emerald-800" href={`${APP_ROUTES.orders}/${order.id}`}>
                  查看独立详情页
                </Link>
              </div>
            </div>
          ))}
        </div>
        <div className="mt-6">
          <PaginationControls
            page={query.page}
            pageSize={query.pageSize}
            total={total}
            onPageChange={(nextPage) => replaceSearch({ page: nextPage, status: query.status, focus: focusedOrderId, sortBy: query.sortBy, sortOrder: query.sortOrder })}
          />
        </div>
      </Panel>
      {selectedOrder ? (
        <OrderDetailCard
          onCancel={async () => {
            setCancelOrderId(selectedOrder.id)
          }}
          onPay={async () => {
            try {
              setError(null)
              setSuccessMessage(null)
              await payOrder(selectedOrder.id)
              setSuccessMessage("订单已支付")
              await refreshSelectedOrder()
            } catch (actionError) {
              if (isUnauthorizedError(actionError)) {
                router.replace(buildLoginRedirectPath(`${pathname}?${searchParams.toString()}`))
                return
              }
              setError(actionError instanceof Error ? actionError.message : "支付订单失败")
            }
          }}
          onRefresh={async () => {
            try {
              await refreshSelectedOrder()
            } catch (actionError) {
              if (isUnauthorizedError(actionError)) {
                router.replace(buildLoginRedirectPath(`${pathname}?${searchParams.toString()}`))
                return
              }
              setError(actionError instanceof Error ? actionError.message : "刷新订单状态失败")
            }
          }}
          order={selectedOrder}
        />
      ) : (
        <Panel className="p-6">
          <SectionTitle title="订单详情" description="请选择左侧订单查看详情。" />
          <div className="mt-6 text-sm text-slate-500">请选择左侧订单查看详情。</div>
        </Panel>
      )}
      {cancelOrderId ? (
        <OrderCancelDialog
          orderId={cancelOrderId}
          onCancelled={async () => {
            setError(null)
            setSuccessMessage("订单已取消")
            await refreshSelectedOrder()
          }}
          onClose={() => setCancelOrderId(null)}
          onError={(message, unauthorized) => {
            if (unauthorized) {
              router.replace(buildLoginRedirectPath(`${pathname}?${searchParams.toString()}`))
              return
            }
            setError(message)
          }}
        />
      ) : null}
    </div>
  )
}
