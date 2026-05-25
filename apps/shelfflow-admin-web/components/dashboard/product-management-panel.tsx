"use client"

import { zodResolver } from "@hookform/resolvers/zod"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useForm } from "react-hook-form"
import { FolderPlus, Pencil, Plus, RefreshCw, Search, Trash2 } from "lucide-react"

import {
  createAdminProductCategory,
  createProduct,
  deleteAdminProductCategory,
  deleteProduct,
  getAdminProductCategories,
  getProducts,
  isUnauthorizedError,
  logoutRequest,
  updateProduct,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants"
import { formatCurrency } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type { AdminProductCategory, ProductRecord, ProductStatus } from "@/lib/types"
import type { ProductCategoryFormValues, ProductFormValues } from "@/lib/validation"
import { productCategorySchema, productSchema } from "@/lib/validation"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Textarea } from "@/components/ui/textarea"

const productStatusLabels: Record<ProductStatus, string> = {
  active: "起售",
  inactive: "停售",
}

const productStatusClasses: Record<ProductStatus, string> = {
  active: "bg-emerald-100 text-emerald-700",
  inactive: "bg-red-100 text-red-700",
}

const DEFAULT_PRODUCT_SHELF_LIFE_DAYS = 7
const numericIdPattern = /^\d+$/

function isProductStatus(value: string | null): value is ProductStatus {
  return value === "active" || value === "inactive"
}

