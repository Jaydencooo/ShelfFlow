"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import {
  AlertTriangle,
  ArrowUpRight,
  Bot,
  CheckCircle,
  Package,
  PackageCheck,
  RefreshCw,
  ShoppingCart,
  Tags,
  TrendingDown,
} from "lucide-react"

import {
  getAdminAiOpsSuggestions,
  getAdminLossStatsOverview,
  getAdminOrders,
  getAdminPricingRules,
  getInventoryBatches,
  isUnauthorizedError,
  logoutRequest,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { formatCurrency } from "@/lib/formatters"
import { glassCard, primaryGradient, statusGradients } from "@/lib/glass-styles"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Spinner } from "@/components/ui/spinner"

const quickActions = [
  {
    title: "商品管理",
    description: "商品资料、分类、售价与上下架状态",
    icon: PackageCheck,
    href: DASHBOARD_ROUTES.products,
    gradient: statusGradients.info,
  },
  {
    title: "批次管理",
    description: "商品、批次、库存状态与价格基础信息",
    icon: Package,
    href: DASHBOARD_ROUTES.batches,
    gradient: primaryGradient,
  },
  {
    title: "定价规则",
    description: "临期折扣规则、AI 建议审核与价格策略",
    icon: Tags,
    href: DASHBOARD_ROUTES.pricing,
    gradient: statusGradients.purple,
  },
  {
    title: "订单管理",
    description: "订单列表、支付状态、用户明细与履约流转",
    icon: ShoppingCart,
    href: DASHBOARD_ROUTES.orderMonitor,
    gradient: statusGradients.success,
  },
  {
    title: "经营分析",
    description: "营业额、订单收入、损耗金额与处置建议",
    icon: TrendingDown,
    href: DASHBOARD_ROUTES.lossStatistics,
    gradient: statusGradients.error,
  },
  {
    title: "AI 运营助手",
    description: "知识库、运营问答与临期处置建议",
    icon: Bot,
    href: DASHBOARD_ROUTES.aiAssistant,
    gradient: statusGradients.cyan,
  },
] as const

const OVERVIEW_SAMPLE_PAGE_SIZE = 1

interface DashboardOverviewState {
  batchTotal: number
  orderTotal: number
  pricingRuleTotal: number
  estimatedLossAmount: number
  aiSuggestionTotal: number
  expiringSoonBatchCount: number
}

interface ActivityItem {
  type: "success" | "warning"
  message: string
  time: string
}

const emptyOverview: DashboardOverviewState = {
  batchTotal: 0,
  orderTotal: 0,
  pricingRuleTotal: 0,
  estimatedLossAmount: 0,
  aiSuggestionTotal: 0,
  expiringSoonBatchCount: 0,
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback
}

