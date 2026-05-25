"use client"

import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { ShoppingCart, ReceiptText, Store, LogOut, UserCircle2 } from "lucide-react"

import { APP_ROUTES } from "@/lib/constants"
import { getCartItems, getSessionRequest, logoutRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, buildRegisterRedirectPath } from "@/lib/navigation"
import type { SessionUser } from "@/lib/types"

const navItems = [
  { href: APP_ROUTES.products, label: "商品目录", icon: Store },
  { href: APP_ROUTES.cart, label: "购物车", icon: ShoppingCart },
  { href: APP_ROUTES.orders, label: "订单", icon: ReceiptText },
  { href: APP_ROUTES.account, label: "我的", icon: UserCircle2 }
]

export function AppHeader() {
  const pathname = usePathname()
  const router = useRouter()
  const [session, setSession] = useState<SessionUser | null>(null)
  const [cartItemCount, setCartItemCount] = useState(0)

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

  async function handleLogout() {
    await logoutRequest()
    setSession(null)
    setCartItemCount(0)
    router.replace(APP_ROUTES.products)
    router.refresh()
  }

  return (
    <header className="sticky top-0 z-20 border-b border-white/60 bg-white/85 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-8">
          <Link className="flex items-center gap-3" href={APP_ROUTES.products}>
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-600 text-white">
              <Store className="h-5 w-5" />
            </div>
            <div>
              <div className="text-sm font-semibold text-emerald-700">ShelfFlow</div>
              <div className="text-xs text-slate-500">用户端</div>
            </div>
          </Link>
          <nav className="hidden items-center gap-5 md:flex">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = pathname.startsWith(item.href)
              return (
                <Link className={`inline-flex items-center gap-2 text-sm font-medium transition ${isActive ? "text-emerald-700" : "text-slate-600 hover:text-slate-900"}`} data-testid={`header-nav-${item.href.replace("/", "") || "products"}`} href={item.href} key={item.href}>
                  <Icon className="h-4 w-4" />
                  {item.label}
                  {item.href === APP_ROUTES.cart && cartItemCount > 0 ? (
                    <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">{cartItemCount}</span>
                  ) : null}
                </Link>
              )
            })}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          {session ? (
            <>
              <div className="hidden items-center gap-2 rounded-full bg-slate-100 px-3 py-2 text-sm text-slate-600 sm:inline-flex">
                <UserCircle2 className="h-4 w-4" />
                {sessionDisplayName}
              </div>
              <button
                className="inline-flex items-center gap-2 rounded-full border border-slate-200 px-3 py-2 text-sm font-medium text-slate-600 transition hover:border-slate-300 hover:text-slate-900"
                data-testid="header-logout"
                onClick={() => void handleLogout()}
                type="button"
              >
                <LogOut className="h-4 w-4" />
                退出
              </button>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link className="rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-300 hover:text-slate-900" data-testid="header-register" href={buildRegisterRedirectPath(pathname)}>
                注册
              </Link>
              <Link className="rounded-full bg-slate-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800" data-testid="header-login" href={buildLoginRedirectPath(pathname)}>
                登录
              </Link>
            </div>
          )}
        </div>
      </div>
      <nav className="border-t border-slate-100 bg-white/90 px-2 py-2 backdrop-blur md:hidden">
        <div className="mx-auto grid max-w-md grid-cols-4 gap-2">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = pathname.startsWith(item.href)

            return (
              <Link
                className={`relative flex flex-col items-center justify-center rounded-2xl px-3 py-2 text-xs font-medium transition ${isActive ? "bg-emerald-50 text-emerald-700" : "text-slate-500 hover:bg-slate-50 hover:text-slate-900"}`}
                data-testid={`mobile-nav-${item.href.replace("/", "") || "products"}`}
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
