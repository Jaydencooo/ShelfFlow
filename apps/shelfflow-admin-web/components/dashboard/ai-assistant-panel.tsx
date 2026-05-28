"use client"

import { useRouter } from "next/navigation"
import { useCallback, useEffect, useMemo, useState } from "react"
import { Book, Bot, Edit, History, MessageSquare, Plus, RefreshCw, Search, Send, Sparkles, Trash2 } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import {
  createAdminAiKnowledge,
  deleteAdminAiKnowledge,
  getAdminAiChatHistory,
  getAdminAiKnowledge,
  getAdminAiOpsSuggestionActions,
  getAdminAiOpsSuggestions,
  isUnauthorizedError,
  logoutRequest,
  sendAdminAiChatMessage,
  updateAdminAiOpsSuggestionAction,
  updateAdminAiKnowledge,
} from "@/lib/client/api"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { formatDate, formatDateTime } from "@/lib/formatters"
import { glassCard, glassDialog, glassInputClassName, primaryGradient, primaryShadow, statusGradients } from "@/lib/glass-styles"
import type { AdminAiKnowledge, AdminAiKnowledgeUpsert, AdminAiOpsSuggestion, AdminAiOpsSuggestionAction, BatchStatus } from "@/lib/types"
import { aiKnowledgeSchema } from "@/lib/validation"
import type { ActionConfirmState } from "@/components/dashboard/action-confirm-dialog"
import { ActionConfirmDialog } from "@/components/dashboard/action-confirm-dialog"

interface ConversationMessage {
  role: "user" | "assistant"
  content: string
  time: string
  provider?: string
  model?: string
  references?: string[]
}

const quickQuestions = ["如何降低本月损耗率？", "哪些批次需要紧急处理？", "推荐最佳促销策略", "临期乳制品应该怎么处理？"]
const priorityLabels: Record<AdminAiOpsSuggestion["priority"], string> = { HIGH: "高", MEDIUM: "中", LOW: "低" }
const priorityStyles: Record<AdminAiOpsSuggestion["priority"], { bg: string; text: string }> = {
  HIGH: { bg: "rgba(220, 38, 38, 0.15)", text: "#dc2626" },
  MEDIUM: { bg: "rgba(217, 119, 6, 0.15)", text: "#d97706" },
  LOW: { bg: "rgba(5, 150, 105, 0.15)", text: "#059669" }
}
const AI_KNOWLEDGE_PAGE_SIZE = 20
const AI_SEARCH_DEBOUNCE_MS = 350
const batchStatusLabels: Record<BatchStatus, string> = {
  draft: "草稿",
  active: "起售",
  paused: "停用",
  sold_out: "售罄",
  expired: "过期",
}

const suggestionActionLabels: Record<AdminAiOpsSuggestionAction["action"], string> = {
  execute: "执行",
  ignore: "忽略",
}

const suggestionActionStatusClasses: Record<AdminAiOpsSuggestionAction["status"], string> = {
  executed: "border-0 bg-emerald-100 text-emerald-700",
  ignored: "border-0 bg-slate-200 text-slate-700",
}

const targetTypeLabels: Record<string, string> = {
  inventory_batch: "库存批次",
  pricing_rule: "定价规则",
  product: "商品",
}

function currentTime() {
  return new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" })
}

function emptyKnowledgeForm(): AdminAiKnowledgeUpsert {
  return { title: "", category: "", content: "" }
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message ? error.message : fallback
}

function parseOperationPayload(payload?: string) {
  if (!payload) {
    return []
  }

  try {
    const parsed = JSON.parse(payload) as Record<string, unknown>
    return Object.entries(parsed)
      .filter(([, value]) => value !== null && value !== undefined && String(value).trim() !== "")
      .map(([key, value]) => `${key}: ${String(value)}`)
  } catch {
    return [payload]
  }
}