function normalizeOptionalId(value: string | null) {
  if (!value) {
    return ""
  }
  return numericIdPattern.test(value) ? value : ""
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function MetricCard(props: { label: string; value: string; hint: string }) {
  return (
    <Card className="border-0" style={glassCard}>
      <CardContent className="space-y-1 p-5">
        <p className="text-sm text-slate-500">{props.label}</p>
        <p className="text-2xl font-semibold text-slate-950">{props.value}</p>
        <p className="text-sm text-slate-600">{props.hint}</p>
      </CardContent>
    </Card>
  )
}

export function ProductManagementPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const [products, setProducts] = useState<ProductRecord[]>([])
  const [categories, setCategories] = useState<AdminProductCategory[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(() => {
    const raw = Number(searchParams.get("page") ?? "1")
    return Number.isFinite(raw) && raw > 0 ? raw : 1
  })
  const [draftKeyword, setDraftKeyword] = useState(searchParams.get("keyword") ?? "")
  const [draftCategoryId, setDraftCategoryId] = useState(() => normalizeOptionalId(searchParams.get("categoryId")))
  const [draftStatus, setDraftStatus] = useState(() => {
    const rawStatus = searchParams.get("status")
    return isProductStatus(rawStatus) ? rawStatus : ""
  })
  const [filters, setFilters] = useState<{ keyword?: string; categoryId?: string; status?: ProductStatus }>(() => ({
    keyword: searchParams.get("keyword") || undefined,
    categoryId: normalizeOptionalId(searchParams.get("categoryId")) || undefined,
    status: isProductStatus(searchParams.get("status")) ? searchParams.get("status") as ProductStatus : undefined,
  }))
  const [loading, setLoading] = useState(true)
  const [categoriesLoading, setCategoriesLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [categoriesError, setCategoriesError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [categoryDialogOpen, setCategoryDialogOpen] = useState(false)
  const [editingProduct, setEditingProduct] = useState<ProductRecord | null>(null)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)

  const form = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: "",
      categoryId: "",
      price: 0,
      description: "",
      image: "",
      status: "active",
      shelfLifeDays: DEFAULT_PRODUCT_SHELF_LIFE_DAYS,
    },
  })

  const categoryForm = useForm<ProductCategoryFormValues>({
    resolver: zodResolver(productCategorySchema),
    defaultValues: {
      name: "",
    },
  })

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  useEffect(() => {
    const params = new URLSearchParams()
    params.set("page", String(page))

    if (filters.keyword) {
      params.set("keyword", filters.keyword)
    }
    if (filters.categoryId) {
      params.set("categoryId", filters.categoryId)
    }
    if (filters.status) {
      params.set("status", filters.status)
    }

    router.replace(params.toString() ? `${pathname}?${params.toString()}` : pathname, { scroll: false })
  }, [filters, page, pathname, router])

  const loadCategories = useCallback(async () => {
    setCategoriesLoading(true)
    setCategoriesError(null)

    try {
      setCategories(await getAdminProductCategories())
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setCategoriesError(getErrorMessage(loadError, "加载商品分类失败"))
    } finally {
      setCategoriesLoading(false)
    }
  }, [handleUnauthorized])

  const loadProducts = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await getProducts({
        page,
        pageSize: DEFAULT_PAGE_SIZE,
        keyword: filters.keyword,
        categoryId: filters.categoryId,
        status: filters.status,
      })

      setProducts(result.items)
      setTotal(result.total)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(loadError, "加载商品列表失败"))
    } finally {
      setLoading(false)
    }
  }, [filters.categoryId, filters.keyword, filters.status, handleUnauthorized, page])

  useEffect(() => {
    void loadCategories()
  }, [loadCategories])

  useEffect(() => {
    void loadProducts()
  }, [loadProducts])

  const metrics = useMemo(() => {
    const active = products.filter((product) => product.status === "active").length
    const inactive = products.filter((product) => product.status === "inactive").length
    const categoryCount = new Set(products.map((product) => product.categoryId)).size

    return {
      total: total.toString(),
      active: active.toString(),
      inactive: inactive.toString(),
      categoryCount: categoryCount.toString(),
    }
  }, [products, total])

  const selectedCategory = useMemo(
    () => categories.find((category) => category.id === filters.categoryId),
    [categories, filters.categoryId],
  )

  function resetForm() {
    setEditingProduct(null)
    form.reset({
      name: "",
      categoryId: categories[0]?.id ?? "",
      price: 0,
      description: "",
      image: "",
      status: "active",
      shelfLifeDays: DEFAULT_PRODUCT_SHELF_LIFE_DAYS,
    })
  }

  function openCreateDialog() {
    resetForm()
    setDialogOpen(true)
  }

  function openEditDialog(product: ProductRecord) {
    setEditingProduct(product)
    form.reset({
      name: product.name,
      categoryId: product.categoryId,
      price: product.price,
      description: product.description || "",
      image: product.image || "",
      status: product.status,
      shelfLifeDays: product.shelfLifeDays || DEFAULT_PRODUCT_SHELF_LIFE_DAYS,
    })
    setDialogOpen(true)
  }

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(1)
    setFilters({
      keyword: draftKeyword.trim() || undefined,
      categoryId: normalizeOptionalId(draftCategoryId) || undefined,
      status: isProductStatus(draftStatus) ? draftStatus : undefined,
    })
  }

  function handleResetFilters() {
    setDraftKeyword("")
    setDraftCategoryId("")
    setDraftStatus("")
    setPage(1)
    setFilters({})
  }

  async function handleSubmitProduct(values: ProductFormValues) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      const payload = {
        ...values,
        description: values.description || "",
        image: values.image || "",
        flavors: [],
      }

      if (editingProduct) {
        await updateProduct(editingProduct.id, payload)
        setSuccessMessage(`商品 ${values.name} 已更新`)
      } else {
        await createProduct(payload)
        setSuccessMessage("商品已创建")
      }

      setDialogOpen(false)
      resetForm()
      await loadProducts()
      await loadCategories()
    } catch (submitError) {
      if (isUnauthorizedError(submitError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(submitError, editingProduct ? "商品更新失败" : "商品创建失败"))
    }
  }

  async function handleSubmitCategory(values: ProductCategoryFormValues) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await createAdminProductCategory(values)
      setSuccessMessage(`分类 ${values.name} 已创建`)
      setCategoryDialogOpen(false)
      categoryForm.reset({ name: "" })
      await loadCategories()
    } catch (submitError) {
      if (isUnauthorizedError(submitError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(submitError, "分类创建失败"))
    }
  }

  async function handleDeleteProduct(product: ProductRecord) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await deleteProduct(product.id)
      setSuccessMessage(`商品 ${product.name} 已删除；如商品存在历史批次，系统会执行停售并停用可售批次`)
      if (products.length === 1 && page > 1) {
        setPage((current) => current - 1)
      }
      await loadProducts()
      await loadCategories()
    } catch (deleteError) {
      if (isUnauthorizedError(deleteError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(deleteError, "商品删除失败"))
    }
  }

  function requestDeleteProduct(product: ProductRecord) {
    setConfirmAction({
      title: "确认删除商品",
      description: `删除「${product.name}」。如果该商品已有批次，系统会改为停售并停用可售批次，避免破坏库存和订单历史。`,
      confirmLabel: "删除",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleDeleteProduct(product)
      },
    })
  }

  async function handleDeleteCategory(category: AdminProductCategory) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await deleteAdminProductCategory(category.id)
      setSuccessMessage(`分类 ${category.name} 已删除`)
      if (filters.categoryId === category.id) {
        setDraftCategoryId("")
        setFilters((current) => ({ ...current, categoryId: undefined }))
      }
      await loadCategories()
      await loadProducts()
    } catch (deleteError) {
      if (isUnauthorizedError(deleteError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(deleteError, "分类删除失败"))
    }
  }

  function requestDeleteCategory(category: AdminProductCategory) {
    setConfirmAction({
      title: "确认删除分类",
      description: `删除「${category.name}」。如果该分类下仍有商品，后端会拒绝删除，请先调整商品分类或停售商品。`,
      confirmLabel: "删除",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleDeleteCategory(category)
      },
    })
  }

  function handleSelectCategory(categoryId: string) {
    setDraftCategoryId(categoryId)
    setPage(1)
    setFilters((current) => ({
      ...current,
      categoryId: categoryId || undefined,
    }))
  }

  const totalPages = Math.max(1, Math.ceil(total / DEFAULT_PAGE_SIZE))

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">商品管理</h1>
        <p className="text-sm text-slate-600">维护商品资料、分类、售价和上下架状态，批次管理只负责库存批次流转。</p>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="商品数" value={metrics.total} hint="当前查询结果的总记录数" />
        <MetricCard label="起售商品" value={metrics.active} hint="当前页可在用户端售卖的商品" />
        <MetricCard label="停售商品" value={metrics.inactive} hint="当前页已暂停售卖的商品" />
        <MetricCard label="涉及分类" value={metrics.categoryCount} hint="当前页商品覆盖的分类数量" />
      </section>

      <Card className="border-0" style={glassCard}>
        <CardHeader className="flex flex-row items-center justify-between gap-3">
          <div className="space-y-1">
            <CardTitle className="text-lg">商品分类</CardTitle>
            <p className="text-sm text-slate-500">
              {selectedCategory ? `当前查看「${selectedCategory.name}」分类商品` : "查看分类商品数量，可点击分类快速筛选商品。"}
            </p>
          </div>
          <Dialog onOpenChange={setCategoryDialogOpen} open={categoryDialogOpen}>
            <DialogTrigger asChild>
              <Button className="border-0 text-white" style={{ background: primaryGradient, boxShadow: primaryShadow }} type="button">
                <FolderPlus className="mr-2 h-4 w-4" />
                新增分类
              </Button>
            </DialogTrigger>
            <DialogContent className="border-0 sm:max-w-[420px]" style={glassDialog}>
              <DialogHeader>
                <DialogTitle>新增商品分类</DialogTitle>
              </DialogHeader>
              <form className="grid gap-4" onSubmit={categoryForm.handleSubmit(handleSubmitCategory)}>
                <div className="space-y-2">
                  <Label htmlFor="categoryName">分类名称</Label>
                  <Input className={glassInputClassName} id="categoryName" {...categoryForm.register("name")} />
                  {categoryForm.formState.errors.name ? <p className="text-sm text-red-600">{categoryForm.formState.errors.name.message}</p> : null}
                </div>
                <p className="rounded-xl bg-white/35 px-3 py-2 text-sm text-slate-600">排序由系统按当前分类顺序自动生成，新增分类会排在现有分类之后。</p>
                <Button className="border-0 text-white" disabled={categoryForm.formState.isSubmitting} style={{ background: primaryGradient, boxShadow: primaryShadow }} type="submit">
                  保存分类
                </Button>
              </form>
            </DialogContent>
          </Dialog>
        </CardHeader>
        <CardContent>
          {categoriesError ? (
            <Alert variant="destructive">
              <AlertTitle>分类加载失败</AlertTitle>
              <AlertDescription>{categoriesError}</AlertDescription>
            </Alert>
          ) : null}
          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <button
              className={`rounded-xl border p-4 text-left transition-all ${!filters.categoryId ? "border-blue-200 bg-white/60 shadow-sm" : "border-white/40 bg-white/25 hover:bg-white/40"}`}
              onClick={() => handleSelectCategory("")}
              type="button"
            >
              <p className="font-semibold text-slate-900">全部分类</p>
              <p className="mt-1 text-sm text-slate-500">共 {categories.reduce((sum, category) => sum + (category.productCount ?? 0), 0)} 个商品</p>
            </button>
            {categories.map((category) => (
              <div
                className={`rounded-xl border p-4 transition-all ${filters.categoryId === category.id ? "border-blue-200 bg-white/60 shadow-sm" : "border-white/40 bg-white/25 hover:bg-white/40"}`}
                key={category.id}
              >
                <button className="w-full text-left" onClick={() => handleSelectCategory(category.id)} type="button">
                  <p className="font-semibold text-slate-900">{category.name}</p>
                  <p className="mt-1 text-sm text-slate-500">{category.productCount ?? 0} 个商品 · 排序 {category.sort ?? 0}</p>
                </button>
                <Button
                  className="mt-3 border-red-200 bg-red-50/70 text-red-600 hover:bg-red-100"
                  onClick={() => requestDeleteCategory(category)}
                  size="sm"
                  type="button"
                  variant="outline"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  删除分类
                </Button>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {actionError ? (
        <Alert variant="destructive">
          <AlertTitle>操作失败</AlertTitle>
          <AlertDescription>{actionError}</AlertDescription>
        </Alert>
      ) : null}

      {successMessage ? (
        <Alert>
          <AlertTitle>操作成功</AlertTitle>
          <AlertDescription>{successMessage}</AlertDescription>
        </Alert>
      ) : null}

      <Card className="border-0" style={glassCard}>
        <CardContent className="flex flex-col gap-4 p-5 xl:flex-row xl:items-end xl:justify-between">
          <form className="grid flex-1 gap-4 xl:grid-cols-[minmax(0,2fr)_220px_180px_auto]" onSubmit={handleSearchSubmit}>
            <div className="space-y-2">
              <Label htmlFor="productKeyword">搜索商品</Label>
              <div className="relative">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input
                  id="productKeyword"
                  className={`${glassInputClassName} pl-9`}
                  onChange={(event) => setDraftKeyword(event.target.value)}
                  placeholder="输入商品名称"
                  value={draftKeyword}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="productCategoryFilter">分类</Label>
              <select
                id="productCategoryFilter"
                className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                onChange={(event) => setDraftCategoryId(event.target.value)}
                value={draftCategoryId}
              >
                <option value="">全部分类</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="productStatusFilter">状态</Label>
              <select
                id="productStatusFilter"
                className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                onChange={(event) => setDraftStatus(event.target.value)}
                value={draftStatus}
              >
                <option value="">全部状态</option>
                <option value="active">起售</option>
                <option value="inactive">停售</option>
              </select>
            </div>

            <div className="flex items-end gap-3">
              <Button className="flex-1 border-0 text-white" style={{ background: primaryGradient, boxShadow: primaryShadow }} type="submit">
                查询
              </Button>
              <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={handleResetFilters} type="button" variant="outline">
                重置
              </Button>
            </div>
          </form>

          <Dialog
            onOpenChange={(open) => {
              setDialogOpen(open)
              if (!open) {
                resetForm()
              }
            }}
            open={dialogOpen}
          >
            <DialogTrigger asChild>
              <Button className="border-0 text-white" data-testid="admin-create-product-open" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }} type="button">
                <Plus className="mr-2 h-4 w-4" />
                创建商品
              </Button>
            </DialogTrigger>
            <DialogContent className="border-0 sm:max-w-[560px]" style={glassDialog}>
              <DialogHeader>
                <DialogTitle>{editingProduct ? "编辑商品" : "创建商品"}</DialogTitle>
              </DialogHeader>
              <form className="grid gap-4" onSubmit={form.handleSubmit(handleSubmitProduct)}>
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="productName">商品名称</Label>
                    <Input className={glassInputClassName} data-testid="admin-product-name" id="productName" {...form.register("name")} />
                    {form.formState.errors.name ? <p className="text-sm text-red-600">{form.formState.errors.name.message}</p> : null}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="categoryId">商品分类</Label>
                    <select
                      className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                      data-testid="admin-product-category-id"
                      disabled={categoriesLoading || categories.length === 0}
                      id="categoryId"
                      {...form.register("categoryId")}
                    >
                      <option value="">请选择分类</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    {categoriesLoading ? <p className="text-sm text-slate-500">正在加载分类...</p> : null}
                    {categoriesError ? <p className="text-sm text-red-600">{categoriesError}</p> : null}
                    {form.formState.errors.categoryId ? <p className="text-sm text-red-600">{form.formState.errors.categoryId.message}</p> : null}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="price">售价</Label>
                    <Input className={glassInputClassName} data-testid="admin-product-price" id="price" step="0.01" type="number" {...form.register("price", { valueAsNumber: true })} />
                    {form.formState.errors.price ? <p className="text-sm text-red-600">{form.formState.errors.price.message}</p> : null}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="productStatus">状态</Label>
                    <select
                      id="productStatus"
                      className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                      {...form.register("status")}
                    >
                      <option value="active">起售</option>
                      <option value="inactive">停售</option>
                    </select>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">描述</Label>
                  <Textarea className={glassInputClassName} data-testid="admin-product-description" id="description" rows={4} {...form.register("description")} />
                  {form.formState.errors.description ? <p className="text-sm text-red-600">{form.formState.errors.description.message}</p> : null}
                </div>
                <Button
                  className="border-0 text-white"
                  data-testid="admin-product-submit"
                  disabled={form.formState.isSubmitting || categories.length === 0}
                  style={{ background: primaryGradient, boxShadow: primaryShadow }}
                  type="submit"
                >
                  {form.formState.isSubmitting ? (
                    <>
                      <Spinner className="mr-2" />
                      提交中
                    </>
                  ) : editingProduct ? (
                    "保存商品"
                  ) : (
                    "提交商品"
                  )}
                </Button>
              </form>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>

      <Card className="border-0" style={glassCard}>
        <CardHeader className="flex flex-row items-center justify-between gap-3">
          <div className="space-y-1">
            <CardTitle className="text-lg">商品列表</CardTitle>
            <p className="text-sm text-slate-500">总计 {total} 条记录，支持按商品、分类和状态独立查询。</p>
          </div>
          <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadProducts()} size="sm" variant="outline">
            <RefreshCw className="mr-2 h-4 w-4" />
            刷新商品
          </Button>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-12 text-sm text-slate-500">
              <Spinner className="mr-2" />
              正在加载商品数据...
            </div>
          ) : null}

          {!loading && error ? (
            <Alert variant="destructive">
              <AlertTitle>加载失败</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          ) : null}

          {!loading && !error && products.length === 0 ? (
            <Empty className="border-white/40 bg-white/20">
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <Plus className="size-5" />
                </EmptyMedia>
                <EmptyTitle>暂无商品</EmptyTitle>
                <EmptyDescription>创建商品后再到批次管理录入库存。</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button className="border-0 text-white" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                  创建首个商品
                </Button>
              </EmptyContent>
            </Empty>
          ) : null}

          {!loading && !error && products.length > 0 ? (
            <div className="space-y-4">
              <div className="overflow-x-auto rounded-xl border border-white/40 bg-white/30">
                <Table className="min-w-[900px]">
                  <TableHeader>
                    <TableRow>
                      <TableHead>商品名称</TableHead>
                      <TableHead>分类</TableHead>
                      <TableHead>售价</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="w-[190px] text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {products.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell className="font-medium text-slate-900">{product.name}</TableCell>
                        <TableCell>{product.categoryName || categories.find((item) => item.id === product.categoryId)?.name || product.categoryId}</TableCell>
                        <TableCell>{formatCurrency(product.price)}</TableCell>
                        <TableCell>
                          <span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${productStatusClasses[product.status]}`}>
                            {productStatusLabels[product.status]}
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button className="w-20 border-white/50 bg-white/30 hover:bg-white/50" onClick={() => openEditDialog(product)} size="sm" variant="outline">
                              <Pencil className="mr-2 h-4 w-4" />
                              编辑
                            </Button>
                            <Button className="w-20 border-red-200 bg-red-50/70 text-red-600 hover:bg-red-100" onClick={() => requestDeleteProduct(product)} size="sm" variant="outline">
                              <Trash2 className="mr-2 h-4 w-4" />
                              删除
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              <div className="flex items-center justify-between text-sm text-slate-600">
                <p>第 {page} / {totalPages} 页</p>
                <div className="flex gap-2">
                  <Button className="border-white/50 bg-white/30 hover:bg-white/50" disabled={page <= 1} onClick={() => setPage((current) => current - 1)} size="sm" variant="outline">
                    上一页
                  </Button>
                  <Button
                    className="border-white/50 bg-white/30 hover:bg-white/50"
                    disabled={page >= totalPages}
                    onClick={() => setPage((current) => current + 1)}
                    size="sm"
                    variant="outline"
                  >
                    下一页
                  </Button>
                </div>
              </div>
            </div>
          ) : null}
        </CardContent>
      </Card>
      <ActionConfirmDialog action={confirmAction} onOpenChange={(open) => {
        if (!open) {
          setConfirmAction(null)
        }
      }} />
    </div>
  )
}
