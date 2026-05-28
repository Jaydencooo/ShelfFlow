"use client"

import Link from "next/link"
import Image from "next/image"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { ArrowRight, Leaf, MapPin, Search, ShieldCheck, ShoppingCart, Tag } from "lucide-react"

import { APP_ROUTES, CART_CHANGED_EVENT_NAME, DEFAULT_PAGE_SIZE, SUCCESS_TOAST_DISMISS_MS } from "@/lib/constants"
import { addCartItem, getCategories, getProducts, isUnauthorizedError } from "@/lib/client/api"
import { formatCurrency, formatDaysToExpire } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserCatalogCategory, UserCatalogProduct } from "@/lib/types"
import { catalogQuerySchema } from "@/lib/validation"
import { EmptyState, InlineError, PaginationControls, Panel, StatusBadge } from "@/components/common/ui"

export function ProductCatalogPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [categories, setCategories] = useState<UserCatalogCategory[]>([])
  const [products, setProducts] = useState<UserCatalogProduct[]>([])
  const [total, setTotal] = useState(0)
  const [submittingProductId, setSubmittingProductId] = useState<string | null>(null)
  const [keywordDraft, setKeywordDraft] = useState("")
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const appliedQuery = useMemo(() => {
    const parsed = catalogQuerySchema.safeParse({
      page: searchParams.get("page") ?? 1,
      pageSize: DEFAULT_PAGE_SIZE,
        keyword: searchParams.get("keyword") ?? undefined,
        categoryId: searchParams.get("categoryId") ?? undefined,
        sortBy: searchParams.get("sortBy") ?? undefined,
        sortOrder: searchParams.get("sortOrder") ?? undefined
      })

    if (parsed.success) {
      return parsed.data
    }

    return {
      page: 1,
      pageSize: DEFAULT_PAGE_SIZE,
      keyword: undefined,
      categoryId: undefined,
      sortBy: "updatedAt" as const,
      sortOrder: "desc" as const
    }
  }, [searchParams])

  useEffect(() => {
    setKeywordDraft(appliedQuery.keyword ?? "")
  }, [appliedQuery.keyword])

  const loadCatalog = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [categoryItems, productPage] = await Promise.all([
        getCategories(),
        getProducts(appliedQuery)
      ])
      setCategories(categoryItems)
      setProducts(productPage.items)
      setTotal(productPage.total)
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "商品目录加载失败")
    } finally {
      setLoading(false)
    }
  }, [appliedQuery])

  useEffect(() => {
    void loadCatalog()
  }, [loadCatalog])

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timer = window.setTimeout(() => setSuccessMessage(null), SUCCESS_TOAST_DISMISS_MS)
    return () => window.clearTimeout(timer)
  }, [successMessage])

  function replaceSearch(next: {
    page?: number
    keyword?: string
    categoryId?: string
    sortBy?: "updatedAt" | "price" | "daysToExpire"
    sortOrder?: "asc" | "desc"
  }) {
    const nextParams = new URLSearchParams(searchParams.toString())

    if (next.page && next.page > 1) {
      nextParams.set("page", String(next.page))
    } else {
      nextParams.delete("page")
    }

    if (next.keyword) {
      nextParams.set("keyword", next.keyword)
    } else {
      nextParams.delete("keyword")
    }

    if (next.categoryId) {
      nextParams.set("categoryId", next.categoryId)
    } else {
      nextParams.delete("categoryId")
    }

    if (next.sortBy) nextParams.set("sortBy", next.sortBy)
    else nextParams.delete("sortBy")

    if (next.sortOrder) nextParams.set("sortOrder", next.sortOrder)
    else nextParams.delete("sortOrder")

    const nextSearch = nextParams.toString()
    router.replace(nextSearch ? `${pathname}?${nextSearch}` : pathname)
  }

  const selectedCategoryId = appliedQuery.categoryId ?? ""

  return (
    <div className="space-y-7">
      <Panel className="overflow-hidden">
        <div className="grid gap-8 p-6 sm:p-8 lg:grid-cols-[1fr_0.86fr] lg:p-10">
          <div className="space-y-7">
            <div className="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-4 py-2 text-sm font-semibold text-[#079669]">
              <Leaf className="h-4 w-4" />
              社区自提 · 临期好物
            </div>
            <div className="space-y-4">
              <h1 className="max-w-3xl text-5xl font-semibold leading-tight tracking-tight text-slate-950 sm:text-6xl">
                把临期好物
                <span className="block text-[#079669]">变成日常补给</span>
              </h1>
              <p className="max-w-xl text-base leading-8 text-slate-500">搜索附近可自提商品，按分类、价格和到期时间挑选适合今天带走的临期特惠。</p>
            </div>
            <div className="grid gap-3 sm:grid-cols-3">
              {[
                { label: "超值优惠", value: "低至 1 折起", icon: Tag },
                { label: "社区自提", value: "就近自提更方便", icon: MapPin },
                { label: "品质保证", value: "库存真实可见", icon: ShieldCheck }
              ].map((item) => {
                const Icon = item.icon
                return (
                  <div className="rounded-[24px] border border-slate-900/[0.06] bg-white/72 p-4 shadow-sm" key={item.label}>
                    <Icon className="h-5 w-5 text-[#079669]" />
                    <div className="mt-3 text-sm font-semibold text-slate-950">{item.label}</div>
                    <div className="mt-1 text-xs text-slate-500">{item.value}</div>
                  </div>
                )
              })}
            </div>
            <form
              className="relative max-w-3xl rounded-[28px] border border-slate-900/[0.08] bg-white/82 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur"
              onSubmit={async (event) => {
                event.preventDefault()
                replaceSearch({
                  page: 1,
                  keyword: keywordDraft.trim() || undefined,
                  categoryId: selectedCategoryId || undefined,
                  sortBy: appliedQuery.sortBy,
                  sortOrder: appliedQuery.sortOrder
                })
              }}
            >
              <div className="flex flex-col gap-3 sm:flex-row">
                <div className="relative flex-1">
                  <Search className="pointer-events-none absolute left-5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input
                    data-testid="catalog-search-input"
                    className="h-14 w-full rounded-2xl border border-slate-900/[0.08] bg-white px-12 text-sm outline-none transition focus:border-emerald-300 focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]"
                    placeholder="搜索牛奶、面包、轻食..."
                    value={keywordDraft}
                    onChange={(event) => setKeywordDraft(event.target.value)}
                  />
                </div>
                <button className="inline-flex h-14 items-center justify-center gap-2 rounded-2xl bg-slate-950 px-6 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(15,23,42,0.16)] transition hover:bg-slate-800" data-testid="catalog-search-submit" type="submit">
                  搜索
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>
            </form>
          </div>
          <div className="relative hidden min-h-[460px] overflow-hidden rounded-[32px] bg-[#EEF7EA] lg:block">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_74%_30%,rgba(16,166,106,0.18),transparent_32%),radial-gradient(circle_at_18%_82%,rgba(255,255,255,0.9),transparent_30%)]" />
            <div className="absolute left-10 top-10 rounded-full bg-[#079669] px-6 py-5 text-center text-white shadow-[0_18px_50px_rgba(7,150,105,0.22)]">
              <div className="text-sm">最高省</div>
              <div className="text-4xl font-semibold">70%</div>
            </div>
            <div className="absolute bottom-20 left-12 h-48 w-36 rotate-[-7deg] rounded-[34px] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.10)]" />
            <div className="absolute bottom-48 left-20 text-center text-base font-bold text-[#079669]">OAT<br />MILK</div>
            <div className="absolute bottom-16 right-14 h-56 w-40 rotate-[5deg] rounded-[32px] bg-slate-950 shadow-[0_24px_60px_rgba(15,23,42,0.18)]" />
            <div className="absolute bottom-36 right-24 text-center text-sm font-bold text-white">LOCAL<br />GOODS</div>
            <div className="absolute bottom-12 right-48 h-28 w-48 rounded-full bg-lime-200/60" />
            <div className="absolute bottom-8 left-10 right-10 rounded-[24px] bg-white/72 px-5 py-4 text-sm font-semibold text-slate-700 shadow-sm">每天更新附近可自提好物，低价优质，先到先得。</div>
          </div>
        </div>
      </Panel>

      <Panel className="space-y-4 p-5 sm:p-6">
        <div className="flex flex-wrap gap-2">
          <button
            className={`rounded-full px-5 py-3 text-sm font-semibold transition ${selectedCategoryId ? "bg-slate-100 text-slate-600 hover:bg-slate-200" : "bg-[#079669] text-white shadow-[0_12px_28px_rgba(7,150,105,0.18)]"}`}
            onClick={() => replaceSearch({ page: 1, keyword: appliedQuery.keyword, categoryId: undefined, sortBy: appliedQuery.sortBy, sortOrder: appliedQuery.sortOrder })}
            type="button"
          >
            全部
          </button>
          {categories.map((category) => (
            <button
              className={`rounded-full px-5 py-3 text-sm font-semibold transition ${selectedCategoryId === category.id ? "bg-[#079669] text-white shadow-[0_12px_28px_rgba(7,150,105,0.18)]" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
              key={category.id}
              onClick={() => replaceSearch({ page: 1, keyword: appliedQuery.keyword, categoryId: category.id, sortBy: appliedQuery.sortBy, sortOrder: appliedQuery.sortOrder })}
              type="button"
            >
              {category.name}
            </button>
          ))}
        </div>
        <div className="flex flex-wrap gap-2">
          {[
            { label: "最新上架", sortBy: "updatedAt" as const, sortOrder: "desc" as const },
            { label: "价格最低", sortBy: "price" as const, sortOrder: "asc" as const },
            { label: "价格最高", sortBy: "price" as const, sortOrder: "desc" as const },
            { label: "临近到期", sortBy: "daysToExpire" as const, sortOrder: "asc" as const }
          ].map((option) => {
            const isActive = appliedQuery.sortBy === option.sortBy && appliedQuery.sortOrder === option.sortOrder
            return (
              <button
                className={`rounded-full px-5 py-3 text-sm font-semibold transition ${isActive ? "bg-slate-950 text-white shadow-[0_12px_28px_rgba(15,23,42,0.14)]" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                data-testid={`catalog-sort-${option.sortBy}-${option.sortOrder}`}
                key={`${option.sortBy}-${option.sortOrder}`}
                onClick={() =>
                  replaceSearch({
                    page: 1,
                    keyword: appliedQuery.keyword,
                    categoryId: selectedCategoryId || undefined,
                    sortBy: option.sortBy,
                    sortOrder: option.sortOrder
                  })
                }
                type="button"
              >
                {option.label}
              </button>
            )
          })}
        </div>
      </Panel>

      {error ? <InlineError message={error} /> : null}
      {loading ? <Panel className="p-8 text-sm text-slate-500">加载商品中...</Panel> : null}
      {!loading && !error && products.length === 0 ? (
        <EmptyState title="暂无可售商品" description="当前筛选条件下没有可售库存，稍后再看或调整搜索条件。" />
      ) : null}
      {!loading && !error && products.length > 0 ? (
        <>
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {products.map((product) => (
              <Panel className="group flex flex-col overflow-hidden p-4 transition hover:-translate-y-1 hover:shadow-[0_30px_80px_rgba(15,23,42,0.10)]" data-testid={`catalog-product-card-${product.id}`} key={product.id}>
                <div className="relative aspect-[4/3] overflow-hidden rounded-[22px] bg-[#EEF7EA]">
                  <div className="absolute inset-0 flex flex-col items-center justify-center bg-[radial-gradient(circle_at_70%_25%,rgba(16,166,106,0.14),transparent_30%),linear-gradient(135deg,#F7FAF6,#ECF6ED)] px-6 text-center">
                    <Leaf className="h-8 w-8 text-[#079669]" />
                    <div className="mt-3 text-sm font-semibold text-slate-700">{product.categoryName || "ShelfFlow"}</div>
                    <div className="mt-1 line-clamp-2 text-xs text-slate-500">{product.name}</div>
                  </div>
                  <Image alt={product.name} className="relative h-full w-full object-cover transition duration-300 group-hover:scale-105" height={360} src={product.image || "https://images.unsplash.com/photo-1514996937319-344454492b37?w=1200&auto=format&fit=crop&q=80"} width={480} />
                </div>
                <div className="mt-4 flex-1 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-xl font-semibold leading-snug text-slate-950">{product.name}</h3>
                      <p className="text-sm text-slate-500">{product.categoryName}</p>
                    </div>
                    <StatusBadge tone={product.availableQuantity > 0 ? "success" : "danger"}>{product.availableQuantity > 0 ? `${product.availableQuantity} 件` : "已售罄"}</StatusBadge>
                  </div>
                  <p className="line-clamp-2 text-sm leading-6 text-slate-600">{product.description || "暂无商品描述"}</p>
                  <div className="flex items-end justify-between gap-3">
                    <div>
                      <div className="text-xs text-slate-400 line-through">{formatCurrency(product.listPrice)}</div>
                      <div className="text-3xl font-semibold text-[#079669]">{formatCurrency(product.currentPrice)}</div>
                    </div>
                    <div className="text-right text-xs text-slate-500">
                      <div className={typeof product.daysToExpire === "number" && product.daysToExpire <= 3 ? "font-medium text-amber-700" : ""}>{formatDaysToExpire(product.daysToExpire)}</div>
                      <div>{product.nearestExpiryDate || "到期时间待定"}</div>
                    </div>
                  </div>
                </div>
                <div className="mt-5 flex gap-3">
                  <Link className="flex-1 rounded-2xl border border-slate-900/[0.08] bg-white px-4 py-3 text-center text-sm font-semibold text-slate-700 transition hover:border-emerald-200 hover:text-[#079669]" data-testid={`catalog-detail-${product.id}`} href={`${APP_ROUTES.products}/${product.id}`}>
                    查看详情
                  </Link>
                  <button
                    data-testid={`catalog-add-cart-${product.id}`}
                    className="inline-flex items-center justify-center rounded-2xl bg-[#079669] px-5 py-3 text-sm font-semibold text-white shadow-[0_12px_28px_rgba(7,150,105,0.18)] transition hover:bg-[#07845d] disabled:cursor-not-allowed disabled:bg-emerald-300"
                    disabled={submittingProductId === product.id || product.availableQuantity < 1}
                    onClick={async () => {
                      setSubmittingProductId(product.id)
                      setError(null)
                      setSuccessMessage(null)
                      try {
                        await addCartItem({ productId: product.id, quantity: 1 })
                        window.dispatchEvent(new Event(CART_CHANGED_EVENT_NAME))
                        setSuccessMessage(`${product.name} 已加入购物车`)
                      } catch (submitError) {
                        if (isUnauthorizedError(submitError)) {
                          router.push(buildLoginRedirectPath(`${pathname}?${searchParams.toString()}`))
                          return
                        }
                        setError(submitError instanceof Error ? submitError.message : "加入购物车失败")
                      } finally {
                        setSubmittingProductId(null)
                      }
                    }}
                    type="button"
                  >
                    <ShoppingCart className="mr-2 h-4 w-4" />
                    {product.availableQuantity < 1 ? "已售罄" : submittingProductId === product.id ? "加入中" : "加购"}
                  </button>
                </div>
              </Panel>
            ))}
          </div>
          <PaginationControls
            page={appliedQuery.page}
            pageSize={appliedQuery.pageSize}
            total={total}
            onPageChange={(nextPage) =>
              replaceSearch({
                page: nextPage,
                keyword: appliedQuery.keyword,
                categoryId: selectedCategoryId || undefined,
                sortBy: appliedQuery.sortBy,
                sortOrder: appliedQuery.sortOrder
              })
            }
          />
        </>
      ) : null}
      {successMessage ? (
        <div className="fixed bottom-5 left-1/2 z-30 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 rounded-[24px] border border-emerald-100 bg-white px-4 py-3 shadow-[0_20px_60px_rgba(7,150,105,0.18)]">
          <div className="flex items-center justify-between gap-3">
            <div className="text-sm font-medium text-emerald-700">{successMessage}</div>
            <Link className="text-sm font-semibold text-slate-900 hover:text-emerald-700" href={APP_ROUTES.cart}>
              去购物车
            </Link>
          </div>
        </div>
      ) : null}
    </div>
  )
}
