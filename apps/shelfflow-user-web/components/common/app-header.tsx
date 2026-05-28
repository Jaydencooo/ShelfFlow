"use client"

import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { ChevronDown, Home, KeyRound, MapPin, ShoppingCart, ReceiptText, Store, LogOut, UserCircle2 } from "lucide-react"

import { APP_ROUTES, CART_CHANGED_EVENT_NAME } from "@/lib/constants"
import { getCartItems, getSessionRequest, logoutRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, buildRegisterRedirectPath } from "@/lib/navigation"
import type { SessionUser } from "@/lib/types"

const navItems = [
  { href: APP_ROUTES.home, label: "首页", icon: Home },
  { href: APP_ROUTES.cart, label: "购物车", icon: ShoppingCart },
  { href: APP_ROUTES.orders, label: "订单", icon: ReceiptText },
  { href: APP_ROUTES.account, label: "我的", icon: UserCircle2 }
]

function isNavActive(pathname: string, href: string) {
  if (href === APP_ROUTES.home) {
    return pathname === APP_ROUTES.home
  }

  return pathname.startsWith(href)
}

export function AppHeader() {
  const pathname = usePathname()
  const router = useRouter()
  const [session, setSession] = useState<SessionUser | null>(null)
  const [cartItemCount, setCartItemCount] = useState(0)
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  const sessionDisplayName = useMemo(
    () => session?.name || session?.phone || session?.openId || "",
    [session]
  )

  const refreshSession = useCallback(async () => {
    try {
      const currentSession = await getSessionRequest()
      setSession(currentSession)

      try {
        const cartItems = await getCartItems()
        setCartItemCount(cartItems.reduce((sum, item) => sum + item.quantity, 0))
      } catch {
        setCartItemCount(0)
      }
    } catch {
      setSession(null)
      setCartItemCount(0)
    }
  }, [])

  useEffect(() => {
    void refreshSession()
  }, [pathname, refreshSession])

  useEffect(() => {
    window.addEventListener(CART_CHANGED_EVENT_NAME, refreshSession)
    return () => window.removeEventListener(CART_CHANGED_EVENT_NAME, refreshSession)
  }, [refreshSession])

  async function handleLogout() {
    await logoutRequest()
    setSession(null)
    setCartItemCount(0)
    setUserMenuOpen(false)
    router.replace(APP_ROUTES.home)
    router.refresh()
  }

  return (
    <header className="sticky top-0 z-20 border-b border-slate-900/[0.06] bg-[#FAFBF8]/88 backdrop-blur-xl">
      <div className="mx-auto flex max-w-[1280px] items-center justify-between gap-5 px-4 py-5 sm:px-6 lg:px-8">
        <div className="flex items-center gap-8">
          <Link className="flex items-center gap-3" href={APP_ROUTES.home}>
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#079669] text-white shadow-[0_12px_30px_rgba(7,150,105,0.22)]">
              <Store className="h-5 w-5" />
            </div>
            <div>
              <div className="text-lg font-semibold leading-5 text-slate-950">ShelfFlow</div>
              <div className="text-xs text-slate-500">社区自提平台</div>
            </div>
          </Link>
          <nav className="hidden items-center gap-7 md:flex">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = isNavActive(pathname, item.href)
              return (
                <Link className={`relative inline-flex items-center gap-2 py-2 text-sm font-semibold transition ${isActive ? "text-[#079669]" : "text-slate-600 hover:text-slate-950"}`} data-testid={`header-nav-${item.href.replace("/", "") || "home"}`} href={item.href} key={item.href}>
                  <Icon className="h-4 w-4" />
                  {item.label}
                  {item.href === APP_ROUTES.cart && cartItemCount > 0 ? (
                    <span className="rounded-full bg-[#079669] px-2 py-0.5 text-xs font-semibold text-white">{cartItemCount}</span>
                  ) : null}
                  {isActive ? <span className="absolute -bottom-2 left-0 right-0 mx-auto h-0.5 w-8 rounded-full bg-[#079669]" /> : null}
                </Link>
              )
            })}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          {session ? (
            <div className="relative">
              <button
                className="inline-flex h-12 items-center gap-2 rounded-full border border-slate-900/[0.06] bg-white/80 px-4 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-white"
                data-testid="header-user-menu"
                onClick={() => setUserMenuOpen((open) => !open)}
                type="button"
              >
                <UserCircle2 className="h-4 w-4" />
                <span className="hidden max-w-28 truncate sm:inline">{sessionDisplayName}</span>
                <ChevronDown className={`h-4 w-4 transition ${userMenuOpen ? "rotate-180" : ""}`} />
              </button>
              {userMenuOpen ? (
                <div className="absolute right-0 mt-3 w-60 overflow-hidden rounded-[24px] border border-slate-900/[0.08] bg-white/95 p-2 shadow-[0_24px_70px_rgba(15,23,42,0.14)] backdrop-blur-xl">
                  {[
                    { href: APP_ROUTES.account, label: "个人资料", icon: UserCircle2 },
                    { href: `${APP_ROUTES.account}#security`, label: "修改密码", icon: KeyRound },
                    { href: `${APP_ROUTES.account}#pickup`, label: "自提信息", icon: MapPin },
                    { href: APP_ROUTES.orders, label: "我的订单", icon: ReceiptText }
                  ].map((item) => {
                    const Icon = item.icon
                    return (
                      <Link
                        className="flex items-center gap-2 rounded-2xl px-3 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-emerald-50 hover:text-emerald-700"
                        href={item.href}
                        key={item.label}
                        onClick={() => setUserMenuOpen(false)}
                      >
                        <Icon className="h-4 w-4" />
                        {item.label}
                      </Link>
                    )
                  })}
                  <button
                    className="mt-1 flex w-full items-center gap-2 rounded-2xl px-3 py-2.5 text-sm font-medium text-red-600 transition hover:bg-red-50"
                    data-testid="header-logout"
                    onClick={() => void handleLogout()}
                    type="button"
                  >
                    <LogOut className="h-4 w-4" />
                    退出登录
                  </button>
                </div>
              ) : null}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link className="rounded-full border border-slate-900/[0.08] bg-white/75 px-5 py-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-white hover:text-slate-950" data-testid="header-register" href={buildRegisterRedirectPath(pathname)}>
                注册
              </Link>
              <Link className="rounded-full bg-slate-950 px-5 py-3 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(15,23,42,0.16)] transition hover:bg-slate-800" data-testid="header-login" href={buildLoginRedirectPath(pathname)}>
                登录
              </Link>
            </div>
          )}
        </div>
      </div>
      <nav className="border-t border-slate-900/[0.06] bg-white/90 px-2 py-2 backdrop-blur md:hidden">
        <div className="mx-auto grid max-w-md grid-cols-4 gap-2">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = isNavActive(pathname, item.href)

            return (
              <Link
                className={`relative flex flex-col items-center justify-center rounded-2xl px-3 py-2 text-xs font-medium transition ${isActive ? "bg-emerald-50 text-[#079669]" : "text-slate-500 hover:bg-slate-50 hover:text-slate-900"}`}
                data-testid={`mobile-nav-${item.href.replace("/", "") || "home"}`}
                href={item.href}
                key={item.href}
              >
                <Icon className="mb-1 h-4 w-4" />
                {item.label}
                {item.href === APP_ROUTES.cart && cartItemCount > 0 ? (
                  <span className="absolute right-5 top-1 rounded-full bg-emerald-600 px-1.5 text-[10px] font-semibold leading-4 text-white">{cartItemCount}</span>
                ) : null}
              </Link>
            )
          })}
        </div>
      </nav>
    </header>
  )
}
