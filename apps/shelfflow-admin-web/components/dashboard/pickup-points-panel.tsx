"use client"

import { useRouter } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { Edit, MapPin, Plus, RefreshCw, Trash2 } from "lucide-react"

import {
  createAdminPickupPoint,
  deleteAdminPickupPoint,
  getAdminPickupPoints,
  isUnauthorizedError,
  logoutRequest,
  updateAdminPickupPoint,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { formatDateTime } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type { PickupPoint, PickupPointUpsert } from "@/lib/types"
import { pickupPointSchema } from "@/lib/validation"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

const emptyForm: PickupPointUpsert = {
  name: "",
  address: "",
  contactName: "",
  contactPhone: "",
  serviceTime: "",
  enabled: true,
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback
}

export function PickupPointsPanel() {
  const router = useRouter()
  const [pickupPoints, setPickupPoints] = useState<PickupPoint[]>([])
  const [form, setForm] = useState<PickupPointUpsert>(emptyForm)
  const [editingPickupPoint, setEditingPickupPoint] = useState<PickupPoint | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  const loadPickupPoints = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setPickupPoints(await getAdminPickupPoints())
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(loadError, "加载自提点失败"))
    } finally {
      setLoading(false)
    }
  }, [handleUnauthorized])

  useEffect(() => {
    void loadPickupPoints()
  }, [loadPickupPoints])

  const metrics = useMemo(() => {
    const enabled = pickupPoints.filter((item) => item.enabled).length
    return {
      total: pickupPoints.length,
      enabled,
      disabled: pickupPoints.length - enabled,
    }
  }, [pickupPoints])

  function openCreateDialog() {
    setEditingPickupPoint(null)
    setForm(emptyForm)
    setError(null)
    setSuccessMessage(null)
    setDialogOpen(true)
  }

  function openEditDialog(item: PickupPoint) {
    setEditingPickupPoint(item)
    setForm({
      name: item.name,
      address: item.address,
      contactName: item.contactName ?? "",
      contactPhone: item.contactPhone ?? "",
      serviceTime: item.serviceTime ?? "",
      sort: item.sort,
      enabled: item.enabled,
    })
    setError(null)
    setDialogOpen(true)
  }

  async function handleSave() {
    setSaving(true)
    setError(null)
    setSuccessMessage(null)
    const parsed = pickupPointSchema.safeParse(form)
    if (!parsed.success) {
      setError(parsed.error.issues[0]?.message ?? "自提点参数不合法")
      setSaving(false)
      return
    }

    try {
      if (editingPickupPoint) {
        await updateAdminPickupPoint(editingPickupPoint.id, parsed.data)
        setSuccessMessage("自提点已更新")
      } else {
        await createAdminPickupPoint(parsed.data)
        setSuccessMessage("自提点已创建")
      }
      setDialogOpen(false)
      await loadPickupPoints()
    } catch (saveError) {
      if (isUnauthorizedError(saveError)) {
        await handleUnauthorized()
        return
      }
      setError(getErrorMessage(saveError, "自提点保存失败"))
    } finally {
      setSaving(false)
    }
  }

  function requestDisable(item: PickupPoint) {
    setConfirmAction({
      title: "确认停用自提点",
      description: `停用「${item.name}」后，用户下单时将不能再选择该自提点，历史订单不受影响。`,
      confirmLabel: "停用",
      onConfirm: async () => {
        setConfirmAction(null)
        setError(null)
        setSuccessMessage(null)
        try {
          await deleteAdminPickupPoint(item.id)
          setSuccessMessage("自提点已停用")
          await loadPickupPoints()
        } catch (deleteError) {
          if (isUnauthorizedError(deleteError)) {
            await handleUnauthorized()
            return
          }
          setError(getErrorMessage(deleteError, "自提点停用失败"))
        }
      },
    })
  }

  function updateForm<T extends keyof PickupPointUpsert>(field: T, value: PickupPointUpsert[T]) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">自提点管理</h1>
          <p className="text-sm text-slate-600">维护社区自提点，用户下单时会选择自提点并写入订单快照。</p>
        </div>
        <div className="flex gap-2">
          <Button className="border-white/50 bg-white/40 text-slate-700 hover:bg-white/60" disabled={loading} onClick={() => void loadPickupPoints()} variant="outline">
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />刷新
          </Button>
          <Dialog onOpenChange={setDialogOpen} open={dialogOpen}>
            <DialogTrigger asChild>
              <Button className="border-0 text-white" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                <Plus className="mr-2 h-4 w-4" />新增自提点
              </Button>
            </DialogTrigger>
            <DialogContent className="border-0" style={glassDialog}>
              <DialogHeader>
                <DialogTitle className="text-slate-800">{editingPickupPoint ? "编辑自提点" : "新增自提点"}</DialogTitle>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="pickupPointName">名称</Label>
                  <Input id="pickupPointName" className={glassInputClassName} onChange={(event) => updateForm("name", event.target.value)} value={form.name} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="pickupPointAddress">地址</Label>
                  <Input id="pickupPointAddress" className={glassInputClassName} onChange={(event) => updateForm("address", event.target.value)} value={form.address} />
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="pickupPointContactName">联系人</Label>
                    <Input id="pickupPointContactName" className={glassInputClassName} onChange={(event) => updateForm("contactName", event.target.value)} value={form.contactName ?? ""} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="pickupPointContactPhone">联系电话</Label>
                    <Input id="pickupPointContactPhone" className={glassInputClassName} onChange={(event) => updateForm("contactPhone", event.target.value)} value={form.contactPhone ?? ""} />
                  </div>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="pickupPointServiceTime">服务时间</Label>
                    <Input id="pickupPointServiceTime" className={glassInputClassName} onChange={(event) => updateForm("serviceTime", event.target.value)} value={form.serviceTime ?? ""} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="pickupPointSort">排序</Label>
                    <Input
                      id="pickupPointSort"
                      className={glassInputClassName}
                      min={0}
                      onChange={(event) => updateForm("sort", event.target.value ? Number(event.target.value) : undefined)}
                      type="number"
                      value={form.sort ?? ""}
                    />
                  </div>
                </div>
                <label className="flex items-center gap-2 text-sm text-slate-700">
                  <input checked={form.enabled ?? true} className="h-4 w-4" onChange={(event) => updateForm("enabled", event.target.checked)} type="checkbox" />
                  启用
                </label>
                <Button className="w-full border-0 text-white" disabled={saving} onClick={() => void handleSave()} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                  {saving ? (
                    <>
                      <Spinner className="mr-2" />
                      保存中
                    </>
                  ) : "保存"}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {error ? <div className="rounded-2xl border border-red-200 bg-red-50/80 px-4 py-3 text-sm text-red-700">{error}</div> : null}
      {successMessage ? <div className="rounded-2xl border border-emerald-200 bg-emerald-50/80 px-4 py-3 text-sm text-emerald-700">{successMessage}</div> : null}

      <div className="grid gap-4 md:grid-cols-3">
        <MetricCard label="自提点总数" value={metrics.total} />
        <MetricCard label="启用中" value={metrics.enabled} />
        <MetricCard label="已停用" value={metrics.disabled} />
      </div>

      <Card className="border-0" style={glassCard}>
        <CardHeader><CardTitle className="flex items-center gap-2 text-lg text-slate-800"><MapPin className="h-5 w-5" />自提点列表</CardTitle></CardHeader>
        <CardContent>
          <div className="overflow-hidden rounded-xl" style={{ background: "rgba(255, 255, 255, 0.3)" }}>
            <Table>
              <TableHeader>
                <TableRow className="border-white/30 hover:bg-white/20">
                  <TableHead>名称</TableHead>
                  <TableHead>地址</TableHead>
                  <TableHead>服务时间</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>更新时间</TableHead>
                  <TableHead className="w-[180px] text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell className="py-10 text-center text-slate-500" colSpan={6}>
                      <span className="inline-flex items-center">
                        <Spinner className="mr-2" />
                        正在加载自提点...
                      </span>
                    </TableCell>
                  </TableRow>
                ) : null}
                {!loading && pickupPoints.map((item) => (
                  <TableRow className="border-white/20 transition-colors hover:bg-white/30" key={item.id}>
                    <TableCell>
                      <p className="font-medium text-slate-900">{item.name}</p>
                      <p className="text-xs text-slate-500">{item.contactName || "-"} {item.contactPhone || ""}</p>
                    </TableCell>
                    <TableCell className="max-w-[360px] text-slate-700">{item.address}</TableCell>
                    <TableCell className="text-slate-700">{item.serviceTime || "-"}</TableCell>
                    <TableCell>
                      <Badge className={item.enabled ? "border-0 bg-emerald-100 text-emerald-700" : "border-0 bg-red-100 text-red-700"}>
                        {item.enabled ? "启用" : "停用"}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-slate-700">{item.updateTime ? formatDateTime(item.updateTime) : "-"}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button className="w-20 border-white/50 bg-white/30 hover:bg-white/50" onClick={() => openEditDialog(item)} size="sm" variant="outline">
                          <Edit className="mr-2 h-4 w-4" />
                          编辑
                        </Button>
                        <Button
                          className="w-20 border-red-200 bg-red-50/70 text-red-600 hover:bg-red-100"
                          disabled={!item.enabled}
                          onClick={() => requestDisable(item)}
                          size="sm"
                          variant="outline"
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          停用
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
                {!loading && pickupPoints.length === 0 ? (
                  <TableRow>
                    <TableCell className="py-10 text-center text-slate-500" colSpan={6}>暂无自提点，请先新增社区自提点。</TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          </div>
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

function MetricCard(props: { label: string; value: number }) {
  return (
    <Card className="border-0" style={glassCard}>
      <CardContent className="p-5">
        <p className="text-sm text-slate-600">{props.label}</p>
        <p className="mt-1 text-2xl font-bold text-slate-800">{props.value.toLocaleString("zh-CN")}</p>
      </CardContent>
    </Card>
  )
}
