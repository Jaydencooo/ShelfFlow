"use client"

import Link from "next/link"
import Image from "next/image"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { MapPin, PackageCheck, ShoppingBag, Trash2 } from "lucide-react"

import { APP_ROUTES, MAX_CART_ITEM_QUANTITY, ORDER_REMARK_MAX_LENGTH } from "@/lib/constants"
import { clearCartItems, getCartItems, getPickupContacts, isUnauthorizedError, removeCartItem, submitOrder, updateCartItemQuantity } from "@/lib/client/api"
import { formatCurrency } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserCartItem, UserPickupContact } from "@/lib/types"
import { EmptyState, InlineError, InlineSuccess, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

export function CartPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const [items, setItems] = useState<UserCartItem[]>([])
  const [contacts, setContacts] = useState<UserPickupContact[]>([])
  const [selectedContactId, setSelectedContactId] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [remark, setRemark] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [updatingItemId, setUpdatingItemId] = useState<string | null>(null)
  const [clearing, setClearing] = useState(false)
  const [confirmingOrder, setConfirmingOrder] = useState(false)

  const loadCart = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [cartItems, pickupContacts] = await Promise.all([
        getCartItems(),
        getPickupContacts()
      ])
      setItems(cartItems)
      setContacts(pickupContacts)
      setSelectedContactId((current) => {
        if (current && pickupContacts.some((contact) => contact.id === current)) {
          return current
        }
        return pickupContacts.find((contact) => contact.defaultContact)?.id ?? pickupContacts[0]?.id ?? ""
      })
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setError(loadError instanceof Error ? loadError.message : "购物车加载失败")
    } finally {
      setLoading(false)
    }
  }, [pathname, router])

  useEffect(() => {
    void loadCart()
  }, [loadCart])

  const totalAmount = useMemo(
    () => items.reduce((sum, item) => sum + Number(item.lineAmount), 0),
    [items]
  )
  const totalQuantity = useMemo(
    () => items.reduce((sum, item) => sum + item.quantity, 0),
    [items]
  )
  const unavailableItems = useMemo(
    () => items.filter((item) => item.availableQuantity < item.quantity || item.availableQuantity < 1),
    [items]
  )
  const canSubmitOrder = !submitting && unavailableItems.length === 0 && items.length > 0

  async function handleSubmitOrder() {
    setSubmitting(true)
    setError(null)
    setSuccessMessage(null)

    try {
      const order = await submitOrder({
        remark: remark.trim() || undefined,
        pickupContactId: selectedContactId || undefined
      })
      router.push(`${APP_ROUTES.orders}/${order.id}`)
    } catch (submitError) {
      if (isUnauthorizedError(submitError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setError(submitError instanceof Error ? submitError.message : "提交订单失败")
    } finally {
      setSubmitting(false)
      setConfirmingOrder(false)
    }
  }

  const selectedContact = useMemo(
    () => contacts.find((contact) => contact.id === selectedContactId) ?? null,
    [contacts, selectedContactId]
  )

  if (loading) {
    return <Panel className="p-8 text-sm text-slate-500">加载购物车中...</Panel>
  }

  if (error && items.length === 0) {
    return <InlineError message={error} />
  }

  if (!loading && items.length === 0) {
    return <EmptyState title="购物车为空" description="先从商品目录加入可售商品，再回来下单。" />
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1.3fr_0.7fr]">
      <Panel className="p-6">
        <SectionTitle
          title="购物车"
          description="购物车与订单链路直接接入 Java 用户服务。"
          action={
            <button
              className="text-sm font-medium text-slate-500 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-50"
              disabled={clearing || items.length === 0}
              onClick={async () => {
                setClearing(true)
                setError(null)
                setSuccessMessage(null)
                try {
                  await clearCartItems()
                  setSuccessMessage("购物车已清空")
                  await loadCart()
                } catch (clearError) {
                  if (isUnauthorizedError(clearError)) {
                    router.replace(buildLoginRedirectPath(pathname))
                    return
                  }
                  setError(clearError instanceof Error ? clearError.message : "清空购物车失败")
                } finally {
                  setClearing(false)
                }
              }}
              type="button"
            >
              清空
            </button>
          }
        />
        <div className="mt-6 space-y-4">
          {error ? <InlineError message={error} /> : null}
          {successMessage ? <InlineSuccess message={successMessage} /> : null}
          {unavailableItems.length > 0 ? (
            <InlineError message={`有 ${unavailableItems.length} 个商品库存不足，请调整数量后再提交订单。`} />
          ) : null}
          {items.map((item) => (
            <div className="flex gap-4 rounded-2xl border border-slate-100 p-4" key={item.id}>
              <Image alt={item.name} className="h-24 w-24 rounded-xl object-cover" height={96} src={item.image || "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=600&auto=format&fit=crop&q=80"} width={96} />
              <div className="flex flex-1 flex-col justify-between">
                <div className="space-y-2">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="font-semibold text-slate-950">{item.name}</h3>
                      <p className="text-sm text-slate-500">{item.productSpec || "默认规格"}</p>
                    </div>
                    <StatusBadge tone="info">库存 {item.availableQuantity}</StatusBadge>
                  </div>
                  <div className="text-sm text-slate-500">最近到期：{item.nearestExpiryDate || "-"}</div>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <div className="text-sm text-slate-500">单价 {formatCurrency(item.unitPrice)}</div>
                    <div className="text-lg font-semibold text-emerald-700">{formatCurrency(item.lineAmount)}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      data-testid={`cart-item-decrease-${item.id}`}
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingItemId === item.id || item.quantity <= 1}
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await updateCartItemQuantity(item.id, item.quantity - 1)
                          setSuccessMessage("购物车数量已更新")
                          await loadCart()
                        } catch (updateError) {
                          if (isUnauthorizedError(updateError)) {
                            router.replace(buildLoginRedirectPath(pathname))
                            return
                          }
                          setError(updateError instanceof Error ? updateError.message : "更新购物车数量失败")
                        } finally {
                          setUpdatingItemId(null)
                        }
                      }}
                      type="button"
                    >
                      -
                    </button>
                    <div className="min-w-10 text-center text-sm font-semibold text-slate-900">{item.quantity}</div>
                    <button
                      data-testid={`cart-item-increase-${item.id}`}
                      className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingItemId === item.id || item.quantity >= Math.min(item.availableQuantity, MAX_CART_ITEM_QUANTITY)}
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await updateCartItemQuantity(item.id, item.quantity + 1)
                          setSuccessMessage("购物车数量已更新")
                          await loadCart()
                        } catch (updateError) {
                          if (isUnauthorizedError(updateError)) {
                            router.replace(buildLoginRedirectPath(pathname))
                            return
                          }
                          setError(updateError instanceof Error ? updateError.message : "更新购物车数量失败")
                        } finally {
                          setUpdatingItemId(null)
                        }
                      }}
                      type="button"
                    >
                      +
                    </button>
                    <button
                      data-testid={`cart-item-remove-${item.id}`}
                      className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-600 transition hover:border-slate-300 hover:text-slate-900"
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await removeCartItem(item.id)
                          setSuccessMessage("购物车项已删除")
                          await loadCart()
                        } catch (removeError) {
                          if (isUnauthorizedError(removeError)) {
                            router.replace(buildLoginRedirectPath(pathname))
                            return
                          }
                          setError(removeError instanceof Error ? removeError.message : "删除购物车项失败")
                        } finally {
                          setUpdatingItemId(null)
                        }
                      }}
                      type="button"
                    >
                      <Trash2 className="h-4 w-4" />
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </Panel>
      <Panel className="p-6">
        <SectionTitle
          title="提交订单"
          description="提交后进入待支付，支付成功后可由管理端履约。"
          action={
            <Link className="inline-flex items-center gap-2 text-sm font-medium text-emerald-700 hover:text-emerald-800" href={APP_ROUTES.products}>
              <ShoppingBag className="h-4 w-4" />
              继续选购
            </Link>
          }
        />
        <div className="mt-6 space-y-4">
          <div className="space-y-3">
            <div className="flex items-center justify-between gap-3">
              <span className="text-sm font-medium text-slate-700">自提联系人</span>
              <Link className="text-xs font-medium text-emerald-700 hover:text-emerald-800" href={APP_ROUTES.account}>
                管理联系人
              </Link>
            </div>
            {contacts.length === 0 ? (
              <div className="rounded-2xl border border-amber-100 bg-amber-50 p-4 text-sm text-amber-700">
                暂无自提联系人，本次将使用账户资料作为订单联系人。
              </div>
            ) : (
              <div className="grid gap-2">
                {contacts.map((contact) => (
                  <label
                    className={`flex cursor-pointer items-start gap-3 rounded-2xl border p-4 transition ${selectedContactId === contact.id ? "border-emerald-200 bg-emerald-50" : "border-slate-100 hover:bg-slate-50"}`}
                    key={contact.id}
                  >
                    <input
                      checked={selectedContactId === contact.id}
                      className="mt-1 h-4 w-4 border-slate-300 text-emerald-600"
                      data-testid={`cart-pickup-contact-${contact.id}`}
                      name="pickupContactId"
                      onChange={() => setSelectedContactId(contact.id)}
                      type="radio"
                    />
                    <span className="flex-1 text-sm">
                      <span className="flex flex-wrap items-center gap-2 font-semibold text-slate-950">
                        {contact.consignee}
                        {contact.defaultContact ? <StatusBadge tone="success">默认</StatusBadge> : null}
                      </span>
                      <span className="mt-1 block text-xs text-slate-500">{contact.phone}</span>
                      <span className="mt-2 flex items-center gap-1.5 text-xs text-slate-500">
                        <MapPin className="h-3.5 w-3.5" />
                        {contact.label || "自提"} · {contact.detail || "未填写备注"}
                      </span>
                    </span>
                  </label>
                ))}
              </div>
            )}
          </div>
          <label className="block space-y-2">
            <span className="text-sm font-medium text-slate-700">备注</span>
            <textarea
              className="min-h-28 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
              maxLength={ORDER_REMARK_MAX_LENGTH}
              placeholder="例如：下班后自提"
              value={remark}
              onChange={(event) => setRemark(event.target.value)}
            />
            <span className="block text-right text-xs text-slate-400">{remark.length}/{ORDER_REMARK_MAX_LENGTH}</span>
          </label>
          <div className="rounded-2xl bg-slate-50 p-4">
            <div className="flex items-center justify-between text-sm text-slate-500">
              <span>商品件数</span>
              <span>{totalQuantity}</span>
            </div>
            <div className="mt-3 flex items-center justify-between">
              <span className="text-sm text-slate-600">应付总额</span>
              <span className="text-2xl font-semibold text-slate-950">{formatCurrency(totalAmount)}</span>
            </div>
          </div>
          <button
            data-testid="cart-submit-order"
            className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300"
            disabled={!canSubmitOrder}
            onClick={() => setConfirmingOrder(true)}
            type="button"
          >
            <PackageCheck className="mr-2 h-4 w-4" />
            {submitting ? "提交中..." : "提交订单"}
          </button>
        </div>
      </Panel>
      {confirmingOrder ? (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/30 px-4 backdrop-blur-sm">
          <Panel className="w-full max-w-md p-6">
            <SectionTitle title="确认提交订单" description="订单提交后会锁定库存，并进入待支付状态。" />
            <div className="mt-5 space-y-3 rounded-2xl bg-slate-50 p-4 text-sm text-slate-600">
              <div className="flex items-center justify-between"><span>商品件数</span><span>{totalQuantity}</span></div>
              <div className="flex items-center justify-between"><span>应付金额</span><span className="font-semibold text-slate-950">{formatCurrency(totalAmount)}</span></div>
              <div className="flex items-center justify-between gap-4"><span>联系人</span><span className="max-w-52 truncate">{selectedContact ? `${selectedContact.consignee} ${selectedContact.phone}` : "账户资料"}</span></div>
              <div className="flex items-center justify-between"><span>备注</span><span className="max-w-52 truncate">{remark.trim() || "无"}</span></div>
            </div>
            <div className="mt-5 flex gap-3">
              <button
                className="flex-1 rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
                disabled={submitting}
                onClick={() => setConfirmingOrder(false)}
                type="button"
              >
                再看看
              </button>
              <button
                className="flex-1 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
                disabled={submitting}
                onClick={() => void handleSubmitOrder()}
                type="button"
              >
                {submitting ? "提交中..." : "确认提交"}
              </button>
            </div>
          </Panel>
        </div>
      ) : null}
    </div>
  )
}
