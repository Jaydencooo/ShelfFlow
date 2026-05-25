"use client"

import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { ClipboardList, Eye, History, RefreshCw, Search } from "lucide-react"

import {
  getAdminOrderDetail,
  getAdminOrders,
  isUnauthorizedError,
  logoutRequest,
  updateAdminOrderStatus,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants"
import { formatCurrency, formatDateTime } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type {
  AdminOrderDetail,
  AdminOrderPayStatus,
  AdminOrderStatus,
  AdminOrderSummary,
} from "@/lib/types"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

const orderStatusLabels: Record<AdminOrderStatus, string> = {
  pending_payment: "待支付",
  to_prepare: "待备货",
  preparing: "备货中",
  ready_for_pickup: "待自提",
  completed: "已完成",
  cancelled: "已取消",
  refunded: "已退款",
}

const orderStatusClasses: Record<AdminOrderStatus, string> = {
  pending_payment: "bg-amber-100 text-amber-700",
  to_prepare: "bg-sky-100 text-sky-700",
  preparing: "bg-indigo-100 text-indigo-700",
  ready_for_pickup: "bg-emerald-100 text-emerald-700",
  completed: "bg-slate-200 text-slate-700",
  cancelled: "bg-red-100 text-red-700",
  refunded: "bg-purple-100 text-purple-700",
}

const payStatusLabels: Record<AdminOrderPayStatus, string> = {
  unpaid: "未支付",
  paid: "已支付",
  refunded: "已退款",
}

const allOrderStatuses: AdminOrderStatus[] = [
  "pending_payment",
  "to_prepare",
  "preparing",
  "ready_for_pickup",
  "completed",
  "cancelled",
  "refunded",
]
const fulfillmentStatuses: AdminOrderStatus[] = ["to_prepare", "preparing", "ready_for_pickup", "completed"]
const paymentStatuses: AdminOrderPayStatus[] = ["unpaid", "paid", "refunded"]

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function isAdminOrderStatus(value: string | null): value is AdminOrderStatus {
  return value === "pending_payment"
    || value === "to_prepare"
    || value === "preparing"
    || value === "ready_for_pickup"
    || value === "completed"
    || value === "cancelled"
    || value === "refunded"
}

function isAdminOrderPayStatus(value: string | null): value is AdminOrderPayStatus {
  return value === "unpaid" || value === "paid" || value === "refunded"
}

function formatOptionalDateTime(value?: string) {
  return value ? formatDateTime(value) : "-"
}

function formatEventTypeLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    submitted: "提交订单",
    paid: "支付订单",
    cancelled: "取消订单",
    fulfillment_updated: "履约更新",
  }
  return value ? (labels[value] ?? value) : "-"
}

function formatActorTypeLabel(value: string | null | undefined) {
  const labels: Record<string, string> = {
    user: "用户",
    admin: "管理端",
    system: "系统",
  }
  return value ? (labels[value] ?? value) : "-"
}

function getNextOrderStatus(status: AdminOrderStatus): AdminOrderStatus | null {
  if (status === "to_prepare") {
    return "preparing"
  }
  if (status === "preparing") {
    return "ready_for_pickup"
  }
  if (status === "ready_for_pickup") {
    return "completed"
  }
  return null
}

