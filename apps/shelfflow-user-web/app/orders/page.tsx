import { Suspense } from "react"

import { OrderHistoryPanel } from "@/components/order/order-history-panel"

export default function OrdersPage() {
  return (
    <Suspense fallback={<div className="rounded-2xl border border-white/70 bg-white/90 p-8 text-sm text-slate-500 shadow-sm">加载订单中...</div>}>
      <OrderHistoryPanel />
    </Suspense>
  )
}