export function AiAssistantPanel() {
  const router = useRouter()
  const [knowledge, setKnowledge] = useState<AdminAiKnowledge[]>([])
  const [suggestions, setSuggestions] = useState<AdminAiOpsSuggestion[]>([])
  const [suggestionActions, setSuggestionActions] = useState<AdminAiOpsSuggestionAction[]>([])
  const [knowledgeTotal, setKnowledgeTotal] = useState(0)
  const [searchTerm, setSearchTerm] = useState("")
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("")
  const [chatInput, setChatInput] = useState("")
  const [conversation, setConversation] = useState<ConversationMessage[]>([{ role: "assistant", content: "我是 ShelfFlow 运营助手。你可以问我临期批次处理、定价策略、损耗控制或履约运营问题。", time: currentTime() }])
  const [chatSessionId, setChatSessionId] = useState<string | undefined>()
  const [executingSuggestionId, setExecutingSuggestionId] = useState<string | null>(null)
  const [isKnowledgeDialogOpen, setIsKnowledgeDialogOpen] = useState(false)
  const [editingKnowledge, setEditingKnowledge] = useState<AdminAiKnowledge | null>(null)
  const [knowledgeForm, setKnowledgeForm] = useState<AdminAiKnowledgeUpsert>(emptyKnowledgeForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [confirmAction, setConfirmAction] = useState<ActionConfirmState | null>(null)
  const [selectedSuggestion, setSelectedSuggestion] = useState<AdminAiOpsSuggestion | null>(null)
  const [suggestionBatchStatus, setSuggestionBatchStatus] = useState<BatchStatus>("paused")
  const [suggestionOperationNote, setSuggestionOperationNote] = useState("")

  const handleUnauthorized = useCallback(async () => {
    await logoutRequest().catch(() => undefined)
    router.replace(DASHBOARD_ROUTES.login)
    router.refresh()
  }, [router])

  const loadKnowledge = useCallback(async () => {
    try {
      const result = await getAdminAiKnowledge({ page: 1, pageSize: AI_KNOWLEDGE_PAGE_SIZE, keyword: debouncedSearchTerm || undefined, sortBy: "updatedAt", sortOrder: "desc" })
      setKnowledge(result.items)
      setKnowledgeTotal(result.total)
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(loadError, "加载知识库失败"))
    }
  }, [debouncedSearchTerm, handleUnauthorized])

  const loadData = useCallback(async () => {
    setIsLoading(true)
    setActionError(null)
    try {
      const [knowledgeResult, suggestionResult, actionResult, chatHistory] = await Promise.all([
        getAdminAiKnowledge({ page: 1, pageSize: AI_KNOWLEDGE_PAGE_SIZE, keyword: debouncedSearchTerm || undefined, sortBy: "updatedAt", sortOrder: "desc" }),
        getAdminAiOpsSuggestions(),
        getAdminAiOpsSuggestionActions(),
        getAdminAiChatHistory(chatSessionId)
      ])
      setKnowledge(knowledgeResult.items)
      setKnowledgeTotal(knowledgeResult.total)
      setSuggestions(suggestionResult)
      setSuggestionActions(actionResult)
      if (chatHistory.length > 0) {
        setChatSessionId(chatHistory[0]?.sessionId)
        setConversation(chatHistory.map((message) => ({
          role: message.role,
          content: message.content,
          provider: message.provider,
          model: message.model,
          references: message.references,
          time: new Date(message.createTime).toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" }),
        })))
      }
    } catch (loadError) {
      if (isUnauthorizedError(loadError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(loadError, "加载 AI 运营助手数据失败"))
    } finally {
      setIsLoading(false)
    }
  }, [chatSessionId, debouncedSearchTerm, handleUnauthorized])

  useEffect(() => {
    void loadData()
  }, [loadData])

  useEffect(() => {
    const debounceTimer = window.setTimeout(() => {
      setDebouncedSearchTerm(searchTerm.trim())
    }, AI_SEARCH_DEBOUNCE_MS)

    return () => window.clearTimeout(debounceTimer)
  }, [searchTerm])

  const metrics = useMemo(() => ({
    knowledge: knowledgeTotal,
    suggestions: suggestions.length,
    actionRecords: suggestionActions.length,
    highPriority: suggestions.filter((item) => item.priority === "HIGH").length
  }), [knowledgeTotal, suggestions, suggestionActions.length])

  function openCreateDialog() {
    setEditingKnowledge(null)
    setKnowledgeForm(emptyKnowledgeForm())
    setActionError(null)
    setIsKnowledgeDialogOpen(true)
  }

  function openEditDialog(item: AdminAiKnowledge) {
    setEditingKnowledge(item)
    setKnowledgeForm({ title: item.title, category: item.category, content: item.content })
    setActionError(null)
    setIsKnowledgeDialogOpen(true)
  }

  async function handleSaveKnowledge() {
    setActionError(null)
    setSuccessMessage(null)

    const parsedPayload = aiKnowledgeSchema.safeParse(knowledgeForm)
    if (!parsedPayload.success) {
      setActionError(parsedPayload.error.issues[0]?.message ?? "知识条目参数不合法")
      return
    }

    try {
      if (editingKnowledge) {
        await updateAdminAiKnowledge(editingKnowledge.id, parsedPayload.data)
        setSuccessMessage("知识条目已更新")
      } else {
        await createAdminAiKnowledge(parsedPayload.data)
        setSuccessMessage("知识条目已创建")
      }
      setIsKnowledgeDialogOpen(false)
      await loadKnowledge()
    } catch (saveError) {
      if (isUnauthorizedError(saveError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(saveError, "知识条目保存失败"))
    }
  }

  async function handleDeleteKnowledge(item: AdminAiKnowledge) {
    setActionError(null)
    setSuccessMessage(null)
    try {
      await deleteAdminAiKnowledge(item.id)
      setSuccessMessage(`知识条目 ${item.title} 已删除`)
      await loadKnowledge()
    } catch (deleteError) {
      if (isUnauthorizedError(deleteError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(deleteError, "知识条目删除失败"))
    }
  }

  function requestDeleteKnowledge(item: AdminAiKnowledge) {
    setConfirmAction({
      title: "确认删除知识条目",
      description: `删除「${item.title}」后，AI 运营助手将不再引用这条知识。该操作不可在页面撤销。`,
      confirmLabel: "删除",
      onConfirm: async () => {
        setConfirmAction(null)
        await handleDeleteKnowledge(item)
      },
    })
  }

  async function handleSendMessage(message = chatInput) {
    const normalized = message.trim()
    if (!normalized || isSending) return
    setConversation((items) => [...items, { role: "user", content: normalized, time: currentTime() }])
    setChatInput("")
    setIsSending(true)
    setActionError(null)
    try {
      const response = await sendAdminAiChatMessage(normalized, chatSessionId)
      setChatSessionId(response.sessionId)
      setConversation((items) => [...items, { role: "assistant", content: response.answer, provider: response.provider, model: response.model, references: response.references, time: currentTime() }])
    } catch (chatError) {
      if (isUnauthorizedError(chatError)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(chatError, "AI 问答失败"))
    } finally {
      setIsSending(false)
    }
  }

  function openSuggestionExecution(item: AdminAiOpsSuggestion) {
    setSelectedSuggestion(item)
    setSuggestionBatchStatus(item.executionPlan?.defaultBatchStatus ?? "paused")
    setSuggestionOperationNote("")
    setActionError(null)
    setSuccessMessage(null)
  }

  async function handleSuggestionAction(item: AdminAiOpsSuggestion, action: "execute" | "ignore", options: { batchStatus?: BatchStatus; operationNote?: string } = {}) {
    setActionError(null)
    setSuccessMessage(null)
    setExecutingSuggestionId(item.id)
    try {
      await updateAdminAiOpsSuggestionAction(item.id, { action, batchStatus: options.batchStatus, operationNote: options.operationNote })
      setSuccessMessage(action === "execute" ? `已执行建议：${item.title}` : `已忽略建议：${item.title}`)
      setSuggestions((current) => current.filter((suggestion) => suggestion.id !== item.id))
      setSuggestionActions(await getAdminAiOpsSuggestionActions())
      setSelectedSuggestion(null)
    } catch (error) {
      if (isUnauthorizedError(error)) {
        await handleUnauthorized()
        return
      }
      setActionError(getErrorMessage(error, "运营建议处理失败"))
    } finally {
      setExecutingSuggestionId(null)
    }
  }

  return (
    <div className="mx-auto w-full max-w-[1600px] space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">AI 运营助手</h1>
          <p className="text-sm text-slate-600">基于运营知识库和实时批次数据提供问答、建议与处置参考。</p>
        </div>
        <Button className="border-white/50 bg-white/40 text-slate-700 hover:bg-white/60" disabled={isLoading} onClick={() => void loadData()} variant="outline">
          <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />刷新
        </Button>
      </div>

      {actionError ? <div className="rounded-2xl border border-red-200 bg-red-50/80 px-4 py-3 text-sm text-red-700">{actionError}</div> : null}
      {successMessage ? <div className="rounded-2xl border border-emerald-200 bg-emerald-50/80 px-4 py-3 text-sm text-emerald-700">{successMessage}</div> : null}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {[
          { label: "知识条目", value: metrics.knowledge, icon: Book, gradient: primaryGradient },
          { label: "运营建议", value: metrics.suggestions, icon: Sparkles, gradient: statusGradients.purple },
          { label: "执行记录", value: metrics.actionRecords, icon: Bot, gradient: statusGradients.cyan }
        ].map((metric) => {
          const Icon = metric.icon
          return (
            <Card className="border-0" key={metric.label} style={glassCard}>
              <CardContent className="flex items-center justify-between p-5">
                <div><p className="text-sm text-slate-600">{metric.label}</p><p className="mt-1 text-2xl font-bold text-slate-800">{metric.value}</p></div>
                <div className="rounded-xl p-3 shadow-lg" style={{ background: metric.gradient }}><Icon className="h-5 w-5 text-white" /></div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <Tabs className="w-full" defaultValue="chat">
        <div className="inline-flex rounded-xl p-1" style={glassCard}>
          <TabsList className="bg-transparent">
            <TabsTrigger className="gap-1 data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="chat"><MessageSquare className="h-4 w-4" />智能问答</TabsTrigger>
            <TabsTrigger className="gap-1 data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="knowledge"><Book className="h-4 w-4" />知识库管理</TabsTrigger>
            <TabsTrigger className="gap-1 data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="suggestions"><Sparkles className="h-4 w-4" />运营建议</TabsTrigger>
            <TabsTrigger className="gap-1 data-[state=active]:bg-white/50 data-[state=active]:text-slate-800 text-slate-600" value="actions"><History className="h-4 w-4" />执行记录</TabsTrigger>
          </TabsList>
        </div>

        <TabsContent className="mt-4" value="chat">
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
            <Card className="overflow-hidden border-0 xl:col-span-2" style={glassCard}>
              <CardHeader className="border-b border-white/30"><CardTitle className="flex items-center gap-2 text-lg text-slate-800"><div className="rounded-lg p-2 shadow-md" style={{ background: statusGradients.purple }}><Bot className="h-4 w-4 text-white" /></div>智能问答</CardTitle></CardHeader>
              <CardContent className="p-0">
                <div className="h-[420px] space-y-4 overflow-y-auto p-4">
                  {conversation.map((message, index) => (
                    <div className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`} key={`${message.time}-${index}`}>
                      <div className={`max-w-[85%] rounded-2xl p-4 ${message.role === "user" ? "text-white" : ""}`} style={message.role === "user" ? { background: primaryGradient, boxShadow: primaryShadow } : { background: "rgba(255, 255, 255, 0.5)", border: "1px solid rgba(255, 255, 255, 0.4)" }}>
                        {message.role === "assistant" ? <div className="mb-2 flex items-center gap-1 text-purple-600"><Sparkles className="h-4 w-4" /><span className="text-xs font-medium">AI 助手</span></div> : null}
                        <p className={`whitespace-pre-wrap text-sm leading-6 ${message.role === "user" ? "text-white" : "text-slate-700"}`}>{message.content}</p>
                        {message.provider ? (
                          <div className="mt-3 flex flex-wrap gap-2">
                            <Badge className="border-0 bg-white/50 text-slate-600">模型: {message.provider}/{message.model}</Badge>
                          </div>
                        ) : null}
                        {message.references?.length ? <div className="mt-2 flex flex-wrap gap-2">{message.references.map((reference) => <Badge className="border-0 bg-white/50 text-slate-600" key={reference}>{reference}</Badge>)}</div> : null}
                        <p className={`mt-2 text-xs ${message.role === "user" ? "text-blue-100" : "text-slate-400"}`}>{message.time}</p>
                      </div>
                    </div>
                  ))}
                </div>
                <div className="border-t border-white/30 p-4">
                  <div className="flex items-center gap-2">
                    <Input className="flex-1 border-white/50 bg-white/40 transition-all focus:bg-white/60" onChange={(event) => setChatInput(event.target.value)} onKeyDown={(event) => { if (event.key === "Enter") void handleSendMessage() }} placeholder="输入问题，例如：哪些批次需要紧急处理？" value={chatInput} />
                    <Button className="border-0 text-white" disabled={isSending} onClick={() => void handleSendMessage()} style={{ background: primaryGradient, boxShadow: primaryShadow }}><Send className="h-4 w-4" /></Button>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card className="border-0" style={glassCard}>
              <CardHeader><CardTitle className="text-lg text-slate-800">快速提问</CardTitle></CardHeader>
              <CardContent className="space-y-3">{quickQuestions.map((question) => <Button className="h-auto w-full justify-start border-white/50 bg-white/30 py-3 text-left text-slate-700 transition-all hover:bg-white/50" key={question} onClick={() => void handleSendMessage(question)} variant="outline"><span className="text-sm">{question}</span></Button>)}</CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent className="mt-4 space-y-4" value="knowledge">
          <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl p-4" style={glassCard}>
            <Dialog onOpenChange={setIsKnowledgeDialogOpen} open={isKnowledgeDialogOpen}>
              <DialogTrigger asChild><Button className="border-0 text-white" data-testid="admin-ai-knowledge-open" onClick={openCreateDialog} style={{ background: primaryGradient, boxShadow: primaryShadow }}><Plus className="mr-2 h-4 w-4" />写入知识</Button></DialogTrigger>
              <DialogContent className="border-0" style={glassDialog}>
                <DialogHeader><DialogTitle className="text-slate-800">{editingKnowledge ? "编辑知识条目" : "添加知识条目"}</DialogTitle></DialogHeader>
                <div className="space-y-4 py-4">
                  <div className="space-y-2"><Label htmlFor="knowledgeTitle">标题</Label><Input className={glassInputClassName} data-testid="admin-ai-knowledge-title" id="knowledgeTitle" onChange={(event) => setKnowledgeForm((value) => ({ ...value, title: event.target.value }))} value={knowledgeForm.title} /></div>
                  <div className="space-y-2"><Label htmlFor="knowledgeCategory">分类</Label><Input className={glassInputClassName} data-testid="admin-ai-knowledge-category" id="knowledgeCategory" onChange={(event) => setKnowledgeForm((value) => ({ ...value, category: event.target.value }))} value={knowledgeForm.category} /></div>
                  <div className="space-y-2"><Label htmlFor="knowledgeContent">内容</Label><Textarea className="min-h-[140px] border-white/50 bg-white/50 transition-all focus:bg-white/70" data-testid="admin-ai-knowledge-content" id="knowledgeContent" onChange={(event) => setKnowledgeForm((value) => ({ ...value, content: event.target.value }))} value={knowledgeForm.content} /></div>
                  <Button className="w-full border-0 text-white" data-testid="admin-ai-knowledge-submit" onClick={() => void handleSaveKnowledge()} style={{ background: primaryGradient, boxShadow: primaryShadow }}>保存知识</Button>
                </div>
              </DialogContent>
            </Dialog>
            <div className="relative"><Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" /><Input className="w-64 border-white/50 bg-white/40 pl-10 transition-all focus:bg-white/60" onChange={(event) => setSearchTerm(event.target.value)} placeholder="搜索知识库..." value={searchTerm} /></div>
          </div>
          <Card className="border-0" style={glassCard}>
            <CardHeader><CardTitle className="text-lg text-slate-800">知识库列表</CardTitle></CardHeader>
            <CardContent>
              <div className="overflow-hidden rounded-xl" style={{ background: "rgba(255, 255, 255, 0.3)" }}>
                <Table><TableHeader><TableRow className="border-white/30 hover:bg-white/20"><TableHead className="text-slate-700">标题</TableHead><TableHead className="text-slate-700">分类</TableHead><TableHead className="text-slate-700">更新时间</TableHead><TableHead className="text-right text-slate-700">操作</TableHead></TableRow></TableHeader>
                  <TableBody>
                    {knowledge.map((item) => <TableRow className="border-white/20 transition-colors hover:bg-white/30" key={item.id}><TableCell><p className="font-medium text-slate-800">{item.title}</p><p className="mt-1 line-clamp-1 text-xs text-slate-500">{item.content}</p></TableCell><TableCell><Badge className="border-white/50 bg-white/30 text-slate-600" variant="outline">{item.category}</Badge></TableCell><TableCell className="text-slate-700">{item.updateTime ? formatDate(item.updateTime) : "-"}</TableCell><TableCell className="text-right"><Button className="text-slate-600 hover:bg-white/40 hover:text-slate-800" onClick={() => openEditDialog(item)} size="sm" variant="ghost"><Edit className="h-4 w-4" /></Button><Button className="text-red-500 hover:bg-red-50/50 hover:text-red-600" onClick={() => requestDeleteKnowledge(item)} size="sm" variant="ghost"><Trash2 className="h-4 w-4" /></Button></TableCell></TableRow>)}
                    {!isLoading && knowledge.length === 0 ? (
                      <TableRow>
                        <TableCell className="py-8 text-center text-slate-500" colSpan={4}>
                          暂无知识条目，可以写入运营规则、履约口径或损耗处理经验。
                        </TableCell>
                      </TableRow>
                    ) : null}
                  </TableBody></Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent className="mt-4" value="suggestions">
          <Card className="border-0" style={glassCard}>
            <CardHeader><CardTitle className="text-lg text-slate-800">实时运营建议</CardTitle></CardHeader>
            <CardContent className="grid gap-4 md:grid-cols-2">
              {isLoading ? (
                <div className="flex items-center justify-center rounded-xl border border-white/40 bg-white/30 p-8 text-sm text-slate-500 md:col-span-2">
                  <Spinner className="mr-2" />
                  正在生成实时运营建议...
                </div>
              ) : null}
              {suggestions.map((item) => <div className="rounded-xl border border-white/40 bg-white/30 p-4" key={item.id}><div className="flex items-start justify-between gap-3"><div><p className="font-semibold text-slate-900">{item.title}</p><p className="mt-1 text-sm text-slate-600">{item.content}</p></div><Badge className="border-0" style={{ background: priorityStyles[item.priority].bg, color: priorityStyles[item.priority].text }}>{priorityLabels[item.priority]}</Badge></div><p className="mt-3 text-sm font-medium text-blue-600">{item.suggestedAction}</p>{item.executionPlan ? <div className="mt-3 rounded-lg border border-white/40 bg-white/30 p-3 text-sm text-slate-600"><p className="font-medium text-slate-800">执行方案</p><p className="mt-1">{item.executionPlan.summary}</p></div> : null}<div className="mt-4 flex justify-end gap-2"><Button className="border-white/50 bg-white/30 text-slate-700 hover:bg-white/50" disabled={executingSuggestionId === item.id} onClick={() => void handleSuggestionAction(item, "ignore")} size="sm" variant="outline">忽略</Button><Button className="border-0 text-white" disabled={executingSuggestionId === item.id} onClick={() => openSuggestionExecution(item)} size="sm" style={{ background: primaryGradient }}>{executingSuggestionId === item.id ? "处理中" : "执行建议"}</Button></div></div>)}
              {!isLoading && suggestions.length === 0 ? (
                <div className="rounded-xl border border-white/40 bg-white/30 p-6 text-center text-sm text-slate-500 md:col-span-2">
                  暂无实时运营建议。
                </div>
              ) : null}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent className="mt-4" value="actions">
          <Card className="border-0" style={glassCard}>
            <CardHeader><CardTitle className="text-lg text-slate-800">运营建议执行记录</CardTitle></CardHeader>
            <CardContent>
              <div className="overflow-hidden rounded-xl" style={{ background: "rgba(255, 255, 255, 0.3)" }}>
                <Table><TableHeader><TableRow className="border-white/30 hover:bg-white/20"><TableHead className="text-slate-700">建议</TableHead><TableHead className="text-slate-700">动作</TableHead><TableHead className="text-slate-700">目标</TableHead><TableHead className="text-slate-700">结果</TableHead><TableHead className="text-slate-700">执行参数</TableHead><TableHead className="text-slate-700">时间</TableHead></TableRow></TableHeader>
                  <TableBody>
                    {isLoading ? (
                      <TableRow>
                        <TableCell className="py-8 text-center text-slate-500" colSpan={6}>
                          <span className="inline-flex items-center">
                            <Spinner className="mr-2" />
                            正在加载执行记录...
                          </span>
                        </TableCell>
                      </TableRow>
                    ) : null}
                    {!isLoading && suggestionActions.map((item) => {
                      const payloadItems = parseOperationPayload(item.operationPayload)
                      return (
                        <TableRow className="border-white/20 transition-colors hover:bg-white/30" key={item.id}>
                          <TableCell className="text-slate-700">{item.suggestionId}</TableCell>
                          <TableCell>
                            <Badge className={suggestionActionStatusClasses[item.status]}>
                              {suggestionActionLabels[item.action]}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-slate-700">
                            <p>{item.targetName ?? item.targetId}</p>
                            <p className="text-xs text-slate-500">{targetTypeLabels[item.targetType] ?? item.targetType}</p>
                          </TableCell>
                          <TableCell className="max-w-[320px] text-slate-700">{item.operationSummary}</TableCell>
                          <TableCell className="max-w-[300px] text-xs text-slate-500">
                            {payloadItems.length > 0 ? payloadItems.map((payloadItem) => <p key={payloadItem}>{payloadItem}</p>) : "-"}
                          </TableCell>
                          <TableCell className="text-slate-700">{formatDateTime(item.createTime)}</TableCell>
                        </TableRow>
                      )
                    })}
                    {!isLoading && suggestionActions.length === 0 ? (
                      <TableRow><TableCell className="py-8 text-center text-slate-500" colSpan={6}>暂无运营建议执行记录。</TableCell></TableRow>
                    ) : null}
                  </TableBody></Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      <Dialog onOpenChange={(open) => { if (!open) setSelectedSuggestion(null) }} open={Boolean(selectedSuggestion)}>
        <DialogContent className="border-0" style={glassDialog}>
          <DialogHeader><DialogTitle className="text-slate-800">执行运营建议</DialogTitle></DialogHeader>
          {selectedSuggestion ? (
            <div className="space-y-4 py-2">
              <div className="rounded-xl border border-white/40 bg-white/30 p-4">
                <p className="font-semibold text-slate-900">{selectedSuggestion.title}</p>
                <p className="mt-2 text-sm leading-6 text-slate-600">{selectedSuggestion.executionPlan?.summary ?? selectedSuggestion.suggestedAction}</p>
              </div>
              {selectedSuggestion.executionPlan?.editableFields.includes("batchStatus") ? (
                <div className="space-y-2">
                  <Label htmlFor="suggestionBatchStatus">批次状态</Label>
                  <select className="h-10 w-full rounded-md border border-white/50 bg-white/50 px-3 text-sm text-slate-700 outline-none transition-all focus:bg-white/70" id="suggestionBatchStatus" onChange={(event) => setSuggestionBatchStatus(event.target.value as BatchStatus)} value={suggestionBatchStatus}>
                    {(["paused", "sold_out", "expired", "active"] as BatchStatus[]).map((status) => <option key={status} value={status}>{batchStatusLabels[status]}</option>)}
                  </select>
                </div>
              ) : null}
              <div className="space-y-2">
                <Label htmlFor="suggestionOperationNote">执行备注</Label>
                <Textarea className="min-h-[96px] border-white/50 bg-white/50 transition-all focus:bg-white/70" id="suggestionOperationNote" onChange={(event) => setSuggestionOperationNote(event.target.value)} placeholder="记录具体处理方式，例如：已通知团长置顶，今晚 20 点前复盘库存。" value={suggestionOperationNote} />
              </div>
              <Button className="w-full border-0 text-white" disabled={executingSuggestionId === selectedSuggestion.id} onClick={() => void handleSuggestionAction(selectedSuggestion, "execute", { batchStatus: suggestionBatchStatus, operationNote: suggestionOperationNote })} style={{ background: primaryGradient, boxShadow: primaryShadow }}>
                {executingSuggestionId === selectedSuggestion.id ? "执行中" : "确认执行"}
              </Button>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>
      <ActionConfirmDialog action={confirmAction} onOpenChange={(open) => {
        if (!open) {
          setConfirmAction(null)
        }
      }} />
    </div>
  )
}
