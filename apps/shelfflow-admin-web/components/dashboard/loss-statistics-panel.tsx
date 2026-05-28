"use client"

import { useRouter } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { AlertTriangle, ArrowDown, CheckCircle, DollarSign, Lightbulb, Package, ReceiptText, RefreshCw, ShoppingBag, TrendingDown } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { acceptAdminPricingSuggestion, getAdminLossStatsOverview, isUnauthorizedError, logoutRequest, updateInventoryBatchStatus } from "@/lib/client/api"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { formatCurrency } from "@/lib/formatters"
import { glassCard, primaryGradient, statusGradients } from "@/lib/glass-styles"
import type { AdminLossStatsOverview, AdminLossStatsSuggestion } from "@/lib/types"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"

const priorityStyles: Record<AdminLossStatsSuggestion["priority"], { bg: string; text: string }> = {
  高: { bg: "rgba(220, 38, 38, 0.15)", text: "#dc2626" },
  中: { bg: "rgba(217, 119, 6, 0.15)", text: "#d97706" },
  低: { bg: "rgba(5, 150, 105, 0.15)", text: "#059669" }
}

function formatPercent(value: number) {
  return `${(value * 100).toFixed(2)}%`
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback
}

export function LossStatisticsPanel() {
  const router = useRouter()
  const [overview, setOverview] = useState<AdminLossStatsOverview | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [executingSuggestionId, setExecutingSuggestionId] = useState<string | null>(null)
  const [executedSuggestionIds, setExecutedSuggestionIds] = useState<Set<string>>(() => new Set())
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  const loadOverview = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      setOverview(await getAdminLossStatsOverview())
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(loadError, "加载经营分析失败"))
    } finally {
      setIsLoading(false)
    }
  }, [handleUnauthorized])

  useEffect(() => {
    void loadOverview()
  }, [loadOverview])

  const statCards = useMemo(() => {
    const data = overview ?? {
      totalBatchCount: 0,
      expiringSoonBatchCount: 0,
      soldOutBatchCount: 0,
      estimatedLossAmount: 0,
      revenueAmount: 0,
      estimatedCostAmount: 0,
      grossProfitAmount: 0,
      operatingExpenseAmount: 0,
      netProfitAmount: 0,
      lossCostAmount: 0,
      grossMarginRate: 0,
      netProfitRate: 0,
      paidOrderCount: 0,
      soldItemQuantity: 0,
      averageOrderAmount: 0
    }

    return [
      {
        title: "营业额",
        value: formatCurrency(data.revenueAmount),
        hint: `${data.paidOrderCount.toLocaleString("zh-CN")} 笔已支付订单`,
        icon: DollarSign,
        gradient: statusGradients.success
      },
      {
        title: "毛利润",
        value: formatCurrency(data.grossProfitAmount),
        hint: `毛利率 ${formatPercent(data.grossMarginRate)}`,
        icon: TrendingDown,
        gradient: statusGradients.purple
      },
      {
        title: "净利润",
        value: formatCurrency(data.netProfitAmount),
        hint: `净利率 ${formatPercent(data.netProfitRate)}`,
        icon: DollarSign,
        gradient: statusGradients.success
      },
      {
        title: "预估成本",
        value: formatCurrency(data.estimatedCostAmount),
        hint: `运营费用 ${formatCurrency(data.operatingExpenseAmount)}`,
        icon: ReceiptText,
        gradient: statusGradients.info
      },
      {
        title: "损耗成本",
        value: formatCurrency(data.lossCostAmount),
        hint: overview ? `损耗率 ${formatPercent(overview.lossRate)}` : "损耗率 0.00%",
        icon: AlertTriangle,
        gradient: statusGradients.error
      },
      {
        title: "客单价",
        value: formatCurrency(data.averageOrderAmount),
        hint: "已支付订单平均金额",
        icon: ReceiptText,
        gradient: statusGradients.warning
      },
      {
        title: "已售商品数",
        value: data.soldItemQuantity.toLocaleString("zh-CN"),
        hint: "按已支付订单明细统计",
        icon: ShoppingBag,
        gradient: primaryGradient
      },
      {
        title: "总批次数",
        value: data.totalBatchCount.toLocaleString("zh-CN"),
        hint: "当前库存批次",
        icon: Package,
        gradient: statusGradients.info
      },
      {
        title: "临期批次",
        value: data.expiringSoonBatchCount.toLocaleString("zh-CN"),
        hint: "配置窗口内待处理",
        icon: AlertTriangle,
        gradient: statusGradients.warning
      },
      {
        title: "已售罄批次",
        value: data.soldOutBatchCount.toLocaleString("zh-CN"),
        hint: "库存已流转完成",
        icon: CheckCircle,
        gradient: statusGradients.success
      },
      {
        title: "预估损耗金额",
        value: formatCurrency(data.estimatedLossAmount),
        hint: overview ? `损耗率 ${formatPercent(overview.lossRate)}` : "损耗率 0.00%",
        icon: TrendingDown,
        gradient: statusGradients.error
      }
    ]
  }, [overview])

  const visibleSuggestions = useMemo(
    () => overview?.suggestions.filter((item) => !executedSuggestionIds.has(item.id)) ?? [],
    [executedSuggestionIds, overview],
  )

  async function executeSuggestion(item: AdminLossStatsSuggestion) {
    setError(null)
    setSuccessMessage(null)
    setExecutingSuggestionId(item.id)

    try {
      if (item.daysToExpire <= 0) {
        await updateInventoryBatchStatus(item.batchId, "paused")
      } else if (item.daysToExpire <= 3) {
        await acceptAdminPricingSuggestion(item.batchId)
      } else {
        router.push(`${DASHBOARD_ROUTES.batches}?keyword=${encodeURIComponent(item.batchCode)}`)
      }

      setExecutedSuggestionIds((current) => new Set(current).add(item.id))
      setSuccessMessage(`已执行 ${item.batchCode} 的处理建议`)
      await loadOverview()
    } catch (executeError) {
      if (isUnauthorizedError(executeError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(executeError, "执行处理建议失败"))
    } finally {
      setExecutingSuggestionId(null)
    }
  }

  function describeSuggestionExecution(item: AdminLossStatsSuggestion) {
    if (item.daysToExpire <= 0) {
      return `将批次 ${item.batchCode} 调整为停用，避免过期库存继续在用户端售卖。`
    }
    if (item.daysToExpire <= 3) {
      return `采纳批次 ${item.batchCode} 的定价建议，为临期库存创建折扣规则。`
    }
    return `跳转到批次管理并按批次号 ${item.batchCode} 筛选，便于人工检查库存和履约情况。`
  }

  function requestExecuteSuggestion(item: AdminLossStatsSuggestion) {
    setConfirmAction({
      title: "确认执行处理建议",
      description: describeSuggestionExecution(item),
      confirmLabel: "执行建议",
      onConfirm: async () => {
        setConfirmAction(null)
        await executeSuggestion(item)
      },
    })
  }

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">经营分析</h1>
          <p className="text-sm text-slate-600">聚合营业额、订单收入、库存批次和损耗风险。</p>
        </div>
        <Button
          className="border-white/50 bg-white/40 text-slate-700 hover:bg-white/60"
          disabled={isLoading}
          onClick={() => void loadOverview()}
          variant="outline"
        >
          <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
          刷新
        </Button>
      </div>

      {error ? (
        <div className="rounded-2xl border border-red-200 bg-red-50/80 px-4 py-3 text-sm text-red-700">{error}</div>
      ) : null}
      {successMessage ? (
        <div className="rounded-2xl border border-emerald-200 bg-emerald-50/80 px-4 py-3 text-sm text-emerald-700">{successMessage}</div>
      ) : null}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {statCards.map((stat) => {
          const Icon = stat.icon
          return (
            <Card key={stat.title} className="border-0" style={glassCard}>
              <CardContent className="p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm text-slate-600">{stat.title}</p>
                    <p className="mt-1 text-2xl font-bold text-slate-800">{stat.value}</p>
                    <p className="mt-1 flex items-center gap-1 text-xs text-slate-500">
                      <ArrowDown className="h-3 w-3 text-slate-400" />
                      {stat.hint}
                    </p>
                  </div>
                  <div className="rounded-xl p-2.5 shadow-lg" style={{ background: stat.gradient }}>
                    <Icon className="h-5 w-5 text-white" />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <Card className="border-0" style={glassCard}>
          <CardHeader>
            <CardTitle className="text-lg text-slate-800">分类损耗统计</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-hidden rounded-xl" style={{ background: "rgba(255, 255, 255, 0.3)" }}>
              <Table>
                <TableHeader>
                  <TableRow className="border-white/30 hover:bg-white/20">
                    <TableHead className="text-slate-700">分类</TableHead>
                    <TableHead className="text-center text-slate-700">批次数</TableHead>
                    <TableHead className="text-center text-slate-700">临期</TableHead>
                    <TableHead className="text-center text-slate-700">售罄</TableHead>
                    <TableHead className="text-right text-slate-700">损耗金额</TableHead>
                    <TableHead className="text-right text-slate-700">损耗率</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {overview?.categoryStats.map((item) => (
                    <TableRow key={item.categoryId} className="border-white/20 transition-colors hover:bg-white/30">
                      <TableCell className="font-medium text-slate-800">{item.categoryName}</TableCell>
                      <TableCell className="text-center text-slate-700">{item.batchCount}</TableCell>
                      <TableCell className="text-center text-amber-600">{item.expiringSoonBatchCount}</TableCell>
                      <TableCell className="text-center text-green-600">{item.soldOutBatchCount}</TableCell>
                      <TableCell className="text-right text-red-600">{formatCurrency(item.estimatedLossAmount)}</TableCell>
                      <TableCell className="text-right text-slate-700">{formatPercent(item.lossRate)}</TableCell>
                    </TableRow>
                  ))}
                  {!isLoading && overview?.categoryStats.length === 0 ? (
                    <TableRow>
                      <TableCell className="py-8 text-center text-slate-500" colSpan={6}>
                        暂无分类损耗数据
                      </TableCell>
                    </TableRow>
                  ) : null}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>

        <Card className="border-0" style={glassCard}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg text-slate-800">
              <div className="rounded-lg p-2 shadow-md" style={{ background: statusGradients.warning }}>
                <Lightbulb className="h-4 w-4 text-white" />
              </div>
              处置建议
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {visibleSuggestions.map((item) => (
                <div
                  className="rounded-xl border border-white/40 bg-white/30 p-4 transition-all duration-200 hover:bg-white/40"
                  key={item.id}
                >
                  <div className="mb-2 flex items-start justify-between gap-3">
                    <div>
                      <span className="font-medium text-slate-800">{item.productName}</span>
                      <span className="ml-2 text-sm text-slate-500">({item.batchCode})</span>
                    </div>
                    <Badge
                      className="border-0"
                      style={{
                        background: priorityStyles[item.priority].bg,
                        color: priorityStyles[item.priority].text
                      }}
                    >
                      优先级: {item.priority}
                    </Badge>
                  </div>
                  <p className="mb-2 text-sm text-slate-600">{item.suggestion}</p>
                  <div className="flex flex-wrap gap-3 text-xs text-slate-500">
                    <span>{item.categoryName}</span>
                    <span>可用库存 {item.availableStock}</span>
                    <span>预估损耗 {formatCurrency(item.estimatedLossAmount)}</span>
                    <span className="font-medium text-blue-600">{item.action}</span>
                  </div>
                  <div className="mt-4 flex justify-end">
                    <Button
                      className="border-0 text-white"
                      disabled={executingSuggestionId === item.id}
                      onClick={() => requestExecuteSuggestion(item)}
                      size="sm"
                      style={{ background: primaryGradient }}
                      type="button"
                    >
                      {executingSuggestionId === item.id ? "执行中" : "执行建议"}
                    </Button>
                  </div>
                </div>
              ))}
              {!isLoading && visibleSuggestions.length === 0 ? (
                <div className="rounded-xl border border-white/40 bg-white/30 p-6 text-center text-sm text-slate-500">
                  暂无需要处理的损耗风险批次
                </div>
              ) : null}
            </div>
          </CardContent>
        </Card>
      </div>
      <ActionConfirmDialog action={confirmAction} onOpenChange={(open) => {
        if (!open) {
          setConfirmAction(null)
        }
      }} />
    </div>
  )
}