function OrderStatusBadge({ status }: { status: AdminOrderStatus }) {
  return (
    <span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${orderStatusClasses[status]}`}>
      {orderStatusLabels[status]}
    </span>
  )
}

function MetricCard(props: { label: string; value: string; hint: string }) {
  return (
    <Card className="border-0" style={glassCard}>
      <CardContent className="space-y-1 p-5">
        <p className="text-sm text-slate-500">{props.label}</p>
        <p className="text-2xl font-semibold text-slate-950">{props.value}</p>
        <p className="text-sm text-slate-600">{props.hint}</p>
      </CardContent>
    </Card>
  )
}

export function OrderFulfillmentPanel(props: {
  title?: string
  description?: string
  mode?: "monitor" | "fulfillment"
}) {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const [orders, setOrders] = useState<AdminOrderSummary[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(() => {
    const raw = Number(searchParams.get("page") ?? "1")
    return Number.isFinite(raw) && raw > 0 ? raw : 1
  })
  const [pageSize] = useState(DEFAULT_PAGE_SIZE)
  const [draftKeyword, setDraftKeyword] = useState(searchParams.get("keyword") ?? "")
  const [draftStatus, setDraftStatus] = useState(() => {
    const rawStatus = searchParams.get("status")
    return isAdminOrderStatus(rawStatus) ? rawStatus : ""
  })
  const [draftPayStatus, setDraftPayStatus] = useState(() => {
    const rawPayStatus = searchParams.get("payStatus")
    return isAdminOrderPayStatus(rawPayStatus) ? rawPayStatus : ""
  })
  const [filters, setFilters] = useState<{
    keyword?: string
    status?: AdminOrderStatus
    payStatus?: AdminOrderPayStatus
  }>(() => ({
    keyword: searchParams.get("keyword") || undefined,
    status: isAdminOrderStatus(searchParams.get("status")) ? searchParams.get("status") as AdminOrderStatus : undefined,
    payStatus: isAdminOrderPayStatus(searchParams.get("payStatus")) ? searchParams.get("payStatus") as AdminOrderPayStatus : undefined,
  }))
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState<AdminOrderDetail | null>(null)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)
  const statusOptions = props.mode === "monitor" ? allOrderStatuses : fulfillmentStatuses
  const isFulfillmentMode = props.mode !== "monitor"

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  useEffect(() => {
    const params = new URLSearchParams()
    params.set("page", String(page))
    if (filters.keyword) {
      params.set("keyword", filters.keyword)
    }
    if (filters.status) {
      params.set("status", filters.status)
    }
    if (filters.payStatus) {
      params.set("payStatus", filters.payStatus)
    }
    router.replace(params.toString() ? `${pathname}?${params.toString()}` : pathname, { scroll: false })
  }, [filters, page, pathname, router])

  const loadOrders = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await getAdminOrders({
        page,
        pageSize,
        keyword: filters.keyword,
        status: filters.status,
        payStatus: filters.payStatus,
        sortBy: "orderTime",
        sortOrder: "desc",
      })

      setOrders(result.items)
      setTotal(result.total)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }

      setError(getErrorMessage(loadError, "加载订单列表失败"))
    } finally {
      setLoading(false)
    }
  }, [filters.keyword, filters.payStatus, filters.status, handleUnauthorized, page, pageSize])

  useEffect(() => {
    void loadOrders()
  }, [loadOrders])

  const metrics = useMemo(() => {
    const toPrepare = orders.filter((order) => order.status === "to_prepare").length
    const processing = orders.filter((order) => order.status === "preparing" || order.status === "ready_for_pickup").length
    const amount = orders.reduce((sum, order) => sum + order.totalAmount, 0)

    return {
      total: total.toString(),
      toPrepare: toPrepare.toString(),
      processing: processing.toString(),
      amount: formatCurrency(amount),
    }
  }, [orders, total])

  async function openOrderDetail(orderId: string) {
    setDetailOpen(true)
    setDetailLoading(true)
    setActionError(null)

    try {
      const detail = await getAdminOrderDetail(orderId)
      setSelectedOrder(detail)
    } catch (detailError) {
      if (isUnauthorizedError(detailError)) {
        await handleUnauthorized()
        return
      }

      setActionError(getErrorMessage(detailError, "加载订单详情失败"))
    } finally {
      setDetailLoading(false)
    }
  }

  async function handleUpdateOrderStatus(order: AdminOrderSummary | AdminOrderDetail) {
    setActionError(null)
    setSuccessMessage(null)

    const nextStatus = getNextOrderStatus(order.status)
    if (!nextStatus) {
      setActionError(`订单 ${order.orderNumber} 当前状态不可在页面直接流转`)
      return
    }

    try {
      const detail = await updateAdminOrderStatus(order.id, nextStatus)
      setSelectedOrder(detail)
      setSuccessMessage(`订单 ${order.orderNumber} 已更新为${orderStatusLabels[nextStatus]}`)
      await loadOrders()
    } catch (updateError) {
      if (isUnauthorizedError(updateError)) {
        await handleUnauthorized()
        return
      }

      setActionError(getErrorMessage(updateError, "订单状态更新失败"))
    }
  }

  function requestOrderStatusUpdate(order: AdminOrderSummary | AdminOrderDetail) {
    const nextStatus = getNextOrderStatus(order.status)
    if (!nextStatus) {
      setActionError(`订单 ${order.orderNumber} 当前状态不可在页面直接流转`)
      return
    }

    setConfirmAction({
      title: "确认流转订单状态",
      description: `将订单 ${order.orderNumber} 从「${orderStatusLabels[order.status]}」流转为「${orderStatusLabels[nextStatus]}」。完成状态会结转库存，操作后不能在页面回退。`,
      confirmLabel: `转${orderStatusLabels[nextStatus]}`,
      onConfirm: async () => {
        setConfirmAction(null)
        await handleUpdateOrderStatus(order)
      },
    })
  }

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(1)
    setFilters({
      keyword: draftKeyword || undefined,
      status: isAdminOrderStatus(draftStatus) ? draftStatus : undefined,
      payStatus: isAdminOrderPayStatus(draftPayStatus) ? draftPayStatus : undefined,
    })
  }

  function handleResetFilters() {
    setDraftKeyword("")
    setDraftStatus("")
    setDraftPayStatus("")
    setPage(1)
    setFilters({})
  }

  const totalPages = Math.max(1, Math.ceil(total / pageSize))

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">{props.title ?? "订单履约"}</h1>
        <p className="text-sm text-slate-600">
          {props.description ?? "管理已支付订单的备货、自提和完成结转，完成订单会把锁定库存结转为已售库存。"}
        </p>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="订单数" value={metrics.total} hint="当前筛选条件下的订单总数" />
        <MetricCard label="待备货" value={metrics.toPrepare} hint="需要进入备货流程的订单" />
        <MetricCard label="履约中" value={metrics.processing} hint="备货中或待自提订单" />
        <MetricCard label="当前页金额" value={metrics.amount} hint="当前页订单金额合计" />
      </section>

      <Card className="border-0" style={glassCard}>
        <CardContent className="p-5">
          <form className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_200px_200px_auto]" onSubmit={handleSearchSubmit}>
            <div className="space-y-2">
              <Label htmlFor="orderKeyword">搜索订单</Label>
              <div className="relative">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input
                  id="orderKeyword"
                  className={`${glassInputClassName} pl-9`}
                  onChange={(event) => setDraftKeyword(event.target.value)}
                  placeholder="订单号 / 手机号 / 自提码"
                  value={draftKeyword}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="orderStatus">订单状态</Label>
              <select
                id="orderStatus"
                className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                onChange={(event) => setDraftStatus(event.target.value)}
                value={draftStatus}
              >
                <option value="">全部状态</option>
                {statusOptions.map((status) => (
                  <option key={status} value={status}>
                    {orderStatusLabels[status]}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="payStatus">支付状态</Label>
              <select
                id="payStatus"
                className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                onChange={(event) => setDraftPayStatus(event.target.value)}
                value={draftPayStatus}
              >
                <option value="">全部支付状态</option>
                {paymentStatuses.map((status) => (
                  <option key={status} value={status}>
                    {payStatusLabels[status]}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex items-end gap-3">
              <Button className="flex-1 border-0 text-white" style={{ background: primaryGradient, boxShadow: primaryShadow }} type="submit">
                查询
              </Button>
              <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={handleResetFilters} type="button" variant="outline">
                重置
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {actionError ? (
        <Alert variant="destructive">
          <AlertTitle>操作失败</AlertTitle>
          <AlertDescription>{actionError}</AlertDescription>
        </Alert>
      ) : null}

      {successMessage ? (
        <Alert>
          <AlertTitle>操作成功</AlertTitle>
          <AlertDescription>{successMessage}</AlertDescription>
        </Alert>
      ) : null}

      <Card className="border-0" style={glassCard}>
        <CardHeader className="flex flex-row items-center justify-between gap-3">
          <div className="space-y-1">
            <CardTitle className="text-lg">订单列表</CardTitle>
            <p className="text-sm text-slate-500">
              总计 {total} 条记录，{isFulfillmentMode ? "支持履约状态推进和详情查看。" : "支持全量状态筛选和详情查看。"}
            </p>
          </div>
          <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadOrders()} size="sm" variant="outline">
            <RefreshCw className="mr-2 h-4 w-4" />
            刷新订单
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-12 text-sm text-slate-500">
              <Spinner className="mr-2" />
              正在加载订单数据...
            </div>
          ) : null}

          {!loading && error ? (
            <Alert variant="destructive">
              <AlertTitle>加载失败</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          ) : null}

          {!loading && !error && orders.length === 0 ? (
            <Empty className="border-white/40 bg-white/20">
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <ClipboardList className="size-5" />
                </EmptyMedia>
                <EmptyTitle>暂无订单</EmptyTitle>
                <EmptyDescription>当前筛选条件下没有需要处理的订单。</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadOrders()} variant="outline">刷新订单</Button>
              </EmptyContent>
            </Empty>
          ) : null}

          {!loading && !error && orders.length > 0 ? (
            <div className="space-y-4">
              <div className="overflow-hidden rounded-xl border border-white/40 bg-white/30">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>订单号</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>金额</TableHead>
                      <TableHead>支付</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>自提</TableHead>
                      <TableHead>下单时间</TableHead>
                      <TableHead>操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {orders.map((order) => (
                      <TableRow key={order.id}>
                        <TableCell className="font-medium text-slate-900">{order.orderNumber}</TableCell>
                        <TableCell>
                          <div className="space-y-1">
                            <p className="font-medium text-slate-900">{order.consignee || order.userName || "-"}</p>
                            <p className="text-xs text-slate-500">{order.phone || `用户 ID: ${order.userId}`}</p>
                          </div>
                        </TableCell>
                        <TableCell>{formatCurrency(order.totalAmount)}</TableCell>
                        <TableCell>{payStatusLabels[order.payStatus]}</TableCell>
                        <TableCell>
                          <OrderStatusBadge status={order.status} />
                        </TableCell>
                        <TableCell>
                          <div className="space-y-1 text-sm">
                            <p>{order.pickupCode || "-"}</p>
                            <p className="text-slate-500">{formatOptionalDateTime(order.pickupDeadline)}</p>
                          </div>
                        </TableCell>
                        <TableCell>{formatDateTime(order.orderTime)}</TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void openOrderDetail(order.id)} size="sm" variant="outline">
                              <Eye className="mr-2 h-4 w-4" />
                              详情
                            </Button>
                            <Button
                              disabled={!getNextOrderStatus(order.status)}
                              className="border-white/50 bg-white/30 hover:bg-white/50"
                              onClick={() => requestOrderStatusUpdate(order)}
                              size="sm"
                              variant="outline"
                            >
                              {getNextOrderStatus(order.status)
                                ? `转${orderStatusLabels[getNextOrderStatus(order.status)!]}`
                                : "不可流转"}
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              <div className="flex items-center justify-between text-sm text-slate-600">
                <p>
                  第 {page} / {totalPages} 页
                </p>
                <div className="flex gap-2">
                  <Button className="border-white/50 bg-white/30 hover:bg-white/50" disabled={page <= 1} onClick={() => setPage((current) => current - 1)} size="sm" variant="outline">
                    上一页
                  </Button>
                  <Button
                    disabled={page >= totalPages}
                    className="border-white/50 bg-white/30 hover:bg-white/50"
                    onClick={() => setPage((current) => current + 1)}
                    size="sm"
                    variant="outline"
                  >
                    下一页
                  </Button>
                </div>
              </div>
            </div>
          ) : null}
        </CardContent>
      </Card>

      <Dialog onOpenChange={setDetailOpen} open={detailOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto border-0 sm:max-w-[820px]" style={glassDialog}>
          <DialogHeader>
            <DialogTitle>订单详情</DialogTitle>
          </DialogHeader>

          {detailLoading ? (
            <div className="flex items-center justify-center py-12 text-sm text-slate-500">
              <Spinner className="mr-2" />
              正在加载订单详情...
            </div>
          ) : null}

          {!detailLoading && selectedOrder ? (
            <div className="space-y-6">
              <div className="grid gap-4 md:grid-cols-3">
                <div className="space-y-1 rounded-xl border border-white/40 bg-white/30 p-3">
                  <p className="text-xs text-slate-500">订单号</p>
                  <p className="font-medium text-slate-950">{selectedOrder.orderNumber}</p>
                </div>
                <div className="space-y-1 rounded-xl border border-white/40 bg-white/30 p-3">
                  <p className="text-xs text-slate-500">订单状态</p>
                  <OrderStatusBadge status={selectedOrder.status} />
                </div>
                <div className="space-y-1 rounded-xl border border-white/40 bg-white/30 p-3">
                  <p className="text-xs text-slate-500">订单金额</p>
                  <p className="font-medium text-slate-950">{formatCurrency(selectedOrder.totalAmount)}</p>
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2 text-sm">
                  <p className="font-medium text-slate-950">用户信息</p>
                  <p>收货人：{selectedOrder.consignee || selectedOrder.userName || "-"}</p>
                  <p>手机号：{selectedOrder.phone || "-"}</p>
                  <p>备注：{selectedOrder.remark || "-"}</p>
                </div>
                <div className="space-y-2 text-sm">
                  <p className="font-medium text-slate-950">自提信息</p>
                  <p>自提点：{selectedOrder.pickupPoint || "-"}</p>
                  <p>自提码：{selectedOrder.pickupCode || "-"}</p>
                  <p>截止时间：{formatOptionalDateTime(selectedOrder.pickupDeadline)}</p>
                </div>
              </div>

              <div className="overflow-hidden rounded-xl border border-white/40 bg-white/30">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>商品</TableHead>
                      <TableHead>批次</TableHead>
                      <TableHead>数量</TableHead>
                      <TableHead>单价</TableHead>
                      <TableHead>小计</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {selectedOrder.items.map((item) => (
                      <TableRow key={`${item.productId}-${item.batchId}-${item.name}`}>
                        <TableCell>{item.name}</TableCell>
                        <TableCell>{item.batchId || "-"}</TableCell>
                        <TableCell>{item.quantity}</TableCell>
                        <TableCell>{formatCurrency(item.unitPrice)}</TableCell>
                        <TableCell>{formatCurrency(item.lineAmount)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {selectedOrder.events && selectedOrder.events.length > 0 ? (
                <div className="rounded-xl border border-white/40 bg-white/30 p-4">
                  <div className="flex items-center gap-2 text-sm font-semibold text-slate-950">
                    <History className="h-4 w-4 text-slate-500" />
                    订单轨迹
                  </div>
                  <div className="mt-4 space-y-4">
                    {selectedOrder.events.map((event) => (
                      <div className="relative pl-5" key={event.id}>
                        <span className="absolute left-0 top-1.5 h-2.5 w-2.5 rounded-full bg-emerald-500" />
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          <div className="text-sm font-medium text-slate-900">{formatEventTypeLabel(event.eventType)}</div>
                          <div className="text-xs text-slate-500">{formatDateTime(event.eventTime)}</div>
                        </div>
                        <div className="mt-1 text-xs text-slate-500">
                          {formatActorTypeLabel(event.actorType)} · {event.note || "无备注"}
                        </div>
                        {(event.fromStatus || event.toStatus || event.fromPayStatus || event.toPayStatus) ? (
                          <div className="mt-2 text-xs text-slate-400">
                            {event.fromStatus ? orderStatusLabels[event.fromStatus] : "-"} → {event.toStatus ? orderStatusLabels[event.toStatus] : "-"}
                            {" · "}
                            {event.fromPayStatus ? payStatusLabels[event.fromPayStatus] : "-"} → {event.toPayStatus ? payStatusLabels[event.toPayStatus] : "-"}
                          </div>
                        ) : null}
                      </div>
                    ))}
                  </div>
                </div>
              ) : null}

              <div className="flex justify-end gap-2">
                <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => setDetailOpen(false)} variant="outline">
                  关闭
                </Button>
                <Button
                  disabled={!getNextOrderStatus(selectedOrder.status)}
                  className="border-0 text-white"
                  style={{ background: primaryGradient, boxShadow: primaryShadow }}
                  onClick={() => requestOrderStatusUpdate(selectedOrder)}
                >
                  {getNextOrderStatus(selectedOrder.status)
                    ? `转${orderStatusLabels[getNextOrderStatus(selectedOrder.status)!]}`
                    : "不可流转"}
                </Button>
              </div>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>
      <ActionConfirmDialog action={confirmAction} onOpenChange={(open) => {
        if (!open) {
          setConfirmAction(null)
        }
      }} />
    </div>
  )
}