export function DashboardOverviewPanel() {
  const router = useRouter()
  const [overview, setOverview] = useState<DashboardOverviewState>(emptyOverview)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  const loadOverview = useCallback(async () => {
    setIsLoading(true)
    setError(null)

    try {
      const [batchResult, orderResult, pricingResult, lossOverview, aiSuggestions] = await Promise.all([
        getInventoryBatches({ page: 1, pageSize: OVERVIEW_SAMPLE_PAGE_SIZE }),
        getAdminOrders({ page: 1, pageSize: OVERVIEW_SAMPLE_PAGE_SIZE, sortBy: "orderTime", sortOrder: "desc" }),
        getAdminPricingRules({ page: 1, pageSize: OVERVIEW_SAMPLE_PAGE_SIZE, sortBy: "updatedAt", sortOrder: "desc" }),
        getAdminLossStatsOverview(),
        getAdminAiOpsSuggestions(),
      ])

      setOverview({
        batchTotal: batchResult.total,
        orderTotal: orderResult.total,
        pricingRuleTotal: pricingResult.total,
        estimatedLossAmount: lossOverview.estimatedLossAmount,
        aiSuggestionTotal: aiSuggestions.length,
        expiringSoonBatchCount: lossOverview.expiringSoonBatchCount,
      })
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }

      setError(getErrorMessage(loadError, "加载管理端概览失败"))
    } finally {
      setIsLoading(false)
    }
  }, [handleUnauthorized])

  useEffect(() => {
    void loadOverview()
  }, [loadOverview])

  const statItems = useMemo(
    () => [
      {
        title: "库存批次",
        value: overview.batchTotal.toLocaleString("zh-CN"),
        hint: `${overview.expiringSoonBatchCount.toLocaleString("zh-CN")} 个临期批次需关注`,
        icon: Package,
        gradient: primaryGradient,
      },
      {
        title: "订单总数",
        value: overview.orderTotal.toLocaleString("zh-CN"),
        hint: "用户端订单生命周期数据",
        icon: ShoppingCart,
        gradient: statusGradients.info,
      },
      {
        title: "定价规则",
        value: overview.pricingRuleTotal.toLocaleString("zh-CN"),
        hint: `${overview.aiSuggestionTotal.toLocaleString("zh-CN")} 条 AI 运营建议`,
        icon: Tags,
        gradient: statusGradients.purple,
      },
      {
        title: "预估损耗",
        value: formatCurrency(overview.estimatedLossAmount),
        hint: "来自批次库存和到期风险",
        icon: TrendingDown,
        gradient: statusGradients.error,
      },
    ],
    [overview],
  )

  const activityItems = useMemo<ActivityItem[]>(
    () => [
      {
        type: overview.orderTotal > 0 ? "success" : "warning",
        message: overview.orderTotal > 0 ? "订单监控已读取真实 Java 后端订单数据" : "当前暂无订单数据，可通过用户端下单验证履约链路",
        time: "实时",
      },
      {
        type: overview.batchTotal > 0 ? "success" : "warning",
        message: overview.batchTotal > 0 ? "批次管理、损耗统计已接入真实库存批次" : "当前暂无库存批次，请先在批次管理创建可售批次",
        time: "实时",
      },
      {
        type: overview.pricingRuleTotal > 0 || overview.aiSuggestionTotal > 0 ? "success" : "warning",
        message: overview.pricingRuleTotal > 0 || overview.aiSuggestionTotal > 0
          ? "定价规则和 AI 运营建议可用于运营决策"
          : "暂无定价规则或 AI 运营建议，可在定价模块补齐策略",
        time: "实时",
      },
    ],
    [overview],
  )

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">系统概览</h1>
          <p className="text-sm text-slate-600">聚合库存、订单、定价、损耗和 AI 运营建议的实时状态。</p>
        </div>
        <Button
          className="border-white/50 bg-white/40 text-slate-700 hover:bg-white/60"
          disabled={isLoading}
          onClick={() => void loadOverview()}
          variant="outline"
        >
          <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
          刷新概览
        </Button>
      </div>

      {error ? (
        <div className="rounded-2xl border border-red-200 bg-red-50/80 px-4 py-3 text-sm text-red-700">{error}</div>
      ) : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {statItems.map((stat) => {
          const Icon = stat.icon
          return (
            <Card className="cursor-default border-0 transition-all duration-300 hover:-translate-y-1" key={stat.title} style={glassCard}>
              <CardContent className="p-5">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm text-slate-600">{stat.title}</p>
                    <p className="mt-1 text-2xl font-bold text-slate-800">
                      {isLoading ? <Spinner className="inline-flex" /> : stat.value}
                    </p>
                    <p className="mt-1 text-xs text-slate-500">{stat.hint}</p>
                  </div>
                  <div className="rounded-xl p-2.5" style={{ background: stat.gradient, boxShadow: "0 4px 12px rgba(15, 23, 42, 0.15)" }}>
                    <Icon className="h-5 w-5 text-white" />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <div>
        <h2 className="mb-4 text-lg font-semibold text-slate-800">快速入口</h2>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {quickActions.map((action) => {
            const Icon = action.icon
            return (
              <Link href={action.href} key={action.title}>
                <Card className="h-full cursor-pointer border-0 transition-all duration-300 hover:-translate-y-1" style={glassCard}>
                  <CardContent className="p-5">
                    <div className="flex items-start gap-4">
                      <div className="rounded-xl p-3" style={{ background: action.gradient, boxShadow: "0 4px 14px rgba(15, 23, 42, 0.2)" }}>
                        <Icon className="h-5 w-5 text-white" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center justify-between gap-3">
                          <h3 className="font-semibold text-slate-800">{action.title}</h3>
                          <ArrowUpRight className="h-4 w-4 text-slate-400" />
                        </div>
                        <p className="mt-1 text-sm leading-6 text-slate-600">{action.description}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            )
          })}
        </div>
      </div>

      <Card className="border-0" style={glassCard}>
        <CardHeader>
          <CardTitle className="text-lg text-slate-800">最近动态</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {activityItems.map((activity) => (
            <div className="flex items-start gap-3 rounded-xl p-3 transition-all hover:bg-white/30" key={activity.message}>
              <div
                className="mt-0.5 flex h-8 w-8 items-center justify-center rounded-full"
                style={{ background: activity.type === "success" ? statusGradients.success : statusGradients.warning }}
              >
                {activity.type === "success" ? <CheckCircle className="h-4 w-4 text-white" /> : <AlertTriangle className="h-4 w-4 text-white" />}
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-medium text-slate-800">{activity.message}</p>
                <p className="mt-1 text-xs text-slate-500">{activity.time}</p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
