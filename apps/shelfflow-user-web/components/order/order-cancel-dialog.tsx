"use client"

import { useState } from "react"

import { cancelOrder, isUnauthorizedError } from "@/lib/client/api"
import { Panel } from "@/components/common/ui"

interface OrderCancelDialogProps {
  orderId: string
  onClose: () => void
  onCancelled: () => Promise<void>
  onError: (message: string, unauthorized: boolean) => void
}

export function OrderCancelDialog({ orderId, onClose, onCancelled, onError }: OrderCancelDialogProps) {
  const [cancelReason, setCancelReason] = useState("")
  const [submitting, setSubmitting] = useState(false)

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/30 px-4 backdrop-blur-sm">
      <Panel className="w-full max-w-md p-6">
        <div className="text-lg font-semibold text-slate-950">确认取消订单</div>
        <p className="mt-2 text-sm text-slate-500">取消后会释放已锁定库存，并写入订单轨迹。</p>
        <label className="mt-5 block space-y-2">
          <span className="text-sm font-medium text-slate-700">取消原因</span>
          <textarea
            className="min-h-28 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
            data-testid="order-cancel-reason"
            maxLength={100}
            onChange={(event) => setCancelReason(event.target.value)}
            placeholder="例如：临时有事，稍后再买"
            value={cancelReason}
          />
          <span className="block text-right text-xs text-slate-400">{cancelReason.length}/100</span>
        </label>
        <div className="mt-5 flex gap-3">
          <button
            className="flex-1 rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={submitting}
            onClick={onClose}
            type="button"
          >
            再看看
          </button>
          <button
            className="flex-1 rounded-xl bg-rose-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:bg-rose-300"
            data-testid="order-cancel-confirm"
            disabled={submitting}
            onClick={async () => {
              setSubmitting(true)
              try {
                await cancelOrder(orderId, { cancelReason: cancelReason.trim() || undefined })
                await onCancelled()
                onClose()
              } catch (error) {
                onError(error instanceof Error ? error.message : "取消订单失败", isUnauthorizedError(error))
              } finally {
                setSubmitting(false)
              }
            }}
            type="button"
          >
            {submitting ? "取消中..." : "确认取消"}
          </button>
        </div>
      </Panel>
    </div>
  )
}
