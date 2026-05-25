"use client"

import Link from "next/link"
import Image from "next/image"
import { useCallback, useEffect, useMemo, useState } from "react"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { Search, ShoppingCart } from "lucide-react"

import { APP_ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants"
import { addCartItem, getCategories, getProducts, isUnauthorizedError } from "@/lib/client/api"
import { formatCurrency, formatDaysToExpire } from "@/lib/formatters"
import { buildLoginRedirectPath } from "@/lib/navigation"
import type { UserCatalogCategory, UserCatalogProduct } from "@/lib/types"
import { catalogQuerySchema } from "@/lib/validation"
import { EmptyState, InlineError, InlineSuccess, PaginationControls, Panel, SectionTitle, StatusBadge } from "@/components/common/ui"

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

  const heroCount = useMemo(() => products.length, [products.length])
  const selectedCategoryId = appliedQuery.categoryId ?? ""

  return (
    <div className="space-y-6">
      <Panel className="overflow-hidden">
        <div className="grid gap-8 p-6 lg:grid-cols-[1.2fr_0.8fr] lg:p-8">
          <div className="space-y-4">
            <StatusBadge tone="success">真实后端已联通</StatusBadge>
            <div className="space-y-3">
              <h1 className="text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">临期好物目录</h1>
              <p className="max-w-2xl text-sm leading-6 text-slate-500">
                面向真实用户链路，展示可售商品、动态价格、到期时间和可用库存。当前页面直接接入 Java 用户服务。
              </p>
            </div>
            <form
              className="flex flex-col gap-3 sm:flex-row"
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
              <div className="relative flex-1">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  data-testid="catalog-search-input"
                  className="w-full rounded-xl border border-slate-200 bg-white px-10 py-3 text-sm outline-none transition focus:border-emerald-400"
                  placeholder="搜索商品名称"
                  value={keywordDraft}
                  onChange={(event) => setKeywordDraft(event.target.value)}
                />
              </div>
              <button className="rounded-xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800" data-testid="catalog-search-submit" type="submit">
                查询
              </button>
            </form>
          </div>
          <div className="grid gap-4 sm:grid-cols-3 lg:grid-cols-1">
            <Panel className="p-5">
              <div className="text-sm text-slate-500">当前类目数</div>
              <div className="mt-3 text-3xl font-semibold text-slate-950">{categories.length}</div>
            </Panel>
            <Panel className="p-5">
              <div className="text-sm text-slate-500">当前页商品数</div>
              <div className="mt-3 text-3xl font-semibold text-slate-950">{heroCount}</div>
            </Panel>
            <Panel className="p-5">
              <div className="text-sm text-slate-500">可售商品总数</div>
              <div className="mt-3 text-3xl font-semibold text-slate-950">{total}</div>
            </Panel>
          </div>
        </div>
      </Panel>

      <Panel className="p-5">
        <SectionTitle title="类目与排序" description="只展示用户侧真实可售商品类目，并支持用户端真实排序口径。" />
        <div className="mt-4 flex flex-wrap gap-2">
          <button
            className={`rounded-full px-4 py-2 text-sm font-medium transition ${selectedCategoryId ? "bg-slate-100 text-slate-600 hover:bg-slate-200" : "bg-emerald-600 text-white"}`}
            onClick={() => replaceSearch({ page: 1, keyword: appliedQuery.keyword, categoryId: undefined, sortBy: appliedQuery.sortBy, sortOrder: appliedQuery.sortOrder })}
            type="button"
          >
            全部
          </button>
          {categories.map((category) => (
            <button
              className={`rounded-full px-4 py-2 text-sm font-medium transition ${selectedCategoryId === category.id ? "bg-emerald-600 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
              key={category.id}
              onClick={() => replaceSearch({ page: 1, keyword: appliedQuery.keyword, categoryId: category.id, sortBy: appliedQuery.sortBy, sortOrder: appliedQuery.sortOrder })}
              type="button"
            >
              {category.name}
            </button>
          ))}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {[
            { label: "最新上架", sortBy: "updatedAt" as const, sortOrder: "desc" as const },
            { label: "价格最低", sortBy: "price" as const, sortOrder: "asc" as const },
            { label: "价格最高", sortBy: "price" as const, sortOrder: "desc" as const },
            { label: "临近到期", sortBy: "daysToExpire" as const, sortOrder: "asc" as const }
          ].map((option) => {
            const isActive = appliedQuery.sortBy === option.sortBy && appliedQuery.sortOrder === option.sortOrder
            return (
              <button
                className={`rounded-full px-4 py-2 text-sm font-medium transition ${isActive ? "bg-slate-900 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
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
      {successMessage ? <InlineSuccess message={successMessage} /> : null}
      {loading ? <Panel className="p-8 text-sm text-slate-500">加载商品中...</Panel> : null}
      {!loading && !error && products.length === 0 ? (
        <EmptyState title="暂无可售商品" description="当前筛选条件下没有可售库存，稍后再看或调整搜索条件。" />
      ) : null}
      {!loading && !error && products.length > 0 ? (
        <>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {products.map((product) => (
              <Panel className="flex flex-col p-5" data-testid={`catalog-product-card-${product.id}`} key={product.id}>
                <div className="aspect-[4/3] overflow-hidden rounded-xl bg-slate-100">
                  <Image alt={product.name} className="h-full w-full object-cover" height={360} src={product.image || "https://images.unsplash.com/photo-1514996937319-344454492b37?w=1200&auto=format&fit=crop&q=80"} width={480} />
                </div>
                <div className="mt-4 flex-1 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-semibold text-slate-950">{product.name}</h3>
                      <p className="text-sm text-slate-500">{product.categoryName}</p>
                    </div>
                    <StatusBadge tone={product.availableQuantity > 0 ? "success" : "warning"}>{product.availableQuantity} 件</StatusBadge>
                  </div>
                  <p className="line-clamp-2 text-sm leading-6 text-slate-600">{product.description || "暂无商品描述"}</p>
                  <div className="flex items-end justify-between gap-3">
                    <div>
                      <div className="text-xs text-slate-400 line-through">{formatCurrency(product.listPrice)}</div>
                      <div className="text-2xl font-semibold text-emerald-700">{formatCurrency(product.currentPrice)}</div>
                    </div>
                    <div className="text-right text-xs text-slate-500">
                      <div>{formatDaysToExpire(product.daysToExpire)}</div>
                      <div>{product.nearestExpiryDate || "到期时间待定"}</div>
                    </div>
                  </div>
                </div>
                <div className="mt-5 flex gap-3">
                  <Link className="flex-1 rounded-xl border border-slate-200 px-4 py-3 text-center text-sm font-medium text-slate-700 transition hover:border-slate-300 hover:bg-slate-50" data-testid={`catalog-detail-${product.id}`} href={`${APP_ROUTES.products}/${product.id}`}>
                    查看详情
                  </Link>
                  <button
                    data-testid={`catalog-add-cart-${product.id}`}
                    className="inline-flex items-center justify-center rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
                    disabled={submittingProductId === product.id || product.availableQuantity < 1}
                    onClick={async () => {
                      setSubmittingProductId(product.id)
                      setError(null)
                      setSuccessMessage(null)
                      try {
                        await addCartItem({ productId: product.id, quantity: 1 })
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
    </div>
  )
}
