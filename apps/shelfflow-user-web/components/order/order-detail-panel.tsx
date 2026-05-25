"use client"

import Link from "next/link"
import { useCallback, useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { ArrowLeft } from "lucide-react"

import { APP_ROUTES } from "@/lib/constants"
import { getOrderDetail, isUnauthorizedError, payOrder } from "@/lib/client/api"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserOrderDetail } from "@/lib/types"
import { InlineError, InlineSuccess, Panel } from "@/components/common/ui"
import { OrderCancelDialog } from "@/components/order/order-cancel-dialog"
import { OrderDetailCard } from "@/components/order/order-detail-card"

export function OrderDetailPanel({ orderId }: { orderId: string }) {
  const router = useRouter()
  const pathname = usePathname()
  const [order, setOrder] = useState<UserOrderDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [confirmingCancel, setConfirmingCancel] = useState(false)

  const loadOrder = useCallback(async () => {
    setLoading(true)
    setLoadError(null)
    try {
      setOrder(await getOrderDetail(orderId))
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setLoadError(loadError instanceof Error ? loadError.message : "订单详情加载失败")
    } finally {
      setLoading(false)
    }
  }, [orderId, pathname, router])

  useEffect(() => {
    void loadOrder()
  }, [loadOrder])

  if (loading) {
    return <Panel className="p-8 text-sm text-slate-500">加载订单详情中...</Panel>
  }

  if (loadError) {
    return <InlineError message={loadError} />
  }

  if (!order) {
    return <InlineError message="订单不存在或当前用户无权查看该订单。" />
  }

  return (
    <div className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-slate-900" href={APP_ROUTES.orders}>
        <ArrowLeft className="h-4 w-4" />
        返回订单列表
      </Link>
      {actionError ? <InlineError message={actionError} /> : null}
      {successMessage ? <InlineSuccess message={successMessage} /> : null}
      <OrderDetailCard
        description="独立订单详情页，适合支付回跳、订单分享和后续履约跟踪。"
        onCancel={async () => {
          setConfirmingCancel(true)
        }}
        onPay={async () => {
          try {
            setActionError(null)
            setSuccessMessage(null)
            setOrder(await payOrder(order.id))
            setSuccessMessage("订单已支付")
          } catch (actionError) {
            if (isUnauthorizedError(actionError)) {
              router.replace(buildLoginRedirectPath(pathname))
              return
            }
            setActionError(actionError instanceof Error ? actionError.message : "支付订单失败")
          }
        }}
        onRefresh={async () => {
          try {
            setActionError(null)
            await loadOrder()
          } catch {
            // loadOrder 内已处理错误
          }
        }}
        order={order}
      />
      {confirmingCancel ? (
        <OrderCancelDialog
          orderId={order.id}
          onCancelled={async () => {
            setActionError(null)
            setSuccessMessage("订单已取消")
            await loadOrder()
          }}
          onClose={() => setConfirmingCancel(false)}
          onError={(message, unauthorized) => {
            if (unauthorized) {
              router.replace(buildLoginRedirectPath(pathname))
              return
            }
            setActionError(message)
          }}
        />
      ) : null}
    </div>
  )
}
