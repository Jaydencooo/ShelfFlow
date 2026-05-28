"use client"

import Image from "next/image"
import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { ArrowRight, Clock3, MapPin, PackageCheck, Search, ShieldCheck, ShoppingCart, Sparkles } from "lucide-react"

import { APP_ROUTES, HOME_FEATURED_PAGE_SIZE } from "@/lib/constants"
import { getCategories, getProducts } from "@/lib/client/api"
import { formatCurrency, formatDaysToExpire } from "@/lib/formatters"
import type { UserCatalogCategory, UserCatalogProduct } from "@/lib/types"
import { EmptyState, InlineError, Panel, StatusBadge } from "@/components/common/ui"

const valueCards = [
  {
    title: "临期特惠",
    description: "围绕到期天数和库存做动态展示，帮助用户快速找到高性价比商品。",
    icon: Clock3
  },
  {
    title: "社区自提",
    description: "下单后到固定自提点领取，减少配送成本，也方便门店集中履约。",
    icon: MapPin
  },
  {
    title: "库存可见",
    description: "只展示当前可购买的商品和可用库存，减少下单后的不确定性。",
    icon: ShieldCheck
  }
] as const

export function HomePagePanel() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [categories, setCategories] = useState<UserCatalogCategory[]>([])
  const [featuredProducts, setFeaturedProducts] = useState<UserCatalogProduct[]>([])
  const [totalProducts, setTotalProducts] = useState(0)

  const loadHome = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const [categoryItems, productPage] = await Promise.all([
        getCategories(),
        getProducts({
          page: 1,
          pageSize: HOME_FEATURED_PAGE_SIZE,
          sortBy: "daysToExpire",
          sortOrder: "asc"
        })
      ])
      setCategories(categoryItems)
      setFeaturedProducts(productPage.items)
      setTotalProducts(productPage.total)
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "首页数据加载失败")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadHome()
  }, [loadHome])

  const nearestExpiryCount = useMemo(
    () => featuredProducts.filter((product) => typeof product.daysToExpire === "number" && product.daysToExpire <= 3).length,
    [featuredProducts]
  )

  return (
    <div className="space-y-8">
      <section className="grid gap-6 lg:grid-cols-[1.08fr_0.92fr]">
        <Panel className="overflow-hidden p-6 sm:p-8">
          <div className="space-y-6">
            <StatusBadge tone="success">社区自提型临期零售</StatusBadge>
            <div className="space-y-4">
              <h1 className="max-w-3xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
                把附近门店的临期好物，变成可自提的日常补给。
              </h1>
              <p className="max-w-2xl text-base leading-7 text-slate-600">
                你可以先逛商品目录，登录后完成加购、结算、自提信息维护和订单跟踪。
              </p>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row">
              <Link className="inline-flex items-center justify-center gap-2 rounded-2xl bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700" href={APP_ROUTES.products}>
                去逛特惠商品
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link className="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50" href={APP_ROUTES.account}>
                完善自提信息
                <MapPin className="h-4 w-4" />
              </Link>
            </div>
            <div className="grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl bg-emerald-50 px-4 py-3">
                <div className="text-2xl font-semibold text-emerald-700">{totalProducts}</div>
                <div className="text-xs text-emerald-700/80">可售商品</div>
              </div>
              <div className="rounded-2xl bg-sky-50 px-4 py-3">
                <div className="text-2xl font-semibold text-sky-700">{categories.length}</div>
                <div className="text-xs text-sky-700/80">商品分类</div>
              </div>
              <div className="rounded-2xl bg-amber-50 px-4 py-3">
                <div className="text-2xl font-semibold text-amber-700">{nearestExpiryCount}</div>
                <div className="text-xs text-amber-700/80">优先自提推荐</div>
              </div>
            </div>
          </div>
        </Panel>

        <div className="grid gap-4">
          {valueCards.map((item) => {
            const Icon = item.icon
            return (
              <Panel className="p-5" key={item.title}>
                <div className="flex gap-4">
                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-slate-900 text-white">
                    <Icon className="h-5 w-5" />
                  </div>
                  <div>
                    <h2 className="font-semibold text-slate-950">{item.title}</h2>
                    <p className="mt-1 text-sm leading-6 text-slate-500">{item.description}</p>
                  </div>
                </div>
              </Panel>
            )
          })}
        </div>
      </section>

      <Panel className="p-5 sm:p-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
              <Sparkles className="h-3.5 w-3.5" />
              今日优先推荐
            </div>
            <h2 className="mt-3 text-2xl font-semibold text-slate-950">临近到期，适合尽快自提</h2>
            <p className="mt-1 text-sm text-slate-500">按到期时间从近到远展示，优先推荐适合尽快自提的商品。</p>
          </div>
          <Link className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50" href={APP_ROUTES.products}>
            查看全部
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>

        {error ? <div className="mt-5"><InlineError message={error} /></div> : null}
        {loading ? <div className="mt-5 rounded-2xl bg-slate-50 p-6 text-sm text-slate-500">加载推荐商品中...</div> : null}
        {!loading && !error && featuredProducts.length === 0 ? (
          <div className="mt-5">
            <EmptyState title="暂无可售商品" description="附近暂时没有可自提商品，稍后再来看看。" />
          </div>
        ) : null}
        {!loading && !error && featuredProducts.length > 0 ? (
          <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {featuredProducts.map((product) => (
              <Link className="group rounded-2xl border border-slate-100 bg-white p-4 transition hover:-translate-y-0.5 hover:border-emerald-100 hover:shadow-lg hover:shadow-emerald-100/60" href={`${APP_ROUTES.products}/${product.id}`} key={product.id}>
                <div className="aspect-[4/3] overflow-hidden rounded-xl bg-slate-100">
                  <Image alt={product.name} className="h-full w-full object-cover transition duration-300 group-hover:scale-105" height={360} src={product.image || "https://images.unsplash.com/photo-1514996937319-344454492b37?w=1200&auto=format&fit=crop&q=80"} width={480} />
                </div>
                <div className="mt-4 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="font-semibold text-slate-950">{product.name}</h3>
                      <p className="text-sm text-slate-500">{product.categoryName}</p>
                    </div>
                    <StatusBadge tone={product.availableQuantity > 0 ? "success" : "warning"}>{product.availableQuantity} 件</StatusBadge>
                  </div>
                  <div className="flex items-end justify-between gap-3">
                    <div>
                      <div className="text-xs text-slate-400 line-through">{formatCurrency(product.listPrice)}</div>
                      <div className="text-xl font-semibold text-emerald-700">{formatCurrency(product.currentPrice)}</div>
                    </div>
                    <div className="text-right text-xs text-slate-500">
                      <div>{formatDaysToExpire(product.daysToExpire)}</div>
                      <div>{product.nearestExpiryDate || "到期时间待定"}</div>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ) : null}
      </Panel>

      <section className="grid gap-4 md:grid-cols-3">
        {[
          { title: "搜索商品", description: "按类目、关键词和价格排序筛选。", href: APP_ROUTES.products, icon: Search },
          { title: "加入购物车", description: "登录后可多选商品统一结算。", href: APP_ROUTES.cart, icon: ShoppingCart },
          { title: "到点自提", description: "支付后跟踪订单状态和自提信息。", href: APP_ROUTES.orders, icon: PackageCheck }
        ].map((item) => {
          const Icon = item.icon
          return (
            <Link className="rounded-2xl border border-white/70 bg-white/90 p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg hover:shadow-slate-200/70" href={item.href} key={item.title}>
              <Icon className="h-5 w-5 text-emerald-700" />
              <h2 className="mt-4 font-semibold text-slate-950">{item.title}</h2>
              <p className="mt-1 text-sm leading-6 text-slate-500">{item.description}</p>
            </Link>
          )
        })}
      </section>
    </div>
  )
}
