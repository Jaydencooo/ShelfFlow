"use client"

import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { CheckCircle2, KeyRound, MapPin, ReceiptText, RefreshCw, Save, ShoppingCart, Star, Trash2, UserCircle2 } from "lucide-react"

import { APP_ROUTES, ORDER_PAGE_SIZE, PICKUP_CONTACT_DETAIL_MAX_LENGTH, PICKUP_CONTACT_LABEL_MAX_LENGTH } from "@/lib/constants"
import {
  createPickupContact,
  deletePickupContact,
  getCartItems,
  getOrders,
  getPickupContacts,
  getSessionRequest,
  isUnauthorizedError,
  setDefaultPickupContact,
  updatePickupContact,
  updateProfileRequest
} from "@/lib/client/api"
import { formatCurrency, formatDateTime, formatOrderStatusLabel } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { SessionUser, UserCartItem, UserOrderSummary, UserPickupContact, UserPickupContactRequest } from "@/lib/types"
import { InlineError, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

interface ProfileFormState {
  name: string
  phone: string
}

interface ContactFormState {
  id?: string
  consignee: string
  phone: string
  label: string
  detail: string
  defaultContact: boolean
}

const emptyContactForm: ContactFormState = {
  consignee: "",
  phone: "",
  label: "",
  detail: "",
  defaultContact: false
}

function orderStatusTone(status: UserOrderSummary["status"]) {
  if (status === "completed") return "success"
  if (status === "cancelled") return "danger"
  if (status === "pending_payment") return "warning"
  return "info"
}

export function AccountPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const [session, setSession] = useState<SessionUser | null>(null)
  const [profileForm, setProfileForm] = useState<ProfileFormState>({ name: "", phone: "" })
  const [orders, setOrders] = useState<UserOrderSummary[]>([])
  const [cartItems, setCartItems] = useState<UserCartItem[]>([])
  const [contacts, setContacts] = useState<UserPickupContact[]>([])
  const [contactForm, setContactForm] = useState<ContactFormState>(emptyContactForm)
  const [loading, setLoading] = useState(true)
  const [savingProfile, setSavingProfile] = useState(false)
  const [savingContact, setSavingContact] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const reloadContacts = useCallback(async () => {
    const pickupContacts = await getPickupContacts()
    setContacts(pickupContacts)
  }, [])

  const loadAccount = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const [currentSession, orderPage, cart, pickupContacts] = await Promise.all([
        getSessionRequest(),
        getOrders({
          page: 1,
          pageSize: ORDER_PAGE_SIZE,
          sortBy: "orderTime",
          sortOrder: "desc"
        }),
        getCartItems(),
        getPickupContacts()
      ])

      setSession(currentSession)
      setProfileForm({
        name: currentSession.name ?? "",
        phone: currentSession.phone ?? ""
      })
      setOrders(orderPage.items)
      setCartItems(cart)
      setContacts(pickupContacts)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }

      setError(loadError instanceof Error ? loadError.message : "用户中心加载失败")
    } finally {
      setLoading(false)
    }
  }, [pathname, router])

  useEffect(() => {
    void loadAccount()
  }, [loadAccount])

  const metrics = useMemo(() => {
    const pendingPayment = orders.filter((order) => order.status === "pending_payment").length
    const inProgress = orders.filter((order) => order.status === "to_prepare" || order.status === "preparing" || order.status === "ready_for_pickup").length
    const cartQuantity = cartItems.reduce((sum, item) => sum + item.quantity, 0)
    const cartAmount = cartItems.reduce((sum, item) => sum + Number(item.lineAmount), 0)

    return {
      pendingPayment,
      inProgress,
      cartQuantity,
      cartAmount
    }
  }, [cartItems, orders])

  const handleProfileSubmit = useCallback(async () => {
    setSavingProfile(true)
    setError(null)
    setSuccessMessage(null)

    try {
      const updated = await updateProfileRequest(profileForm)
      setSession(updated)
      setProfileForm({
        name: updated.name ?? "",
        phone: updated.phone ?? ""
      })
      setSuccessMessage("资料已更新")
    } catch (saveError) {
      if (isUnauthorizedError(saveError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setError(saveError instanceof Error ? saveError.message : "资料更新失败")
    } finally {
      setSavingProfile(false)
    }
  }, [pathname, profileForm, router])

  const handleContactSubmit = useCallback(async () => {
    setSavingContact(true)
    setError(null)
    setSuccessMessage(null)

    const payload: UserPickupContactRequest = {
      consignee: contactForm.consignee,
      phone: contactForm.phone,
      label: contactForm.label,
      detail: contactForm.detail,
      defaultContact: contactForm.defaultContact
    }

    try {
      if (contactForm.id) {
        await updatePickupContact(contactForm.id, payload)
        setSuccessMessage("自提联系人已更新")
      } else {
        await createPickupContact(payload)
        setSuccessMessage("自提联系人已创建")
      }
      setContactForm(emptyContactForm)
      await reloadContacts()
    } catch (saveError) {
      if (isUnauthorizedError(saveError)) {
        router.replace(buildLoginRedirectPath(pathname))
        return
      }
      setError(saveError instanceof Error ? saveError.message : "自提联系人保存失败")
    } finally {
      setSavingContact(false)
    }
  }, [contactForm, pathname, reloadContacts, router])

  const handleEditContact = useCallback((contact: UserPickupContact) => {
    setContactForm({
      id: contact.id,
      consignee: contact.consignee,
      phone: contact.phone,
      label: contact.label ?? "",
      detail: contact.detail ?? "",
      defaultContact: contact.defaultContact
    })
  }, [])

  const handleSetDefaultContact = useCallback(async (id: string) => {
    setError(null)
    setSuccessMessage(null)
    try {
      await setDefaultPickupContact(id)
      await reloadContacts()
      setSuccessMessage("默认联系人已更新")
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "默认联系人设置失败")
    }
  }, [reloadContacts])

  const handleDeleteContact = useCallback(async (id: string) => {
    setError(null)
    setSuccessMessage(null)
    try {
      await deletePickupContact(id)
      if (contactForm.id === id) {
        setContactForm(emptyContactForm)
      }
      await reloadContacts()
      setSuccessMessage("自提联系人已删除")
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "自提联系人删除失败")
    }
  }, [contactForm.id, reloadContacts])

  if (loading) {
    return <Panel className="p-8 text-sm text-slate-500">加载用户中心中...</Panel>
  }

  return (
    <div className="space-y-6">
      {error ? <InlineError message={error} /> : null}
      {successMessage ? (
        <div className="flex items-center gap-2 rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
          <CheckCircle2 className="h-4 w-4" />
          {successMessage}
        </div>
      ) : null}

      <Panel className="overflow-hidden">
        <div className="grid gap-6 p-6 lg:grid-cols-[1fr_1.2fr] lg:p-8">
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-600 text-white">
              <UserCircle2 className="h-7 w-7" />
            </div>
            <div>
              <h1 className="text-2xl font-semibold text-slate-950">{session?.name || "ShelfFlow 用户"}</h1>
              <p className="mt-1 text-sm text-slate-500">账号：{session?.openId || "-"}</p>
              <p className="mt-1 text-sm text-slate-500">手机号：{session?.phone || "未绑定"}</p>
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            <Panel className="p-4">
              <div className="text-sm text-slate-500">待支付</div>
              <div className="mt-2 text-2xl font-semibold text-slate-950">{metrics.pendingPayment}</div>
            </Panel>
            <Panel className="p-4">
              <div className="text-sm text-slate-500">履约中</div>
              <div className="mt-2 text-2xl font-semibold text-slate-950">{metrics.inProgress}</div>
            </Panel>
            <Panel className="p-4">
              <div className="text-sm text-slate-500">购物车</div>
              <div className="mt-2 text-2xl font-semibold text-slate-950">{metrics.cartQuantity}</div>
              <div className="mt-1 text-xs text-slate-500">{formatCurrency(metrics.cartAmount)}</div>
            </Panel>
          </div>
        </div>
      </Panel>

      <div className="grid gap-6 lg:grid-cols-[0.8fr_1.2fr]">
        <div className="space-y-6">
          <Panel className="p-6">
            <SectionTitle title="个人资料" description="用于订单联系人和账户展示。" />
            <div className="mt-5 grid gap-4">
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                昵称
                <input
                  className="rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="account-profile-name"
                  maxLength={32}
                  onChange={(event) => setProfileForm((current) => ({ ...current, name: event.target.value }))}
                  value={profileForm.name}
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                手机号
                <input
                  className="rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="account-profile-phone"
                  inputMode="tel"
                  maxLength={11}
                  onChange={(event) => setProfileForm((current) => ({ ...current, phone: event.target.value }))}
                  value={profileForm.phone}
                />
              </label>
              <button
                className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300"
                data-testid="account-profile-save"
                disabled={savingProfile}
                onClick={() => void handleProfileSubmit()}
                type="button"
              >
                <Save className="mr-2 h-4 w-4" />
                {savingProfile ? "保存中..." : "保存资料"}
              </button>
            </div>
          </Panel>

          <Panel className="p-6">
            <SectionTitle title="快捷操作" description="基础用户能力统一入口。" />
            <div className="mt-5 grid gap-3">
              <Link className="flex items-center justify-between rounded-2xl border border-slate-100 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-50" href={APP_ROUTES.cart}>
                <span className="inline-flex items-center gap-2"><ShoppingCart className="h-4 w-4" />查看购物车</span>
                <span>{metrics.cartQuantity} 件</span>
              </Link>
              <Link className="flex items-center justify-between rounded-2xl border border-slate-100 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-50" href={APP_ROUTES.orders}>
                <span className="inline-flex items-center gap-2"><ReceiptText className="h-4 w-4" />查看订单</span>
                <span>{orders.length} 条最近订单</span>
              </Link>
              <Link className="flex items-center justify-between rounded-2xl border border-slate-100 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-50" href={`${APP_ROUTES.forgotPassword}?next=${encodeURIComponent(APP_ROUTES.account)}`}>
                <span className="inline-flex items-center gap-2"><KeyRound className="h-4 w-4" />修改登录密码</span>
                <span>安全设置</span>
              </Link>
              <button
                className="flex items-center justify-between rounded-2xl border border-slate-100 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
                onClick={() => void loadAccount()}
                type="button"
              >
                <span className="inline-flex items-center gap-2"><RefreshCw className="h-4 w-4" />刷新用户中心</span>
                <span>同步最新状态</span>
              </button>
            </div>
          </Panel>
        </div>

        <Panel className="p-6">
          <SectionTitle title="自提联系人" description="下单前维护常用联系人，第一位会自动设为默认。" />
          <div className="mt-5 grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
            <div className="space-y-3">
              {contacts.length === 0 ? (
                <div className="rounded-2xl bg-slate-50 p-5 text-sm text-slate-500">暂无联系人，添加后可在账户中心统一维护。</div>
              ) : null}
              {contacts.map((contact) => (
                <div className="rounded-2xl border border-slate-100 p-4" key={contact.id}>
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2 text-sm font-semibold text-slate-950">
                        {contact.consignee}
                        {contact.defaultContact ? <StatusBadge tone="success">默认</StatusBadge> : null}
                      </div>
                      <div className="mt-1 text-xs text-slate-500">{contact.phone}</div>
                      <div className="mt-2 flex items-center gap-2 text-xs text-slate-500">
                        <MapPin className="h-3.5 w-3.5" />
                        <span>{contact.label || "自提"} · {contact.detail || "未填写备注"}</span>
                      </div>
                    </div>
                  </div>
                  <div className="mt-4 flex flex-wrap gap-2">
                    <button className="rounded-xl border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600 transition hover:bg-slate-50" onClick={() => handleEditContact(contact)} type="button">
                      编辑
                    </button>
                    <button
                      className="inline-flex items-center rounded-xl border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={contact.defaultContact}
                      onClick={() => void handleSetDefaultContact(contact.id)}
                      type="button"
                    >
                      <Star className="mr-1.5 h-3.5 w-3.5" />
                      设为默认
                    </button>
                    <button className="inline-flex items-center rounded-xl border border-red-100 px-3 py-2 text-xs font-medium text-red-600 transition hover:bg-red-50" onClick={() => void handleDeleteContact(contact.id)} type="button">
                      <Trash2 className="mr-1.5 h-3.5 w-3.5" />
                      删除
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="grid gap-4 rounded-2xl bg-slate-50 p-4">
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                联系人
                <input
                  className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="pickup-contact-consignee"
                  maxLength={32}
                  onChange={(event) => setContactForm((current) => ({ ...current, consignee: event.target.value }))}
                  value={contactForm.consignee}
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                手机号
                <input
                  className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="pickup-contact-phone"
                  inputMode="tel"
                  maxLength={11}
                  onChange={(event) => setContactForm((current) => ({ ...current, phone: event.target.value }))}
                  value={contactForm.phone}
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                标签
                <input
                  className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="pickup-contact-label"
                  maxLength={PICKUP_CONTACT_LABEL_MAX_LENGTH}
                  onChange={(event) => setContactForm((current) => ({ ...current, label: event.target.value }))}
                  placeholder="例如：公司 / 家"
                  value={contactForm.label}
                />
              </label>
              <label className="grid gap-2 text-sm font-medium text-slate-700">
                备注
                <textarea
                  className="min-h-24 rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-emerald-400"
                  data-testid="pickup-contact-detail"
                  maxLength={PICKUP_CONTACT_DETAIL_MAX_LENGTH}
                  onChange={(event) => setContactForm((current) => ({ ...current, detail: event.target.value }))}
                  placeholder="例如：下班后自提，联系本人"
                  value={contactForm.detail}
                />
              </label>
              <label className="flex items-center gap-2 text-sm font-medium text-slate-700">
                <input
                  checked={contactForm.defaultContact}
                  className="h-4 w-4 rounded border-slate-300 text-emerald-600"
                  data-testid="pickup-contact-default"
                  onChange={(event) => setContactForm((current) => ({ ...current, defaultContact: event.target.checked }))}
                  type="checkbox"
                />
                设为默认联系人
              </label>
              <div className="grid gap-2 sm:grid-cols-2">
                <button
                  className="inline-flex items-center justify-center rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
                  data-testid="pickup-contact-save"
                  disabled={savingContact}
                  onClick={() => void handleContactSubmit()}
                  type="button"
                >
                  <Save className="mr-2 h-4 w-4" />
                  {savingContact ? "保存中..." : contactForm.id ? "保存联系人" : "新增联系人"}
                </button>
                <button className="rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:bg-white" onClick={() => setContactForm(emptyContactForm)} type="button">
                  清空
                </button>
              </div>
            </div>
          </div>
        </Panel>
      </div>

      <Panel className="p-6">
        <SectionTitle title="最近订单" description="展示最近订单状态，完整操作请进入订单页。" />
        <div className="mt-5 space-y-3">
          {orders.length === 0 ? (
            <div className="rounded-2xl bg-slate-50 p-5 text-sm text-slate-500">暂无订单，先去商品目录选购。</div>
          ) : null}
          {orders.slice(0, 5).map((order) => (
            <Link className="block rounded-2xl border border-slate-100 p-4 transition hover:bg-slate-50" href={`${APP_ROUTES.orders}/${order.id}`} key={order.id}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-sm font-semibold text-slate-950">{order.orderNumber}</div>
                  <div className="mt-1 text-xs text-slate-500">{formatDateTime(order.orderTime)}</div>
                </div>
                <StatusBadge tone={orderStatusTone(order.status)}>{formatOrderStatusLabel(order.status)}</StatusBadge>
              </div>
              <div className="mt-3 flex items-center justify-between text-sm">
                <span className="text-slate-500">{order.itemCount} 件商品</span>
                <span className="font-semibold text-slate-900">{formatCurrency(order.totalAmount)}</span>
              </div>
            </Link>
          ))}
        </div>
      </Panel>
    </div>
  )
}
