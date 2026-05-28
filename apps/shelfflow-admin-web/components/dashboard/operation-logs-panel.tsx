"use client"

import type { ReactNode } from "react"
import { useCallback, useEffect, useMemo, useState } from "react"
import { Activity, CheckCircle2, ListChecks, RefreshCw, RotateCcw, XCircle } from "lucide-react"

import { getAdminOperationLogPage, isUnauthorizedError, logoutRequest } from "@/lib/client/api"
import { DASHBOARD_ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants"
import { formatDateTime } from "@/lib/formatters"
import { glassCard, glassInputClassName } from "@/lib/glass-styles"
import type { AdminOperationLog } from "@/lib/types"
import { useRouter } from "next/navigation"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

const moduleOptions = [
  "商品管理",
  "批次管理",
  "定价规则",
  "订单履约",
  "自提点管理",
  "AI 运营助手",
] as const

const actionOptions = ["新增或执行", "更新", "删除", "状态变更", "自提核销"] as const
const successStatusLowerBound = 200
const successStatusUpperBound = 400

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function isSuccessStatus(statusCode?: number) {
  return Boolean(statusCode && statusCode >= successStatusLowerBound && statusCode < successStatusUpperBound)
}

function resolveStatusTone(statusCode?: number) {
  if (!statusCode) {
    return "bg-slate-100 text-slate-600"
  }
  if (isSuccessStatus(statusCode)) {
    return "bg-emerald-100 text-emerald-700"
  }
  return "bg-red-100 text-red-700"
}

function resolveMethodTone(method: string) {
  const normalizedMethod = method.toUpperCase()
  if (normalizedMethod === "POST") {
    return "bg-blue-100 text-blue-700"
  }
  if (normalizedMethod === "PUT") {
    return "bg-amber-100 text-amber-700"
  }
  if (normalizedMethod === "DELETE") {
    return "bg-red-100 text-red-700"
  }
  return "bg-slate-100 text-slate-600"
}

function formatOperationPath(path: string) {
  return path.replace(/^\/api\/admin/, "")
}

function getFilterSummary(module: string, action: string) {
  if (!module && !action) {
    return "当前展示全部管理端关键写操作"
  }
  return [module || "全部模块", action || "全部操作"].join(" / ")
}

function LogMetricCard(props: { label: string; value: number | string; hint: string; icon: ReactNode }) {
  return (
    <Card className="border-0" style={glassCard}>
      <CardContent className="flex items-center gap-3 p-4">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/45 text-slate-700">
          {props.icon}
        </div>
        <div>
          <p className="text-sm text-slate-500">{props.label}</p>
          <p className="mt-1 text-xl font-semibold text-slate-950">{props.value}</p>
          <p className="mt-1 text-xs text-slate-500">{props.hint}</p>
        </div>
      </CardContent>
    </Card>
  )
}

export function OperationLogsPanel() {
  const router = useRouter()
  const [logs, setLogs] = useState<AdminOperationLog[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [module, setModule] = useState("")
  const [action, setAction] = useState("")
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const totalPages = useMemo(() => Math.max(1, Math.ceil(total / DEFAULT_PAGE_SIZE)), [total])
  const successCount = useMemo(() => logs.filter((log) => isSuccessStatus(log.statusCode)).length, [logs])
  const failedCount = useMemo(() => logs.filter((log) => log.statusCode && !isSuccessStatus(log.statusCode)).length, [logs])
  const filterSummary = useMemo(() => getFilterSummary(module, action), [action, module])

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  const loadLogs = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await getAdminOperationLogPage({
        page,
        pageSize: DEFAULT_PAGE_SIZE,
        module: module || undefined,
        action: action || undefined,
      })
      setLogs(result.items)
      setTotal(result.total)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(loadError, "加载操作日志失败"))
    } finally {
      setLoading(false)
    }
  }, [action, handleUnauthorized, module, page])

  useEffect(() => {
    void loadLogs()
  }, [loadLogs])

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">操作日志</h1>
        <p className="text-sm text-slate-600">查看管理端关键写操作，便于追踪商品、批次、订单、定价和 AI 建议执行记录。</p>
      </header>

      <div className="grid gap-4 md:grid-cols-3">
        <LogMetricCard
          hint={filterSummary}
          icon={<Activity className="h-5 w-5" />}
          label="匹配记录"
          value={total}
        />
        <LogMetricCard
          hint="当前页成功操作"
          icon={<CheckCircle2 className="h-5 w-5 text-emerald-600" />}
          label="成功"
          value={successCount}
        />
        <LogMetricCard
          hint="当前页失败操作"
          icon={<XCircle className="h-5 w-5 text-red-600" />}
          label="失败"
          value={failedCount}
        />
      </div>

      <Card className="border-0" style={glassCard}>
        <CardContent className="grid gap-4 p-5 md:grid-cols-[220px_220px_auto_auto]">
          <div className="space-y-2">
            <Label htmlFor="operationLogModule">模块</Label>
            <select
              className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
              id="operationLogModule"
              onChange={(event) => {
                setPage(1)
                setModule(event.target.value)
              }}
              value={module}
            >
              <option value="">全部模块</option>
              {moduleOptions.map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="operationLogAction">操作</Label>
            <select
              className={`flex h-10 w-full rounded-md px-3 text-sm ${glassInputClassName}`}
              id="operationLogAction"
              onChange={(event) => {
                setPage(1)
                setAction(event.target.value)
              }}
              value={action}
            >
              <option value="">全部操作</option>
              {actionOptions.map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
          </div>

          <div className="flex items-end">
            <Button className="w-full border-white/50 bg-white/30 hover:bg-white/50 md:w-auto" onClick={() => void loadLogs()} variant="outline">
              <RefreshCw className="mr-2 h-4 w-4" />
              刷新
            </Button>
          </div>

          <div className="flex items-end">
            <Button
              className="w-full border-white/50 bg-white/30 hover:bg-white/50 md:w-auto"
              disabled={!module && !action}
              onClick={() => {
                setPage(1)
                setModule("")
                setAction("")
              }}
              variant="outline"
            >
              <RotateCcw className="mr-2 h-4 w-4" />
              清空筛选
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card className="border-0" style={glassCard}>
        <CardHeader className="flex flex-row items-center justify-between gap-3">
          <div>
            <CardTitle className="text-lg">日志列表</CardTitle>
            <p className="mt-1 text-sm text-slate-500">总计 {total} 条记录</p>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-12 text-sm text-slate-500">
              <Spinner className="mr-2" />
              正在加载操作日志...
            </div>
          ) : null}

          {!loading && error ? (
            <Alert variant="destructive">
              <AlertTitle>加载失败</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          ) : null}

          {!loading && !error && logs.length === 0 ? (
            <Empty className="border-white/40 bg-white/20">
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <ListChecks className="size-5" />
                </EmptyMedia>
                <EmptyTitle>暂无操作日志</EmptyTitle>
                <EmptyDescription>当前筛选条件下没有管理端操作记录。</EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : null}

          {!loading && !error && logs.length > 0 ? (
            <div className="space-y-4">
              <div className="overflow-hidden rounded-xl border border-white/40 bg-white/30">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>时间</TableHead>
                      <TableHead>模块</TableHead>
                      <TableHead>操作</TableHead>
                      <TableHead>结果</TableHead>
                      <TableHead>请求</TableHead>
                      <TableHead>操作者</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {logs.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell>
                          <div className="space-y-1">
                            <p className="font-medium text-slate-900">{formatDateTime(log.createTime)}</p>
                            <p className="text-xs text-slate-500">日志 ID：{log.id}</p>
                          </div>
                        </TableCell>
                        <TableCell className="font-medium text-slate-900">{log.module}</TableCell>
                        <TableCell>{log.action}</TableCell>
                        <TableCell>
                          <span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${resolveStatusTone(log.statusCode)}`}>
                            {log.summary}
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="space-y-2 text-xs text-slate-500">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className={`inline-flex rounded-full px-2 py-1 font-medium ${resolveMethodTone(log.method)}`}>
                                {log.method}
                              </span>
                              <span className="break-all text-slate-700">{formatOperationPath(log.path)}</span>
                            </div>
                            <p>HTTP 状态码：{log.statusCode ?? "-"}</p>
                          </div>
                        </TableCell>
                        <TableCell>{log.actorId ? `管理员 ${log.actorId}` : "-"}</TableCell>
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
    </div>
  )
}
