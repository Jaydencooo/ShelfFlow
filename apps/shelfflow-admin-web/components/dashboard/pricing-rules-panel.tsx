"use client"

import { zodResolver } from "@hookform/resolvers/zod"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useForm } from "react-hook-form"
import { Check, Pencil, Plus, RefreshCw, Search, Sparkles, Trash2, X } from "lucide-react"

import {
  acceptAdminPricingSuggestion,
  createAdminPricingRule,
  deleteAdminPricingRule,
  getAdminPricingRules,
  getAdminPricingSuggestions,
  isUnauthorizedError,
  logoutRequest,
  updateAdminPricingRule,
  updateAdminPricingRuleStatus,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants"
import { formatCurrency, formatDateTime } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type { AdminPricingRule, AdminPricingSuggestion, PricingRuleStatus } from "@/lib/types"
import type { PricingRuleFormValues } from "@/lib/validation"
import { pricingRuleSchema } from "@/lib/validation"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

const pricingRuleStatusLabels: Record<PricingRuleStatus, string> = {
  enabled: "启用",
  disabled: "停用",
}

const pricingRuleStatusClasses: Record<PricingRuleStatus, string> = {
  enabled: "bg-emerald-100 text-emerald-700",
  disabled: "bg-red-100 text-red-700",
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function formatRate(rate: number) {
  return `${Math.round(rate * 100)}%`
}

function isPricingRuleStatus(value: string | null): value is PricingRuleStatus {
  return value === "enabled" || value === "disabled"
}

function MetricCard(props: { label: string; value: string; hint: string }) {
  return (
    <Card className="border-0" style={glassCard}>
      <CardContent className="space-y-1 p-5">
        <p className="text-sm text-slate-500">{props.label}</p>
        <p className="text-2xl font-semibold text-slate-900">{props.value}</p>
        <p className="text-sm text-slate-600">{props.hint}</p>
      </CardContent>
    </Card>
  )
}

export function PricingRulesPanel() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const [rules, setRules] = useState<AdminPricingRule[]>([])
  const [suggestions, setSuggestions] = useState<AdminPricingSuggestion[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(() => {
    const raw = Number(searchParams.get("page") ?? "1")
    return Number.isFinite(raw) && raw > 0 ? raw : 1
  })
  const [keyword, setKeyword] = useState(searchParams.get("keyword") ?? "")
  const [status, setStatus] = useState(() => {
    const queryStatus = searchParams.get("status")
    return isPricingRuleStatus(queryStatus) ? queryStatus : ""
  })
  const [loading, setLoading] = useState(true)
  const [suggestionsLoading, setSuggestionsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [suggestionsError, setSuggestionsError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingRule, setEditingRule] = useState<AdminPricingRule | null>(null)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)

  const form = useForm<PricingRuleFormValues>({
    resolver: zodResolver(pricingRuleSchema),
    defaultValues: {
      name: "",
      minDaysToExpire: 0,
      maxDaysToExpire: 7,
      discountRate: 0.8,
      priority: 10,
      status: "enabled",
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

    if (keyword.trim()) {
      params.set("keyword", keyword.trim())
    }

    if (isPricingRuleStatus(status)) {
      params.set("status", status)
    }

    router.replace(params.toString() ? `${pathname}?${params.toString()}` : pathname, { scroll: false })
  }, [keyword, page, pathname, router, status])

  const loadRules = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await getAdminPricingRules({
        page,
        pageSize: DEFAULT_PAGE_SIZE,
        keyword: keyword.trim() || undefined,
        status: isPricingRuleStatus(status) ? status : undefined,
        sortBy: "priority",
        sortOrder: "desc",
      })
      setRules(result.items)
      setTotal(result.total)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(loadError, "加载定价规则失败"))
    } finally {
      setLoading(false)
    }
  }, [handleUnauthorized, keyword, page, status])

  const loadSuggestions = useCallback(async () => {
    setSuggestionsLoading(true)
    setSuggestionsError(null)

    try {
      setSuggestions(await getAdminPricingSuggestions())
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setSuggestionsError(getErrorMessage(loadError, "加载 AI 定价建议失败"))
    } finally {
      setSuggestionsLoading(false)
    }
  }, [handleUnauthorized])

  useEffect(() => {
    void loadRules()
  }, [loadRules])

  useEffect(() => {
    void loadSuggestions()
  }, [loadSuggestions])

  const metrics = useMemo(() => {
    const enabled = rules.filter((rule) => rule.status === "enabled").length
    const averageRate = rules.length === 0
      ? "0%"
      : formatRate(rules.reduce((sum, rule) => sum + Number(rule.discountRate), 0) / rules.length)

    return {
      total: total.toString(),
      enabled: enabled.toString(),
      suggestions: suggestions.length.toString(),
      averageRate,
    }
  }, [rules, suggestions.length, total])

  function resetForm() {
    setEditingRule(null)
    form.reset({
      name: "",
      minDaysToExpire: 0,
      maxDaysToExpire: 7,
      discountRate: 0.8,
      priority: 10,
      status: "enabled",
    })
  }

  function openCreateDialog() {
    resetForm()
    setDialogOpen(true)
  }

  function openEditDialog(rule: AdminPricingRule) {
    setEditingRule(rule)
    form.reset({
      name: rule.name,
      minDaysToExpire: rule.minDaysToExpire,
      maxDaysToExpire: rule.maxDaysToExpire,
      discountRate: Number(rule.discountRate),
      priority: rule.priority,
      status: rule.status,
    })
    setDialogOpen(true)
  }

  async function handleSubmitRule(values: PricingRuleFormValues) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      if (editingRule) {
        await updateAdminPricingRule(editingRule.id, values)
        setSuccessMessage(`定价规则 ${values.name} 已更新`)
      } else {
        await createAdminPricingRule(values)
        setSuccessMessage("定价规则已创建")
      }
      setDialogOpen(false)
      resetForm()
      await loadRules()
    } catch (submitError) {
      if (isUnauthorizedError(submitError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(submitError, editingRule ? "定价规则更新失败" : "定价规则创建失败"))
    }
  }

  async function handleToggleStatus(rule: AdminPricingRule) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      const nextStatus: PricingRuleStatus = rule.status === "enabled" ? "disabled" : "enabled"
      await updateAdminPricingRuleStatus(rule.id, nextStatus)
      setSuccessMessage(`定价规则 ${rule.name} 已${pricingRuleStatusLabels[nextStatus]}`)
      await loadRules()
    } catch (toggleError) {
      if (isUnauthorizedError(toggleError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(toggleError, "定价规则状态更新失败"))
    }
  }

  function requestToggleStatus(rule: AdminPricingRule) {
    const nextStatus: PricingRuleStatus = rule.status === "enabled" ? "disabled" : "enabled"
    setConfirmAction({
      title: "确认调整定价规则状态",
      description: `将定价规则「${rule.name}」${pricingRuleStatusLabels[nextStatus]}。该操作会影响用户端商品动态价格。`,
      confirmLabel: pricingRuleStatusLabels[nextStatus],
      onConfirm: async () => {
        setConfirmAction(null)
        await handleToggleStatus(rule)
      },
    })
  }

  async function handleDeleteRule(rule: AdminPricingRule) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await deleteAdminPricingRule(rule.id)
      setSuccessMessage(`定价规则 ${rule.name} 已删除`)
      await loadRules()
      await loadSuggestions()
    } catch (deleteError) {
      if (isUnauthorizedError(deleteError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(deleteError, "定价规则删除失败"))
    }
  }

  function requestDeleteRule(rule: AdminPricingRule) {
    setConfirmAction({
      title: "确认删除定价规则",
      description: `删除定价规则「${rule.name}」。删除后该规则不再参与用户端动态价格计算。`,
      confirmLabel: "删除",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleDeleteRule(rule)
      },
    })
  }

  async function handleAcceptSuggestion(suggestion: AdminPricingSuggestion) {
    setActionError(null)
    setSuccessMessage(null)

    try {
      await acceptAdminPricingSuggestion(suggestion.batchId)
      setSuggestions((current) => current.filter((item) => item.batchId !== suggestion.batchId))
      setSuccessMessage(`已采纳批次 ${suggestion.batchCode} 的 AI 定价建议`)
      await Promise.all([loadRules(), loadSuggestions()])
    } catch (acceptError) {
      if (isUnauthorizedError(acceptError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(acceptError, "AI 定价建议采纳失败"))
    }
  }

  function requestAcceptSuggestion(suggestion: AdminPricingSuggestion) {
    setConfirmAction({
      title: "确认采纳 AI 定价建议",
      description: `采纳后会为批次 ${suggestion.batchCode} 创建定价规则，建议价为 ${formatCurrency(Number(suggestion.suggestedPrice))}，建议折扣为 ${formatRate(Number(suggestion.suggestedDiscountRate))}。`,
      confirmLabel: "采纳建议",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleAcceptSuggestion(suggestion)
      },
    })
  }

  const totalPages = Math.max(1, Math.ceil(total / DEFAULT_PAGE_SIZE))

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">定价规则</h1>
        <p className="text-sm text-slate-600">管理临期折扣规则和 AI 定价建议，规则会影响用户端商品动态价。</p>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="规则总数" value={metrics.total} hint="当前筛选条件下的规则数量" />
        <MetricCard label="启用规则" value={metrics.enabled} hint="参与动态价格计算" />
        <MetricCard label="AI 建议" value={metrics.suggestions} hint="基于可售临期批次生成" />
        <MetricCard label="平均折扣" value={metrics.averageRate} hint="当前页规则折扣均值" />
      </section>

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

      <Tabs defaultValue="rules" className="w-full">
        <div className="inline-flex rounded-xl p-1" style={glassCard}>
          <TabsList className="bg-transparent">
            <TabsTrigger className="data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="rules">
              规则列表
            </TabsTrigger>
            <TabsTrigger className="gap-1 data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="suggestions">
              <Sparkles className="h-4 w-4" />
              AI 建议审核
              <Badge className="ml-1 border-0 bg-purple-100/60 text-purple-700">{suggestions.length}</Badge>
            </TabsTrigger>
          </TabsList>
        </div>

        <TabsContent className="mt-4 space-y-4" value="rules">
          <Card className="border-0" style={glassCard}>
            <CardContent className="flex flex-col gap-4 p-5 xl:flex-row xl:items-end xl:justify-between">
              <div className="grid flex-1 gap-4 md:grid-cols-[minmax(0,1fr)_180px]">
                <div className="space-y-2">
                  <Label htmlFor="pricingKeyword">搜索规则</Label>
                  <div className="relative">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
                    <Input
                      className={`${glassInputClassName} pl-10`}
                      id="pricingKeyword"
                      onChange={(event) => {
                        setPage(1)
                        setKeyword(event.target.value)
                      }}
                      placeholder="搜索规则名称..."
                      value={keyword}
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="pricingStatus">状态</Label>
                  <select
                    className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
                    id="pricingStatus"
                    onChange={(event) => {
                      setPage(1)
                      setStatus(event.target.value)
                    }}
                    value={status}
                  >
                    <option value="">全部状态</option>
                    <option value="enabled">启用</option>
                    <option value="disabled">停用</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-3">
                <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadRules()} type="button" variant="outline">
                  <RefreshCw className="mr-2 h-4 w-4" />
                  刷新
                </Button>
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
                    <Button className="border-0 text-white" data-testid="admin-create-pricing-rule-open" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                      <Plus className="mr-2 h-4 w-4" />
                      创建规则
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="border-0 sm:max-w-[560px]" style={glassDialog}>
                    <DialogHeader>
                      <DialogTitle>{editingRule ? "编辑定价规则" : "创建定价规则"}</DialogTitle>
                    </DialogHeader>
                    <form className="grid gap-4" onSubmit={form.handleSubmit(handleSubmitRule)}>
                      <div className="space-y-2">
                        <Label htmlFor="pricingName">规则名称</Label>
                        <Input className={glassInputClassName} data-testid="admin-pricing-rule-name" id="pricingName" {...form.register("name")} />
                        {form.formState.errors.name ? <p className="text-sm text-red-600">{form.formState.errors.name.message}</p> : null}
                      </div>
                      <div className="grid gap-4 md:grid-cols-2">
                        <div className="space-y-2">
                          <Label htmlFor="minDaysToExpire">最小剩余天数</Label>
                          <Input className={glassInputClassName} data-testid="admin-pricing-rule-min-days" id="minDaysToExpire" type="number" {...form.register("minDaysToExpire", { valueAsNumber: true })} />
                          {form.formState.errors.minDaysToExpire ? <p className="text-sm text-red-600">{form.formState.errors.minDaysToExpire.message}</p> : null}
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="maxDaysToExpire">最大剩余天数</Label>
                          <Input className={glassInputClassName} data-testid="admin-pricing-rule-max-days" id="maxDaysToExpire" type="number" {...form.register("maxDaysToExpire", { valueAsNumber: true })} />
                          {form.formState.errors.maxDaysToExpire ? <p className="text-sm text-red-600">{form.formState.errors.maxDaysToExpire.message}</p> : null}
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="discountRate">折扣率</Label>
                          <Input
                            className={glassInputClassName}
                            data-testid="admin-pricing-rule-discount-rate"
                            id="discountRate"
                            step="0.01"
                            type="number"
                            {...form.register("discountRate", {
                              setValueAs: (value) => {
                                const numberValue = Number(value)
                                return numberValue > 1 ? numberValue / 100 : numberValue
                              },
                            })}
                          />
                          <p className="text-xs text-slate-500">可输入 0.8 或 80，系统会统一保存为 8 折。</p>
                          {form.formState.errors.discountRate ? <p className="text-sm text-red-600">{form.formState.errors.discountRate.message}</p> : null}
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="priority">优先级</Label>
                          <Input className={glassInputClassName} data-testid="admin-pricing-rule-priority" id="priority" type="number" {...form.register("priority", { valueAsNumber: true })} />
                          {form.formState.errors.priority ? <p className="text-sm text-red-600">{form.formState.errors.priority.message}</p> : null}
                        </div>
                        <div className="space-y-2 md:col-span-2">
                          <Label htmlFor="ruleStatus">状态</Label>
                          <select className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`} id="ruleStatus" {...form.register("status")}>
                            <option value="enabled">启用</option>
                            <option value="disabled">停用</option>
                          </select>
                        </div>
                      </div>
                      <Button className="border-0 text-white" data-testid="admin-pricing-rule-submit" disabled={form.formState.isSubmitting} style={{ background: primaryGradient, boxShadow: primaryShadow }} type="submit">
                        {form.formState.isSubmitting ? (
                          <>
                            <Spinner className="mr-2" />
                            提交中
                          </>
                        ) : editingRule ? "保存规则" : "提交规则"}
                      </Button>
                    </form>
                  </DialogContent>
                </Dialog>
              </div>
            </CardContent>
          </Card>

          <Card className="border-0" style={glassCard}>
            <CardHeader>
              <CardTitle className="text-lg text-slate-800">规则列表</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex items-center justify-center py-12 text-sm text-slate-500">
                  <Spinner className="mr-2" />
                  正在加载定价规则...
                </div>
              ) : null}

              {!loading && error ? (
                <Alert variant="destructive">
                  <AlertTitle>加载失败</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              ) : null}

              {!loading && !error && rules.length === 0 ? (
                <Empty className="border-white/40 bg-white/20">
                  <EmptyHeader>
                    <EmptyMedia variant="icon">
                      <Sparkles className="size-5" />
                    </EmptyMedia>
                    <EmptyTitle>暂无定价规则</EmptyTitle>
                    <EmptyDescription>创建规则后，用户端商品动态价会按规则计算。</EmptyDescription>
                  </EmptyHeader>
                  <EmptyContent>
                    <Button className="border-0 text-white" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}>创建首条规则</Button>
                  </EmptyContent>
                </Empty>
              ) : null}

              {!loading && !error && rules.length > 0 ? (
                <div className="space-y-4">
                  <div className="overflow-hidden rounded-xl border border-white/40 bg-white/30">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>规则名称</TableHead>
                          <TableHead>触发条件</TableHead>
                          <TableHead>折扣</TableHead>
                          <TableHead>优先级</TableHead>
                          <TableHead>状态</TableHead>
                          <TableHead>更新时间</TableHead>
                          <TableHead>操作</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {rules.map((rule) => (
                          <TableRow key={rule.id}>
                            <TableCell className="font-medium text-slate-900">{rule.name}</TableCell>
                            <TableCell>剩余 {rule.minDaysToExpire}-{rule.maxDaysToExpire} 天</TableCell>
                            <TableCell>{formatRate(Number(rule.discountRate))}</TableCell>
                            <TableCell>{rule.priority}</TableCell>
                            <TableCell>
                              <Badge className={`border-0 ${pricingRuleStatusClasses[rule.status]}`}>{pricingRuleStatusLabels[rule.status]}</Badge>
                            </TableCell>
                            <TableCell>{rule.updateTime ? formatDateTime(rule.updateTime) : "-"}</TableCell>
                            <TableCell>
                              <div className="flex gap-2">
                                <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => openEditDialog(rule)} size="sm" variant="outline">
                                  <Pencil className="mr-2 h-4 w-4" />
                                  编辑
                                </Button>
                                <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => requestToggleStatus(rule)} size="sm" variant="outline">
                                  {rule.status === "enabled" ? <X className="mr-2 h-4 w-4" /> : <Check className="mr-2 h-4 w-4" />}
                                  {rule.status === "enabled" ? "停用" : "启用"}
                                </Button>
                                <Button className="border-red-200 bg-red-50/70 text-red-600 hover:bg-red-100" onClick={() => requestDeleteRule(rule)} size="sm" variant="outline">
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
                      <Button className="border-white/50 bg-white/30 hover:bg-white/50" disabled={page >= totalPages} onClick={() => setPage((current) => current + 1)} size="sm" variant="outline">
                        下一页
                      </Button>
                    </div>
                  </div>
                </div>
              ) : null}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent className="mt-4 space-y-4" value="suggestions">
          <Card className="border-0" style={glassCard}>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-lg text-slate-800">AI 建议审核</CardTitle>
              <Button className="border-white/50 bg-white/30 hover:bg-white/50" onClick={() => void loadSuggestions()} size="sm" variant="outline">
                <RefreshCw className="mr-2 h-4 w-4" />
                刷新建议
              </Button>
            </CardHeader>
            <CardContent>
              {suggestionsLoading ? (
                <div className="flex items-center justify-center py-12 text-sm text-slate-500">
                  <Spinner className="mr-2" />
                  正在分析临期批次...
                </div>
              ) : null}

              {!suggestionsLoading && suggestionsError ? (
                <Alert variant="destructive">
                  <AlertTitle>加载失败</AlertTitle>
                  <AlertDescription>{suggestionsError}</AlertDescription>
                </Alert>
              ) : null}

              {!suggestionsLoading && !suggestionsError && suggestions.length === 0 ? (
                <Empty className="border-white/40 bg-white/20">
                  <EmptyHeader>
                    <EmptyMedia variant="icon">
                      <Sparkles className="size-5" />
                    </EmptyMedia>
                    <EmptyTitle>暂无 AI 定价建议</EmptyTitle>
                    <EmptyDescription>当前没有符合条件的可售临期批次。</EmptyDescription>
                  </EmptyHeader>
                </Empty>
              ) : null}

              {!suggestionsLoading && !suggestionsError && suggestions.length > 0 ? (
                <div className="grid gap-4 xl:grid-cols-2">
                  {suggestions.map((suggestion) => (
                    <div className="rounded-xl border border-white/40 bg-white/30 p-4" key={suggestion.id}>
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <p className="font-semibold text-slate-900">{suggestion.productName}</p>
                          <p className="mt-1 text-sm text-slate-500">批次 {suggestion.batchCode}，剩余 {suggestion.daysToExpire} 天</p>
                        </div>
                        <Badge className="border-0 bg-purple-100/70 text-purple-700">置信度 {suggestion.confidence}</Badge>
                      </div>
                      <p className="mt-3 text-sm leading-6 text-slate-600">{suggestion.reason}</p>
                      <div className="mt-4 grid gap-3 text-sm md:grid-cols-3">
                        <div className="rounded-lg bg-white/40 p-3">
                          <p className="text-slate-500">当前价</p>
                          <p className="font-medium text-slate-900">{formatCurrency(Number(suggestion.currentPrice))}</p>
                        </div>
                        <div className="rounded-lg bg-white/40 p-3">
                          <p className="text-slate-500">建议价</p>
                          <p className="font-medium text-slate-900">{formatCurrency(Number(suggestion.suggestedPrice))}</p>
                        </div>
                        <div className="rounded-lg bg-white/40 p-3">
                          <p className="text-slate-500">建议折扣</p>
                          <p className="font-medium text-slate-900">{formatRate(Number(suggestion.suggestedDiscountRate))}</p>
                        </div>
                      </div>
                      <div className="mt-4 flex justify-end">
                        <Button className="border-0 text-white" onClick={() => requestAcceptSuggestion(suggestion)} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                          <Check className="mr-2 h-4 w-4" />
                          采纳建议
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : null}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      <ActionConfirmDialog action={confirmAction} onOpenChange={(open) => {
        if (!open) {
          setConfirmAction(null)
        }
      }} />
    </div>
  )
}
