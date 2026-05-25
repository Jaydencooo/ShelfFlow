"use client"

import { zodResolver } from "@hookform/resolvers/zod"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useForm } from "react-hook-form"
import { Package2, Pencil, RefreshCw, Search, Trash2 } from "lucide-react"

import {
  createInventoryBatch,
  deleteInventoryBatch,
  getAdminProductCategories,
  getInventoryBatches,
  getProducts,
  isUnauthorizedError,
  logoutRequest,
  updateInventoryBatch,
  updateInventoryBatchStatus,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES, DEFAULT_PAGE_SIZE, MAX_QUERY_PAGE_SIZE } from "@/lib/constants"
import { calculateDaysLeft, formatCurrency, formatDateTime } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type {
  AdminProductCategory,
  BatchStatus,
  InventoryBatchRecord,
  ProductRecord,
} from "@/lib/types"
import type { BatchFormValues } from "@/lib/validation"
import { batchSchema } from "@/lib/validation"
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

const batchStatusLabels: Record<BatchStatus, string> = {
  draft: "草稿",
  paused: "停用",
  active: "可售",
  sold_out: "售罄",
  expired: "过期",
}

const batchStatusClasses: Record<BatchStatus, string> = {
  draft: "bg-slate-200 text-slate-700",
  paused: "bg-slate-200 text-slate-700",
  active: "bg-emerald-100 text-emerald-700",
  sold_out: "bg-amber-100 text-amber-700",
  expired: "bg-red-100 text-red-700",
}

const URGENT_EXPIRY_DAYS = 3
const DEFAULT_PRODUCT_SHELF_LIFE_DAYS = 7
const DEFAULT_BATCH_STOCK_QUANTITY = 1
const DEFAULT_BATCH_BASE_PRICE = 1
const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000
const BATCH_CODE_PREFIX = "B"
const BATCH_CODE_RANDOM_RADIX = 36
const BATCH_CODE_RANDOM_START = 2
const BATCH_CODE_RANDOM_LENGTH = 6
const numericIdPattern = /^\d+$/

function toDateTimeLocal(date: Date) {
  const offset = date.getTimezoneOffset()
  const adjusted = new Date(date.getTime() - offset * 60 * 1000)
  return adjusted.toISOString().slice(0, 16)
}

function toGatewayDateTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function formatDateSegment(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}${month}${day}`
}

function generateBatchCode(productId?: string) {
  const dateSegment = formatDateSegment(new Date())
  const productSegment = productId ? `P${productId}` : "PENDING"
  const randomSegment = Date.now()
    .toString(BATCH_CODE_RANDOM_RADIX)
    .toUpperCase()
    .slice(BATCH_CODE_RANDOM_START, BATCH_CODE_RANDOM_START + BATCH_CODE_RANDOM_LENGTH)

  return `${BATCH_CODE_PREFIX}-${productSegment}-${dateSegment}-${randomSegment}`
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function getNextBatchStatus(status: BatchStatus): BatchStatus | null {
  if (status === "active") {
    return "paused"
  }

  if (status === "paused") {
    return "active"
  }

  return null
}

function isBatchStatus(value: string | null): value is BatchStatus {
  return value === "draft" || value === "active" || value === "paused" || value === "sold_out" || value === "expired"
}

function normalizeOptionalId(value: string | null) {
  if (!value) {
    return ""
  }
  return numericIdPattern.test(value) ? value : ""
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

function BatchStatusBadge({ status }: { status: BatchStatus }) {
  return (
    <span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${batchStatusClasses[status]}`}>
      {batchStatusLabels[status]}
    </span>
  )
}

