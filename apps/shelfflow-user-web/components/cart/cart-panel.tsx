"use client"

import Link from "next/link"
import Image from "next/image"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { CheckSquare2, MapPin, PackageCheck, ShoppingBag, Trash2 } from "lucide-react"

import { APP_ROUTES, CART_CHANGED_EVENT_NAME, MAX_CART_ITEM_QUANTITY, ORDER_REMARK_MAX_LENGTH } from "@/lib/constants"
import { clearCartItems, getCartItems, getPickupContacts, getPickupPoints, getSessionRequest, isUnauthorizedError, removeCartItem, submitOrder, updateCartItemQuantity } from "@/lib/client/api"
import { formatCurrency } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { PickupPoint, SessionUser, UserCartItem, UserPickupContact } from "@/lib/types"
import { EmptyState, InlineError, InlineSuccess, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

export function CartPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const [items, setItems] = useState<UserCartItem[]>([])
  const [contacts, setContacts] = useState<UserPickupContact[]>([])
  const [pickupPoints, setPickupPoints] = useState<PickupPoint[]>([])
  const [session, setSession] = useState<SessionUser | null>(null)
  const [selectedContactId, setSelectedContactId] = useState("")
  const [selectedPickupPointId, setSelectedPickupPointId] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [remark, setRemark] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [updatingItemId, setUpdatingItemId] = useState<string | null>(null)
  const [clearing, setClearing] = useState(false)
  const [deletingSelected, setDeletingSelected] = useState(false)
  const [confirmingOrder, setConfirmingOrder] = useState(false)
  const [selectedItemIds, setSelectedItemIds] = useState<string[]>([])

  const loadCart = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [cartItems, pickupContacts, availablePickupPoints, currentSession] = await Promise.all([
        getCartItems(),
        getPickupContacts(),
        getPickupPoints(),
        getSessionRequest()
      ])
      setItems(cartItems)
      setSession(currentSession)
      setSelectedItemIds((current) => {
        const selectableIds = cartItems
          .filter((item) => item.availableQuantity >= item.quantity && item.availableQuantity > 0)
          .map((item) => item.id)
        const retained = current.filter((id) => selectableIds.includes(id))
        return retained.length > 0 ? retained : selectableIds
      })
      setContacts(pickupContacts)
      setPickupPoints(availablePickupPoints)
      setSelectedContactId((current) => {
        if (current && pickupContacts.some((contact) => contact.id === current)) {
          return current
        }
        return pickupContacts.find((contact) => contact.defaultContact)?.id ?? pickupContacts[0]?.id ?? ""
      })
      setSelectedPickupPointId((current) => {
        if (current && availablePickupPoints.some((pickupPoint) => pickupPoint.id === current)) {
          return current
        }
        return availablePickupPoints[0]?.id ?? ""
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

  const selectedItems = useMemo(
    () => items.filter((item) => selectedItemIds.includes(item.id)),
    [items, selectedItemIds]
  )
  const totalAmount = useMemo(
    () => selectedItems.reduce((sum, item) => sum + Number(item.lineAmount), 0),
    [selectedItems]
  )
  const totalQuantity = useMemo(
    () => selectedItems.reduce((sum, item) => sum + item.quantity, 0),
    [selectedItems]
  )
  const unavailableItems = useMemo(
    () => items.filter((item) => item.availableQuantity < item.quantity || item.availableQuantity < 1),
    [items]
  )
  const selectableItems = useMemo(
    () => items.filter((item) => item.availableQuantity >= item.quantity && item.availableQuantity > 0),
    [items]
  )
  const allSelectableSelected = selectableItems.length > 0 && selectableItems.every((item) => selectedItemIds.includes(item.id))
  const fallbackContactReady = Boolean(session?.name?.trim() && session?.phone?.trim())
  const hasUsablePickupContact = contacts.length > 0 || fallbackContactReady
  const canSubmitOrder = !submitting
    && selectedItems.length > 0
    && hasUsablePickupContact
    && selectedItems.every((item) => item.availableQuantity >= item.quantity && item.availableQuantity > 0)

  function toggleItemSelection(item: UserCartItem) {
    if (item.availableQuantity < item.quantity || item.availableQuantity < 1) {
      return
    }
    setSelectedItemIds((current) =>
      current.includes(item.id) ? current.filter((id) => id !== item.id) : [...current, item.id]
    )
  }

  function toggleAllSelection() {
    setSelectedItemIds(allSelectableSelected ? [] : selectableItems.map((item) => item.id))
  }

  async function handleSubmitOrder() {
    setSubmitting(true)
    setError(null)
    setSuccessMessage(null)

    try {
      const order = await submitOrder({
        remark: remark.trim() || undefined,
        pickupContactId: selectedContactId || undefined,
        pickupPointId: selectedPickupPointId || undefined,
        cartItemIds: selectedItemIds
      })
      window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
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

  async function handleDeleteSelectedItems() {
    if (selectedItemIds.length === 0) {
      return
    }
    setDeletingSelected(true)
    setError(null)
    setSuccessMessage(null)
    try {
      await Promise.all(selectedItemIds.map((id) => removeCartItem(id)))
      setSelectedItemIds([])
      window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
      setSuccessMessage("已删除选中商品")
      await loadCart()
    } catch (deleteError) {
      if (isUnauthorizedError(deleteError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setError(deleteError instanceof Error ? deleteError.message : "批量删除失败")
    } finally {
      setDeletingSelected(false)
    }
  }

  const selectedContact = useMemo(
    () => contacts.find((contact) => contact.id === selectedContactId) ?? null,
    [contacts, selectedContactId]
  )
  const selectedPickupPoint = useMemo(
    () => pickupPoints.find((pickupPoint) => pickupPoint.id === selectedPickupPointId) ?? null,
    [pickupPoints, selectedPickupPointId]
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
    <div className="grid gap-7 lg:grid-cols-[minmax(0,1.35fr)_420px]">
      <Panel className="p-6 sm:p-7">
        <SectionTitle
          title="购物车"
          description="可勾选部分商品结算，下单前会确认商品库存和可售状态。"
          action={
            <div className="flex flex-wrap gap-2">
              <button
                className="inline-flex items-center gap-2 rounded-full border border-slate-900/[0.08] bg-white px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-emerald-200 hover:text-[#079669] disabled:cursor-not-allowed disabled:opacity-50"
                disabled={selectableItems.length === 0}
                onClick={toggleAllSelection}
                type="button"
              >
                <CheckSquare2 className="h-4 w-4" />
                {allSelectableSelected ? "取消全选" : "全选可结算"}
              </button>
              <button
                className="inline-flex items-center gap-2 rounded-full border border-rose-100 bg-white px-4 py-2 text-sm font-semibold text-rose-600 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
                disabled={deletingSelected || selectedItemIds.length === 0}
                onClick={() => void handleDeleteSelectedItems()}
                type="button"
              >
                <Trash2 className="h-4 w-4" />
                {deletingSelected ? "删除中" : `删除选中 ${selectedItemIds.length}`}
              </button>
              <button
                className="text-sm font-medium text-slate-500 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-50"
                disabled={clearing || items.length === 0}
                onClick={async () => {
                  setClearing(true)
                  setError(null)
                  setSuccessMessage(null)
                  try {
                    await clearCartItems()
                    setSelectedItemIds([])
                    window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
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
            </div>
          }
        />
        <div className="mt-5 rounded-[24px] border border-emerald-100 bg-emerald-50/70 px-5 py-4 text-sm text-slate-600">
          已选 <span className="font-semibold text-slate-950">{selectedItems.length}</span> 种商品，
          共 <span className="font-semibold text-slate-950">{totalQuantity}</span> 件，
          合计 <span className="font-semibold text-emerald-700">{formatCurrency(totalAmount)}</span>
        </div>
        <div className="mt-6 space-y-4">
          {error ? <InlineError message={error} /> : null}
          {successMessage ? <InlineSuccess message={successMessage} /> : null}
          {unavailableItems.length > 0 ? (
            <InlineError message={`有 ${unavailableItems.length} 个商品已失效或库存不足，已自动排除在结算外。`} />
          ) : null}
          {items.map((item) => (
            <div className={`flex gap-5 rounded-[26px] border p-4 transition ${selectedItemIds.includes(item.id) ? "border-emerald-200 bg-emerald-50/60 shadow-[0_16px_40px_rgba(7,150,105,0.08)]" : "border-slate-900/[0.06] bg-white/78 hover:border-slate-200"}`} key={item.id}>
              <input
                checked={selectedItemIds.includes(item.id)}
                className="mt-10 h-5 w-5 rounded border-slate-300 text-emerald-600 disabled:cursor-not-allowed disabled:opacity-40"
                disabled={item.availableQuantity < item.quantity || item.availableQuantity < 1}
                onChange={() => toggleItemSelection(item)}
                type="checkbox"
              />
              <Image alt={item.name} className="h-28 w-28 rounded-[22px] object-cover sm:h-32 sm:w-32" height={128} src={item.image || "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=600&auto=format&fit=crop&q=80"} width={128} />
              <div className="flex flex-1 flex-col justify-between">
                <div className="space-y-2">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-semibold text-slate-950">{item.name}</h3>
                      <p className="text-sm text-slate-500">{item.productSpec || "默认规格"}</p>
                    </div>
                    <StatusBadge tone="info">库存 {item.availableQuantity}</StatusBadge>
                  </div>
                  <div className="text-sm text-slate-500">最近到期：{item.nearestExpiryDate || "-"}</div>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <div className="text-sm text-slate-500">单价 {formatCurrency(item.unitPrice)}</div>
                    <div className="text-2xl font-semibold text-[#079669]">{formatCurrency(item.lineAmount)}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      data-testid={`cart-item-decrease-${item.id}`}
                      className="h-10 w-10 rounded-full border border-slate-900/[0.08] bg-white text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingItemId === item.id || item.quantity <= 1}
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await updateCartItemQuantity(item.id, item.quantity - 1)
                          window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
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
                      className="h-10 w-10 rounded-full border border-slate-900/[0.08] bg-white text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingItemId === item.id || item.quantity >= Math.min(item.availableQuantity, MAX_CART_ITEM_QUANTITY)}
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await updateCartItemQuantity(item.id, item.quantity + 1)
                          window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
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
                      className="inline-flex items-center gap-2 rounded-full border border-slate-900/[0.08] bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:border-rose-200 hover:text-rose-600"
                      onClick={async () => {
                        setUpdatingItemId(item.id)
                        setError(null)
                        setSuccessMessage(null)
                        try {
                          await removeCartItem(item.id)
                          setSelectedItemIds((current) => current.filter((id) => id !== item.id))
                          window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
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
      <Panel className="self-start p-6 sm:p-7 lg:sticky lg:top-28">
        <SectionTitle
          title="提交订单"
          description="提交后进入待支付，支付成功后工作人员会按自提信息备货。"
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
              <span className="text-sm font-medium text-slate-700">社区自提点</span>
            </div>
            {pickupPoints.length === 0 ? (
              <div className="rounded-2xl border border-amber-100 bg-amber-50 p-4 text-sm text-amber-700">
                暂无可选自提点，订单将使用系统默认自提点。
              </div>
            ) : (
              <div className="grid gap-2">
                {pickupPoints.map((pickupPoint) => (
                  <label
                    className={`flex cursor-pointer items-start gap-3 rounded-[24px] border p-4 transition ${selectedPickupPointId === pickupPoint.id ? "border-emerald-200 bg-emerald-50 shadow-[0_14px_34px_rgba(7,150,105,0.08)]" : "border-slate-900/[0.06] bg-white/72 hover:bg-white"}`}
                    key={pickupPoint.id}
                  >
                    <input
                      checked={selectedPickupPointId === pickupPoint.id}
                      className="mt-1 h-4 w-4 border-slate-300 text-emerald-600"
                      data-testid={`cart-pickup-point-${pickupPoint.id}`}
                      name="pickupPointId"
                      onChange={() => setSelectedPickupPointId(pickupPoint.id)}
                      type="radio"
                    />
                    <span className="flex-1 text-sm">
                      <span className="font-semibold text-slate-950">{pickupPoint.name}</span>
                      <span className="mt-1 flex items-center gap-1.5 text-xs text-slate-500">
                        <MapPin className="h-3.5 w-3.5" />
                        {pickupPoint.address}
                      </span>
                      {pickupPoint.serviceTime ? <span className="mt-1 block text-xs text-slate-500">服务时间：{pickupPoint.serviceTime}</span> : null}
                    </span>
                  </label>
                ))}
              </div>
            )}
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between gap-3">
              <span className="text-sm font-medium text-slate-700">自提联系人</span>
              <Link className="text-xs font-medium text-emerald-700 hover:text-emerald-800" href={APP_ROUTES.account}>
                管理联系人
              </Link>
            </div>
            {contacts.length === 0 ? (
              <div className={`rounded-2xl border p-4 text-sm ${fallbackContactReady ? "border-emerald-100 bg-emerald-50 text-emerald-700" : "border-amber-100 bg-amber-50 text-amber-700"}`}>
                {fallbackContactReady ? (
                  <div>
                    暂无自提联系人，本次将使用账号资料：
                    <span className="font-semibold">{session?.name}</span>
                    {" · "}
                    <span className="font-semibold">{session?.phone}</span>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <div>暂无可用自提联系人，账号资料也缺少手机号，暂不能提交订单。</div>
                    <Link className="font-semibold text-amber-800 underline underline-offset-4" href={`${APP_ROUTES.account}#pickup`}>
                      去完善自提信息
                    </Link>
                  </div>
                )}
              </div>
            ) : (
              <div className="grid gap-2">
                {contacts.map((contact) => (
                  <label
                    className={`flex cursor-pointer items-start gap-3 rounded-[24px] border p-4 transition ${selectedContactId === contact.id ? "border-emerald-200 bg-emerald-50 shadow-[0_14px_34px_rgba(7,150,105,0.08)]" : "border-slate-900/[0.06] bg-white/72 hover:bg-white"}`}
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
              className="min-h-28 w-full rounded-2xl border border-slate-900/[0.08] bg-white/86 px-4 py-3 text-sm outline-none transition focus:border-emerald-300 focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]"
              maxLength={ORDER_REMARK_MAX_LENGTH}
              placeholder="例如：下班后自提"
              value={remark}
              onChange={(event) => setRemark(event.target.value)}
            />
            <span className="block text-right text-xs text-slate-400">{remark.length}/{ORDER_REMARK_MAX_LENGTH}</span>
          </label>
          <div className="rounded-[24px] border border-slate-900/[0.06] bg-white/80 p-5">
            <div className="flex items-center justify-between text-sm text-slate-500">
              <span>已选商品件数</span>
              <span>{totalQuantity}</span>
            </div>
            <div className="mt-3 flex items-center justify-between">
              <span className="text-sm text-slate-600">应付总额</span>
              <span className="text-3xl font-semibold text-slate-950">{formatCurrency(totalAmount)}</span>
            </div>
          </div>
          <button
            data-testid="cart-submit-order"
            className="inline-flex h-14 w-full items-center justify-center rounded-2xl bg-[#079669] px-5 text-sm font-semibold text-white shadow-[0_18px_36px_rgba(7,150,105,0.22)] transition hover:bg-[#07845d] disabled:cursor-not-allowed disabled:bg-slate-300"
            disabled={!canSubmitOrder}
            onClick={() => setConfirmingOrder(true)}
            type="button"
          >
            <PackageCheck className="mr-2 h-4 w-4" />
            {submitting ? "提交中..." : "提交订单"}
          </button>
          {selectedItems.length === 0 ? (
            <div className="text-center text-xs text-slate-400">请选择需要结算的商品</div>
          ) : null}
          {!hasUsablePickupContact ? (
            <div className="text-center text-xs text-amber-600">请先完善自提联系人或账号手机号</div>
          ) : null}
        </div>
      </Panel>
      {confirmingOrder ? (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/30 px-4 backdrop-blur-sm">
          <Panel className="w-full max-w-md p-7">
            <SectionTitle title="确认提交订单" description="订单提交后会锁定库存，并进入待支付状态。" />
            <div className="mt-5 space-y-3 rounded-[24px] border border-slate-900/[0.06] bg-white/82 p-5 text-sm text-slate-600">
              <div className="flex items-center justify-between"><span>商品件数</span><span>{totalQuantity}</span></div>
              <div className="flex items-center justify-between"><span>应付金额</span><span className="font-semibold text-slate-950">{formatCurrency(totalAmount)}</span></div>
              <div className="flex items-center justify-between gap-4"><span>联系人</span><span className="max-w-52 truncate">{selectedContact ? `${selectedContact.consignee} ${selectedContact.phone}` : "账户资料"}</span></div>
              <div className="flex items-center justify-between gap-4"><span>自提点</span><span className="max-w-52 truncate">{selectedPickupPoint ? selectedPickupPoint.name : "系统默认"}</span></div>
              <div className="flex items-center justify-between"><span>备注</span><span className="max-w-52 truncate">{remark.trim() || "无"}</span></div>
            </div>
            <div className="mt-5 flex gap-3">
              <button
                className="flex-1 rounded-2xl border border-slate-900/[0.08] bg-white px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
                disabled={submitting}
                onClick={() => setConfirmingOrder(false)}
                type="button"
              >
                再看看
              </button>
              <button
                className="flex-1 rounded-2xl bg-[#079669] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#07845d] disabled:cursor-not-allowed disabled:bg-emerald-300"
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
