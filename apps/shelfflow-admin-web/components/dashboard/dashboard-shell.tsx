"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import type { FormEvent, ReactNode } from "react"
import { useState } from "react"
import {
  Bell,
  Bot,
  DollarSign,
  PackageCheck,
  LayoutDashboard,
  LogOut,
  Package,
  PanelLeft,
  Search,
  ShoppingCart,
  Tags,
  TrendingDown,
  User,
  X,
} from "lucide-react"

import { logoutRequest } from "@/lib/client/api"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { glassInputClassName, glassPanel, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

const navigationItems = [
  {
    href: DASHBOARD_ROUTES.overview,
    label: "系统概览",
    icon: LayoutDashboard,
  },
  {
    href: DASHBOARD_ROUTES.products,
    label: "商品管理",
    icon: PackageCheck,
  },
  {
    href: DASHBOARD_ROUTES.batches,
    label: "批次管理",
    icon: Package,
  },
  {
    href: DASHBOARD_ROUTES.pricing,
    label: "定价规则",
    icon: Tags,
  },
  {
    href: DASHBOARD_ROUTES.orderMonitor,
    label: "订单管理",
    icon: ShoppingCart,
  },
  {
    href: DASHBOARD_ROUTES.lossStatistics,
    label: "经营分析",
    icon: TrendingDown,
  },
  {
    href: DASHBOARD_ROUTES.aiAssistant,
    label: "AI 运营助手",
    icon: Bot,
  },
] as const

const notificationItems = [
  {
    href: DASHBOARD_ROUTES.lossStatistics,
    label: "损耗预警",
    description: "查看临期与损耗趋势",
    icon: TrendingDown,
  },
  {
    href: DASHBOARD_ROUTES.orderMonitor,
    label: "订单履约",
    description: "处理待备货和待自提订单",
    icon: PackageCheck,
  },
  {
    href: DASHBOARD_ROUTES.pricing,
    label: "定价建议",
    description: "检查折扣规则与 AI 建议",
    icon: DollarSign,
  },
] as const

const GLOBAL_SEARCH_FIELD_NAME = "globalKeyword"
const SIDEBAR_DEFAULT_WIDTH = 256

function resolvePageTitle(pathname: string) {
  const current = navigationItems.find((item) => pathname === item.href || pathname.startsWith(`${item.href}/`))
  return current?.label ?? "系统概览"
}

function resolveGlobalSearchRoute(keyword: string) {
  const normalizedKeyword = keyword.trim().toLowerCase()

  if (normalizedKeyword.includes("ord") || normalizedKeyword.includes("订单")) {
    return DASHBOARD_ROUTES.orderMonitor
  }

  if (normalizedKeyword.includes("商品") || normalizedKeyword.includes("product")) {
    return DASHBOARD_ROUTES.products
  }

  if (normalizedKeyword.includes("批次") || normalizedKeyword.includes("batch")) {
    return DASHBOARD_ROUTES.batches
  }

  if (normalizedKeyword.includes("规则") || normalizedKeyword.includes("折扣") || normalizedKeyword.includes("price")) {
    return DASHBOARD_ROUTES.pricing
  }

  if (normalizedKeyword.includes("损耗") || normalizedKeyword.includes("loss")) {
    return DASHBOARD_ROUTES.lossStatistics
  }

  return DASHBOARD_ROUTES.batches
}

export function DashboardShell(props: {
  children: ReactNode
  name: string
  userName: string
}) {
  const pathname = usePathname()
  const router = useRouter()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [notificationOpen, setNotificationOpen] = useState(false)
  const [globalKeyword, setGlobalKeyword] = useState("")
  const pageTitle = resolvePageTitle(pathname)

  async function handleLogout() {
    await logoutRequest()
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }

  function handleGlobalSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)
    const keyword = String(formData.get(GLOBAL_SEARCH_FIELD_NAME) ?? "").trim()
    if (!keyword) {
      return
    }

    const params = new URLSearchParams({ keyword })
    router.push(`${resolveGlobalSearchRoute(keyword)}?${params.toString()}`)
    setMobileMenuOpen(false)
  }

  function renderNavigation(options?: { mobile?: boolean }) {
    return navigationItems.map((item) => {
      const Icon = item.icon
      const active = pathname === item.href || pathname.startsWith(`${item.href}/`)

      return (
        <Link
          key={item.href}
          href={item.href}
          onClick={() => {
            if (options?.mobile) {
              setMobileMenuOpen(false)
            }
          }}
          className={cn(
            "flex w-full items-center rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-200",
            "gap-3",
            active ? "text-white" : "text-slate-700 hover:bg-white/40 hover:text-slate-900"
          )}
          style={active ? { background: primaryGradient, boxShadow: "0 4px 14px rgba(12, 17, 91, 0.3)" } : undefined}
        >
          <Icon className={cn("h-5 w-5 flex-shrink-0", active ? "text-white" : "text-slate-600")} />
          <span className="truncate">{item.label}</span>
        </Link>
      )
    })
  }

  return (
    <div
      className="min-h-screen"
      style={{
        backgroundImage: "url('/images/gradient-background.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundAttachment: "fixed",
      }}
    >
      <div className="min-h-screen bg-white/10">
        <div className="relative z-10 flex min-h-screen">
          <aside
            className="relative hidden shrink-0 flex-col lg:flex"
            style={{
              ...glassPanel,
              width: SIDEBAR_DEFAULT_WIDTH,
              borderTop: 0,
              borderBottom: 0,
              borderLeft: 0,
              borderRadius: 0,
              boxShadow: "4px 0 24px rgba(15, 23, 42, 0.1), inset -1px 0 0 rgba(255, 255, 255, 0.3)",
            }}
          >
            <div className="flex h-16 items-center gap-2 px-4" style={{ borderBottom: "1px solid rgba(255, 255, 255, 0.3)" }}>
              <div className="flex min-w-0 items-center">
                <div
                  className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl"
                  style={{ background: primaryGradient, boxShadow: primaryShadow }}
                >
                  <Package className="h-5 w-5 text-white" />
                </div>
                <div className="ml-3 min-w-0">
                  <h1 className="truncate font-bold text-slate-800">ShelfFlow</h1>
                  <p className="truncate text-xs text-slate-600">临期库存管理</p>
                </div>
              </div>
            </div>

            <nav className="flex-1 space-y-1 px-3 py-4">{renderNavigation()}</nav>
          </aside>

          <div className="flex min-h-screen min-w-0 flex-1 flex-col">
            <header
              className="relative z-30 flex h-16 items-center justify-between gap-4 px-6"
              style={{
                ...glassPanel,
                borderTop: 0,
                borderLeft: 0,
                borderRight: 0,
                borderRadius: 0,
                boxShadow: "0 4px 24px rgba(15, 23, 42, 0.05), inset 0 -1px 0 rgba(255, 255, 255, 0.3)",
              }}
            >
              <div className="flex items-center gap-4">
                <button
                  aria-label="打开管理端菜单"
                  className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-white/30 text-slate-700 transition-all hover:bg-white/50 lg:hidden"
                  onClick={() => setMobileMenuOpen(true)}
                  type="button"
                >
                  <PanelLeft className="h-4 w-4" />
                </button>
                <h2 className="text-xl font-semibold text-slate-800">{pageTitle}</h2>
              </div>

              <div className="hidden max-w-md flex-1 md:block">
                <form className="relative" onSubmit={handleGlobalSearch}>
                  <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
                  <Input
                    aria-label="全局搜索"
                    className={cn("pl-10 pr-12", glassInputClassName)}
                    name={GLOBAL_SEARCH_FIELD_NAME}
                    onChange={(event) => setGlobalKeyword(event.target.value)}
                    placeholder="搜索商品、批次、订单..."
                    value={globalKeyword}
                  />
                  <button
                    aria-label="提交全局搜索"
                    className="absolute right-2 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg text-slate-600 transition-all hover:bg-white/50 hover:text-slate-900"
                    type="submit"
                  >
                    <Search className="h-4 w-4" />
                  </button>
                </form>
              </div>

              <div className="flex items-center gap-3">
                <div className="relative">
                  <Button
                    aria-expanded={notificationOpen}
                    aria-label="打开运营提醒"
                    className="relative hover:bg-white/30"
                    onClick={() => setNotificationOpen((value) => !value)}
                    size="icon"
                    type="button"
                    variant="ghost"
                  >
                    <Bell className="h-5 w-5 text-slate-700" />
                    <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-red-500 shadow-sm" />
                  </Button>
                  {notificationOpen ? (
                    <div
                      className="absolute right-0 top-12 z-40 w-80 overflow-hidden rounded-2xl p-2"
                      style={{
                        ...glassPanel,
                        boxShadow: "0 18px 50px rgba(15, 23, 42, 0.18)",
                      }}
                    >
                      <div className="px-3 py-2">
                        <p className="text-sm font-semibold text-slate-900">运营提醒</p>
                        <p className="text-xs text-slate-600">快速进入需要优先关注的模块</p>
                      </div>
                      <div className="space-y-1">
                        {notificationItems.map((item) => {
                          const Icon = item.icon

                          return (
                            <button
                              className="flex w-full items-start gap-3 rounded-xl px-3 py-2.5 text-left transition-all hover:bg-white/45"
                              key={item.href}
                              onClick={(event) => {
                                event.preventDefault()
                                setNotificationOpen(false)
                                router.push(item.href)
                              }}
                              type="button"
                            >
                              <span className="mt-0.5 rounded-lg bg-white/50 p-2 text-slate-700">
                                <Icon className="h-4 w-4" />
                              </span>
                              <span className="min-w-0">
                                <span className="block text-sm font-medium text-slate-800">{item.label}</span>
                                <span className="block text-xs text-slate-600">{item.description}</span>
                              </span>
                            </button>
                          )
                        })}
                      </div>
                    </div>
                  ) : null}
                </div>
                <div className="hidden items-center gap-2 rounded-xl px-3 py-2 transition-all hover:bg-white/30 sm:flex">
                  <div
                    className="flex h-8 w-8 items-center justify-center rounded-full"
                    style={{ background: primaryGradient, boxShadow: "0 2px 8px rgba(12, 17, 91, 0.3)" }}
                  >
                    <User className="h-4 w-4 text-white" />
                  </div>
                  <span className="max-w-32 truncate text-sm font-medium text-slate-700">{props.name || props.userName}</span>
                </div>
                <Button onClick={handleLogout} size="sm" type="button" variant="ghost" className="hover:bg-white/30">
                  <LogOut className="mr-2 h-4 w-4" />
                  退出登录
                </Button>
              </div>
            </header>
            <main className="min-w-0 flex-1 overflow-auto p-6">{props.children}</main>
          </div>
        </div>
      </div>
      {mobileMenuOpen ? (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button
            aria-label="关闭管理端菜单"
            className="absolute inset-0 bg-slate-950/30"
            onClick={() => setMobileMenuOpen(false)}
            type="button"
          />
          <aside
            className="relative flex h-full w-72 flex-col p-4"
            style={{
              ...glassPanel,
              borderBottomLeftRadius: 0,
              borderTopLeftRadius: 0,
              boxShadow: "12px 0 40px rgba(15, 23, 42, 0.18)",
            }}
          >
            <div className="flex items-center justify-between pb-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl" style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                  <Package className="h-5 w-5 text-white" />
                </div>
                <div className="min-w-0">
                  <h1 className="truncate font-bold text-slate-800">ShelfFlow</h1>
                  <p className="truncate text-xs text-slate-600">临期库存管理</p>
                </div>
              </div>
              <button
                aria-label="关闭菜单"
                className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-white/30 text-slate-700 transition-all hover:bg-white/50"
                onClick={() => setMobileMenuOpen(false)}
                type="button"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form className="relative mb-4" onSubmit={handleGlobalSearch}>
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
              <Input
                aria-label="移动端全局搜索"
                className={cn("pl-10 pr-12", glassInputClassName)}
                name={GLOBAL_SEARCH_FIELD_NAME}
                onChange={(event) => setGlobalKeyword(event.target.value)}
                placeholder="搜索商品、批次、订单..."
                value={globalKeyword}
              />
              <button
                aria-label="提交移动端全局搜索"
                className="absolute right-2 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg text-slate-600 transition-all hover:bg-white/50 hover:text-slate-900"
                type="submit"
              >
                <Search className="h-4 w-4" />
              </button>
            </form>

            <nav className="flex-1 space-y-1 overflow-y-auto">{renderNavigation({ mobile: true })}</nav>

            <Button className="mt-4 justify-start bg-white/30 hover:bg-white/45" onClick={handleLogout} type="button" variant="ghost">
              <LogOut className="mr-2 h-4 w-4" />
              退出登录
            </Button>
          </aside>
        </div>
      ) : null}
    </div>
  )
}