export function BatchManagementPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const [batches, setBatches] = useState<InventoryBatchRecord[]>([])
  const [products, setProducts] = useState<ProductRecord[]>([])
  const [categories, setCategories] = useState<AdminProductCategory[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(() => {
    const raw = Number(searchParams.get("page") ?? "1")
    return Number.isFinite(raw) && raw > 0 ? raw : 1
  })
  const [pageSize] = useState(DEFAULT_PAGE_SIZE)
  const [draftBatchCode, setDraftBatchCode] = useState(searchParams.get("keyword") ?? "")
  const [draftStatus, setDraftStatus] = useState(() => {
    const rawStatus = searchParams.get("batchStatus")
    return isBatchStatus(rawStatus) ? rawStatus : ""
  })
  const [draftCategoryId, setDraftCategoryId] = useState(() => normalizeOptionalId(searchParams.get("categoryId")))
  const [filters, setFilters] = useState<{ keyword?: string; batchStatus?: BatchStatus; categoryId?: string }>(() => ({
    keyword: searchParams.get("keyword") || undefined,
    categoryId: normalizeOptionalId(searchParams.get("categoryId")) || undefined,
    batchStatus: isBatchStatus(searchParams.get("batchStatus")) ? searchParams.get("batchStatus") as BatchStatus : undefined,
  }))
  const [batchesLoading, setBatchesLoading] = useState(true)
  const [productsLoading, setProductsLoading] = useState(true)
  const [categoriesLoading, setCategoriesLoading] = useState(true)
  const [batchesError, setBatchesError] = useState<string | null>(null)
  const [productsError, setProductsError] = useState<string | null>(null)
  const [categoriesError, setCategoriesError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [batchDialogOpen, setBatchDialogOpen] = useState(false)
  const [editingBatch, setEditingBatch] = useState<InventoryBatchRecord | null>(null)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)
  const [productSearchKeyword, setProductSearchKeyword] = useState("")

  const batchForm = useForm<BatchFormValues>({
    resolver: zodResolver(batchSchema),
    defaultValues: {
      productId: "",
      batchCode: generateBatchCode(),
      productionDate: toDateTimeLocal(new Date()),
      expiryDate: toDateTimeLocal(new Date(Date.now() + DEFAULT_PRODUCT_SHELF_LIFE_DAYS * MILLISECONDS_PER_DAY)),
      stockQuantity: DEFAULT_BATCH_STOCK_QUANTITY,
      lockedQuantity: 0,
      soldQuantity: 0,
      basePrice: DEFAULT_BATCH_BASE_PRICE,
      batchStatus: "active",
      pricingStatus: "active",
    },
  })
  const selectedBatchProductId = batchForm.watch("productId")

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
    if (filters.batchStatus) {
      params.set("batchStatus", filters.batchStatus)
    }
    router.replace(params.toString() ? `${pathname}?${params.toString()}` : pathname, { scroll: false })
  }, [filters, page, pathname, router])

  const loadCategories = useCallback(async () => {
    setCategoriesLoading(true)
    setCategoriesError(null)

    try {
      setCategories(await getAdminProductCategories())
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setCategoriesError(getErrorMessage(error, "加载商品分类失败"))
    } finally {
      setCategoriesLoading(false)
    }
  }, [handleUnauthorized])

  const loadProducts = useCallback(async () => {
    setProductsLoading(true)
    setProductsError(null)

    try {
      const collectedProducts: ProductRecord[] = []
      let currentPage = 1
      let total = 0

      do {
        const result = await getProducts({
          page: currentPage,
          pageSize: MAX_QUERY_PAGE_SIZE,
          status: "active",
        })

        collectedProducts.push(...result.items)
        total = result.total
        currentPage += 1
      } while (collectedProducts.length < total)

      setProducts(collectedProducts)
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setProductsError(getErrorMessage(error, "加载商品列表失败"))
    } finally {
      setProductsLoading(false)
    }
  }, [handleUnauthorized])

  const loadBatches = useCallback(async () => {
    setBatchesLoading(true)
    setBatchesError(null)

    try {
      const result = await getInventoryBatches({
        page,
        pageSize,
        keyword: filters.keyword,
        categoryId: filters.categoryId,
        batchStatus: filters.batchStatus,
      })

      setBatches(result.items)
      setTotal(result.total)
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setBatchesError(getErrorMessage(error, "加载批次列表失败"))
    } finally {
      setBatchesLoading(false)
    }
  }, [filters.batchStatus, filters.categoryId, filters.keyword, handleUnauthorized, page, pageSize])

  useEffect(() => {
    void loadCategories()
  }, [loadCategories])

  useEffect(() => {
    void loadProducts()
  }, [loadProducts])

  useEffect(() => {
    void loadBatches()
  }, [loadBatches])

  const selectedBatchProduct = useMemo(
    () => products.find((product) => product.id === selectedBatchProductId) ?? null,
    [products, selectedBatchProductId],
  )

  const selectableProducts = useMemo(() => {
    const keyword = productSearchKeyword.trim().toLowerCase()
    if (!keyword) {
      return products
    }
    return products.filter((product) =>
      product.name.toLowerCase().includes(keyword) || product.id.includes(keyword),
    )
  }, [productSearchKeyword, products])

  useEffect(() => {
    if (!selectedBatchProduct) {
      return
    }

    batchForm.setValue("basePrice", selectedBatchProduct.price, {
      shouldDirty: true,
      shouldValidate: true,
    })

    if (!editingBatch) {
      batchForm.setValue("batchCode", generateBatchCode(selectedBatchProduct.id), {
        shouldDirty: true,
        shouldValidate: true,
      })
    }
  }, [batchForm, editingBatch, selectedBatchProduct])

  const metrics = useMemo(() => {
    const urgent = batches.filter((item) => calculateDaysLeft(item.expiryDate) <= URGENT_EXPIRY_DAYS).length
    const saleable = batches.filter((item) => item.batchStatus === "active").length
    const stock = batches.reduce((sum, item) => sum + item.availableStock, 0)

    return {
      total: total.toString(),
      urgent: urgent.toString(),
      saleable: saleable.toString(),
      stock: stock.toString(),
    }
  }, [batches, total])

  function resetBatchForm() {
    setEditingBatch(null)
    setProductSearchKeyword("")
    batchForm.reset({
      productId: "",
      batchCode: generateBatchCode(),
      productionDate: toDateTimeLocal(new Date()),
      expiryDate: toDateTimeLocal(new Date(Date.now() + DEFAULT_PRODUCT_SHELF_LIFE_DAYS * MILLISECONDS_PER_DAY)),
      stockQuantity: DEFAULT_BATCH_STOCK_QUANTITY,
      lockedQuantity: 0,
      soldQuantity: 0,
      basePrice: DEFAULT_BATCH_BASE_PRICE,
      batchStatus: "active",
      pricingStatus: "active",
    })
  }

  function openCreateBatchDialog() {
    resetBatchForm()
    setBatchDialogOpen(true)
  }

  function openEditBatchDialog(batch: InventoryBatchRecord) {
    setEditingBatch(batch)
    setProductSearchKeyword(batch.productName)
    batchForm.reset({
      productId: batch.productId,
      batchCode: batch.batchCode,
      productionDate: toDateTimeLocal(new Date(batch.productionDate)),
      expiryDate: toDateTimeLocal(new Date(batch.expiryDate)),
      stockQuantity: batch.availableStock + batch.lockedStock + batch.soldStock,
      lockedQuantity: batch.lockedStock,
      soldQuantity: batch.soldStock,
      basePrice: batch.basePrice,
      batchStatus: batch.batchStatus,
      pricingStatus: batch.pricingStatus,
    })
    setBatchDialogOpen(true)
  }

  async function handleSubmitBatch(values: BatchFormValues) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      const payload = {
        ...values,
        productionDate: toGatewayDateTime(values.productionDate),
        expiryDate: toGatewayDateTime(values.expiryDate),
      }

      if (editingBatch) {
        await updateInventoryBatch(editingBatch.id, payload)
        setSuccessMessage(`批次 ${values.batchCode} 已更新`)
      } else {
        await createInventoryBatch(payload)
        setSuccessMessage("批次已创建")
      }

      setBatchDialogOpen(false)
      resetBatchForm()
      await loadBatches()
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setActionError(getErrorMessage(error, editingBatch ? "批次更新失败" : "批次创建失败"))
    }
  }

  async function handleUpdateBatchStatus(batch: InventoryBatchRecord) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      const nextStatus = getNextBatchStatus(batch.batchStatus)

      if (!nextStatus) {
        setActionError(`批次 ${batch.batchCode} 当前状态不可在页面直接流转`)
        return
      }

      await updateInventoryBatchStatus(batch.id, nextStatus)
      setSuccessMessage(`批次 ${batch.batchCode} 已更新为${batchStatusLabels[nextStatus]}`)
      await loadBatches()
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setActionError(getErrorMessage(error, "批次状态更新失败"))
    }
  }

  function requestBatchStatusUpdate(batch: InventoryBatchRecord) {
    const nextStatus = getNextBatchStatus(batch.batchStatus)
    if (!nextStatus) {
      setActionError(`批次 ${batch.batchCode} 当前状态不可在页面直接流转`)
      return
    }

    setConfirmAction({
      title: "确认流转批次状态",
      description: `将批次 ${batch.batchCode} 从「${batchStatusLabels[batch.batchStatus]}」流转为「${batchStatusLabels[nextStatus]}」。该操作会影响用户端可售状态。`,
      confirmLabel: batch.batchStatus === "active" ? "停用批次" : "启用批次",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleUpdateBatchStatus(batch)
      },
    })
  }

  async function handleDeleteBatch(batch: InventoryBatchRecord) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await deleteInventoryBatch(batch.id)
      setSuccessMessage(`批次 ${batch.batchCode} 已删除`)
      await loadBatches()
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }

      setActionError(getErrorMessage(error, "批次删除失败"))
    }
  }

  function requestBatchDelete(batch: InventoryBatchRecord) {
    setConfirmAction({
      title: "确认删除批次",
      description: `删除批次 ${batch.batchCode}。如果该批次已有锁定库存或已售库存，后端会拒绝删除以保护订单和库存历史。`,
      confirmLabel: "删除",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleDeleteBatch(batch)
      },
    })
  }

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(1)
    setFilters({
      keyword: draftBatchCode || undefined,
      categoryId: normalizeOptionalId(draftCategoryId) || undefined,
      batchStatus: isBatchStatus(draftStatus) ? draftStatus : undefined,
    })
  }

  function handleResetFilters() {
    setDraftBatchCode("")
    setDraftStatus("")
    setDraftCategoryId("")
    setPage(1)
    setFilters({})
  }

  const totalPages = Math.max(1, Math.ceil(total / pageSize))

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">批次管理</h1>
        <p className="text-sm text-slate-600">
          管理库存批次、临期状态和可售库存；商品资料请在商品管理中维护。
        </p>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="批次数" value={metrics.total} hint="当前查询结果的总记录数" />
        <MetricCard label="3 天内到期" value={metrics.urgent} hint="需要优先关注的临期批次" />
        <MetricCard label="可售批次" value={metrics.saleable} hint="状态仍可流转的批次" />
        <MetricCard label="可售库存" value={metrics.stock} hint="当前页批次的可售库存合计" />
      </section>

      <Card className="border-0" style={glassCard}>
        <CardContent className="flex flex-col gap-4 p-5">
          <form className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_200px_240px_auto]" onSubmit={handleSearchSubmit}>
            <div className="space-y-2">
              <Label htmlFor="batchCode">搜索批次</Label>
              <div className="relative">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input
                  id="batchCode"
                  className={`${glassInputClassName} pl-9`}
                  onChange={(event) => setDraftBatchCode(event.target.value)}
                  placeholder="输入批次号或商品名"
                  value={draftBatchCode}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="statusFilter">状态</Label>
              <select
                id="statusFilter"
                className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                onChange={(event) => setDraftStatus(event.target.value)}
                value={draftStatus}
              >
                <option value="">全部状态</option>
                {(["draft", "active", "paused", "sold_out", "expired"] as BatchStatus[]).map((status) => (
                  <option key={status} value={status}>
                    {batchStatusLabels[status]}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="categoryFilter">分类</Label>
              <select
                id="categoryFilter"
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
              {categoriesLoading ? <p className="text-sm text-slate-500">正在加载分类...</p> : null}
              {categoriesError ? <p className="text-sm text-red-600">{categoriesError}</p> : null}
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

          <div className="flex flex-wrap gap-3">
            <Dialog
              onOpenChange={(open) => {
                setBatchDialogOpen(open)
                if (!open) {
                  resetBatchForm()
                }
              }}
              open={batchDialogOpen}
            >
              <DialogTrigger asChild>
                <Button className="border-0 text-white" data-testid="admin-create-batch-open" onClick={openCreateBatchDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }} type="button">
                  <Package2 className="mr-2 h-4 w-4" />
                  创建批次
                </Button>
              </DialogTrigger>
              <DialogContent className="border-0 sm:max-w-[560px]" style={glassDialog}>
                <DialogHeader>
                  <DialogTitle>{editingBatch ? "编辑批次" : "创建批次"}</DialogTitle>
                </DialogHeader>
                <form className="grid gap-4" onSubmit={batchForm.handleSubmit(handleSubmitBatch)}>
                  <div className="space-y-2">
                    <Label htmlFor="productId">商品</Label>
                    <div className="relative">
                      <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                      <Input
                        className={`${glassInputClassName} pl-9`}
                        onChange={(event) => setProductSearchKeyword(event.target.value)}
                        placeholder="先搜索商品名称或 ID"
                        value={productSearchKeyword}
                      />
                    </div>
                    <select
                      id="productId"
                      data-testid="admin-batch-product-id"
                      className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                      {...batchForm.register("productId")}
                    >
                      <option value="">请选择商品</option>
                      {selectableProducts.map((product) => (
                        <option key={product.id} value={product.id}>
                          {product.name}
                        </option>
                      ))}
                    </select>
                    {productsLoading ? <p className="text-sm text-slate-500">正在加载商品列表...</p> : null}
                    {productsError ? <p className="text-sm text-red-600">{productsError}</p> : null}
                    {batchForm.formState.errors.productId ? (
                      <p className="text-sm text-red-600">{batchForm.formState.errors.productId.message}</p>
                    ) : null}
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor="batchCodeInput">批次号</Label>
                      <div className="flex gap-2">
                        <Input className={glassInputClassName} data-testid="admin-batch-code" id="batchCodeInput" {...batchForm.register("batchCode")} />
                        <Button
                          className="border-white/50 bg-white/30 hover:bg-white/50"
                          onClick={() => {
                            batchForm.setValue("batchCode", generateBatchCode(selectedBatchProductId), {
                              shouldDirty: true,
                              shouldValidate: true,
                            })
                          }}
                          type="button"
                          variant="outline"
                        >
                          生成
                        </Button>
                      </div>
                      {batchForm.formState.errors.batchCode ? (
                        <p className="text-sm text-red-600">{batchForm.formState.errors.batchCode.message}</p>
                      ) : null}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="stockQuantity">入库数量</Label>
                      <Input className={glassInputClassName} data-testid="admin-batch-stock-quantity" id="stockQuantity" type="number" {...batchForm.register("stockQuantity", { valueAsNumber: true })} />
                      {batchForm.formState.errors.stockQuantity ? (
                        <p className="text-sm text-red-600">{batchForm.formState.errors.stockQuantity.message}</p>
                      ) : null}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="basePrice">基准价</Label>
                      <Input
                        id="basePrice"
                        className={glassInputClassName}
                        readOnly
                        step="0.01"
                        type="number"
                        {...batchForm.register("basePrice", { valueAsNumber: true })}
                      />
                      <p className="text-xs text-slate-500">基准价跟随商品售价，需修改请先编辑商品。</p>
                      {batchForm.formState.errors.basePrice ? (
                        <p className="text-sm text-red-600">{batchForm.formState.errors.basePrice.message}</p>
                      ) : null}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="productionDate">生产时间</Label>
                      <Input className={glassInputClassName} data-testid="admin-batch-production-date" id="productionDate" type="datetime-local" {...batchForm.register("productionDate")} />
                      {batchForm.formState.errors.productionDate ? (
                        <p className="text-sm text-red-600">{batchForm.formState.errors.productionDate.message}</p>
                      ) : null}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="expiryDate">过期时间</Label>
                      <Input className={glassInputClassName} data-testid="admin-batch-expiry-date" id="expiryDate" type="datetime-local" {...batchForm.register("expiryDate")} />
                      {batchForm.formState.errors.expiryDate ? (
                        <p className="text-sm text-red-600">{batchForm.formState.errors.expiryDate.message}</p>
                      ) : null}
                    </div>
                  </div>

                  <Button className="border-0 text-white" data-testid="admin-batch-submit" disabled={batchForm.formState.isSubmitting || products.length === 0} style={{ background: primaryGradient, boxShadow: primaryShadow }} type="submit">
                    {batchForm.formState.isSubmitting ? (
                      <>
                        <Spinner className="mr-2" />
                        提交中
                      </>
                    ) : editingBatch ? (
                      "保存批次"
                    ) : (
                      "提交批次"
                    )}
                  </Button>
                </form>
              </DialogContent>
            </Dialog>
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
        <CardHeader className="flex flex-row items-center justify-between gap-3">
          <div className="space-y-1">
            <CardTitle className="text-lg">批次列表</CardTitle>
            <p className="text-sm text-slate-500">总计 {total} 条记录，支持分类筛选、状态流转和直接编辑。</p>
          </div>
          <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadBatches()} size="sm" variant="outline">
            <RefreshCw className="mr-2 h-4 w-4" />
            重新加载
          </Button>
        </CardHeader>
        <CardContent>
          {batchesLoading ? (
            <div className="flex items-center justify-center py-12 text-sm text-slate-500">
              <Spinner className="mr-2" />
              正在加载批次数据...
            </div>
          ) : null}

          {!batchesLoading && batchesError ? (
            <Alert variant="destructive">
              <AlertTitle>加载失败</AlertTitle>
              <AlertDescription>{batchesError}</AlertDescription>
            </Alert>
          ) : null}

          {!batchesLoading && !batchesError && batches.length === 0 ? (
            <Empty className="border-white/40 bg-white/20">
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <Package2 className="size-5" />
                </EmptyMedia>
                <EmptyTitle>暂无批次</EmptyTitle>
                <EmptyDescription>当前筛选条件下没有结果，先创建商品并录入批次。</EmptyDescription>
              </EmptyHeader>
              <EmptyContent>
                <Button className="border-0 text-white" onClick={openCreateBatchDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}>创建首个批次</Button>
              </EmptyContent>
            </Empty>
          ) : null}

          {!batchesLoading && !batchesError && batches.length > 0 ? (
            <div className="space-y-4">
              <div className="overflow-x-auto rounded-xl border border-white/40 bg-white/30">
                <Table className="min-w-[1320px]">
                  <TableHeader>
                    <TableRow>
                      <TableHead>批次号</TableHead>
                      <TableHead>商品</TableHead>
                      <TableHead>分类</TableHead>
                      <TableHead>生产时间</TableHead>
                      <TableHead>过期时间</TableHead>
                      <TableHead>剩余天数</TableHead>
                      <TableHead>库存</TableHead>
                      <TableHead>售价</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead className="w-[260px] text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {batches.map((batch) => (
                      <TableRow key={batch.id}>
                        <TableCell className="font-medium text-slate-900">{batch.batchCode}</TableCell>
                        <TableCell>
                          <div className="space-y-1">
                            <p className="font-medium text-slate-900">{batch.productName}</p>
                            <p className="text-xs text-slate-500">商品 ID: {batch.productId}</p>
                          </div>
                        </TableCell>
                        <TableCell>{categories.find((item) => item.id === batch.categoryId)?.name || batch.categoryId || "-"}</TableCell>
                        <TableCell>{formatDateTime(batch.productionDate)}</TableCell>
                        <TableCell>{formatDateTime(batch.expiryDate)}</TableCell>
                        <TableCell>{batch.shelfLifeDays} 天</TableCell>
                        <TableCell>
                          <div className="space-y-1 text-sm">
                            <p>可售 {batch.availableStock}</p>
                            <p className="text-slate-500">
                              锁定 {batch.lockedStock} / 已售 {batch.soldStock} / 损耗 {batch.wasteStock}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="space-y-1 text-sm">
                            <p>{formatCurrency(batch.currentPrice ?? batch.basePrice)}</p>
                            <p className="text-slate-500">基准 {formatCurrency(batch.basePrice)}</p>
                          </div>
                        </TableCell>
                        <TableCell>
                          <BatchStatusBadge status={batch.batchStatus} />
                        </TableCell>
                        <TableCell className="text-right align-top">
                          <div className="flex justify-end gap-2">
                            <Button className="w-20 border-white/50 bg-white/30 hover:bg-white/50" onClick={() => openEditBatchDialog(batch)} size="sm" variant="outline">
                              <Pencil className="mr-2 h-4 w-4" />
                              编辑
                            </Button>
                            <Button
                              disabled={!getNextBatchStatus(batch.batchStatus)}
                              className="w-20 border-white/50 bg-white/30 hover:bg-white/50"
                              onClick={() => requestBatchStatusUpdate(batch)}
                              size="sm"
                              variant="outline"
                            >
                              {getNextBatchStatus(batch.batchStatus)
                                ? batch.batchStatus === "active"
                                  ? "停用"
                                  : "启用"
                                : "不可流转"}
                            </Button>
                            <Button
                              className="w-20 border-red-200 bg-red-50/70 text-red-600 hover:bg-red-100"
                              onClick={() => requestBatchDelete(batch)}
                              size="sm"
                              variant="outline"
                            >
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
                <p>
                  第 {page} / {totalPages} 页
                </p>
                <div className="flex gap-2">
                  <Button className="border-white/50 bg-white/30 hover:bg-white/50" disabled={page <= 1} onClick={() => setPage((current) => current - 1)} size="sm" variant="outline">
                    上一页
                  </Button>
                  <Button
                    disabled={page >= totalPages}
                    className="border-white/50 bg-white/30 hover:bg-white/50"
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
