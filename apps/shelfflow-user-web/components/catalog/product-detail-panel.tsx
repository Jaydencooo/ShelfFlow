"use client"

import { useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import Link from "next/link"
import Image from "next/image"
import { ArrowLeft, CheckCircle2, ShoppingCart } from "lucide-react"

import { APP_ROUTES, CART_CHANGED_EVENT_NAME, MAX_CART_ITEM_QUANTITY, SUCCESS_TOAST_DISMISS_MS } from "@/lib/constants"
import { addCartItem, getProductDetail, isUnauthorizedError } from "@/lib/client/api"
import { formatCurrency, formatDaysToExpire } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserCatalogProductDetail } from "@/lib/types"
import { EmptyState, InlineError, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

export function ProductDetailPanel({ productId }: { productId: string }) {
  const router = useRouter()
  const pathname = usePathname()
  const [product, setProduct] = useState<UserCatalogProductDetail | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [quantity, setQuantity] = useState(1)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setLoadError(null)
    getProductDetail(productId)
      .then(setProduct)
      .catch((error) => setLoadError(error instanceof Error ? error.message : "商品详情加载失败"))
      .finally(() => setLoading(false))
  }, [productId])

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timer = window.setTimeout(() => setSuccessMessage(null), SUCCESS_TOAST_DISMISS_MS)
    return () => window.clearTimeout(timer)
  }, [successMessage])

  if (loading) {
    return <Panel className="p-8 text-sm text-slate-500">加载商品详情中...</Panel>
  }

  if (loadError) {
    return <InlineError message={loadError} />
  }

  if (!product) {
    return <EmptyState title="商品不存在" description="目标商品不存在或当前不可售。" />
  }

  return (
    <div className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-slate-900" href={APP_ROUTES.products}>
        <ArrowLeft className="h-4 w-4" />
        返回商品列表
      </Link>
      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <Panel className="overflow-hidden">
          <Image alt={product.name} className="h-full w-full object-cover" height={720} src={product.image || "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1600&auto=format&fit=crop&q=80"} width={900} />
        </Panel>
        <Panel className="p-6">
          <SectionTitle title={product.name} description={product.categoryName} />
          <div className="mt-6 space-y-5">
            {actionError ? <InlineError message={actionError} /> : null}
            {successMessage ? (
              <div className="flex items-center justify-between gap-3 rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                <span className="inline-flex items-center gap-2 font-medium">
                  <CheckCircle2 className="h-4 w-4" />
                  {successMessage}
                </span>
                <Link className="font-semibold text-emerald-900 hover:text-emerald-700" href={APP_ROUTES.cart}>
                  去购物车
                </Link>
              </div>
            ) : null}
            <div className="flex flex-wrap gap-2">
              <StatusBadge tone={product.availableQuantity > 0 ? "success" : "danger"}>
                {product.availableQuantity > 0 ? `${product.availableQuantity} 件可售` : "已售罄"}
              </StatusBadge>
              <StatusBadge tone={typeof product.daysToExpire === "number" && product.daysToExpire <= 3 ? "warning" : "info"}>{formatDaysToExpire(product.daysToExpire)}</StatusBadge>
            </div>
            <p className="text-sm leading-7 text-slate-600">{product.description || "暂无商品描述"}</p>
            <div className="flex items-end gap-4">
              <div className="text-sm text-slate-400 line-through">{formatCurrency(product.listPrice)}</div>
              <div className="text-3xl font-semibold text-emerald-700">{formatCurrency(product.currentPrice)}</div>
            </div>
            <div className="rounded-2xl bg-slate-50 p-4 text-sm text-slate-600">
              <div>推荐批次：{product.recommendedBatchId || "-"}</div>
              <div className="mt-1">最近到期：{product.nearestExpiryDate || "-"}</div>
            </div>
            {product.specs.length > 0 ? (
              <Panel className="p-4">
                <SectionTitle title="规格信息" />
                <div className="mt-4 space-y-3">
                  {product.specs.map((spec) => (
                    <div className="flex flex-wrap gap-2" key={spec.name}>
                      <div className="w-20 text-sm font-medium text-slate-700">{spec.name}</div>
                      <div className="flex flex-wrap gap-2">
                        {spec.values.map((value) => (
                          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-600" key={`${spec.name}-${value}`}>{value}</span>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </Panel>
            ) : null}
            <div className="rounded-2xl border border-slate-200 p-4">
              <div className="text-sm font-medium text-slate-700">购买数量</div>
              <div className="mt-3 flex items-center gap-3">
                <button
                  data-testid="product-detail-quantity-decrease"
                  className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={quantity <= 1}
                  onClick={() => setQuantity((current) => Math.max(1, current - 1))}
                  type="button"
                >
                  -
                </button>
                <div className="min-w-12 text-center text-base font-semibold text-slate-950">{quantity}</div>
                <button
                  data-testid="product-detail-quantity-increase"
                  className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={quantity >= Math.min(product.availableQuantity, MAX_CART_ITEM_QUANTITY)}
                  onClick={() => setQuantity((current) => Math.min(Math.min(product.availableQuantity, MAX_CART_ITEM_QUANTITY), current + 1))}
                  type="button"
                >
                  +
                </button>
              </div>
            </div>
            <button
              data-testid="product-detail-add-cart"
              className="inline-flex w-full items-center justify-center rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
              disabled={submitting || product.availableQuantity < 1}
              onClick={async () => {
                setSubmitting(true)
                setActionError(null)
                setSuccessMessage(null)
                try {
                  await addCartItem({ productId: product.id, quantity })
                  window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
                  setSuccessMessage(`已加入购物车，共 ${quantity} 件`)
                } catch (submitError) {
                  if (isUnauthorizedError(submitError)) {
                    router.push(buildLoginRedirectPath(pathname))
                    return
                  }
                  setActionError(submitError instanceof Error ? submitError.message : "加入购物车失败")
                } finally {
                  setSubmitting(false)
                }
              }}
              type="button"
            >
              <ShoppingCart className="mr-2 h-4 w-4" />
              {product.availableQuantity < 1 ? "已售罄" : submitting ? "加入中..." : "加入购物车"}
            </button>
          </div>
        </Panel>
      </div>
    </div>
  )
}
