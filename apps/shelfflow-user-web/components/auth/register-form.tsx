"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { ArrowRight, BadgeCheck, Leaf, LockKeyhole, Mail, MessageSquareText, Phone, ShoppingBag, UserRound } from "lucide-react"

import { APP_ROUTES, ONE_SECOND_MS, VERIFICATION_CODE_RESEND_SECONDS } from "@/lib/constants"
import { registerRequest, sendVerificationCodeRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { accountSchema, registerSchema } from "@/lib/validation"
import { InlineError, Panel } from "@/components/common/ui"

export function RegisterForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ account: "", name: "", password: "", confirmPassword: "", verificationCode: "" })
  const [error, setError] = useState<string | null>(null)
  const [codeMessage, setCodeMessage] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [sendingCode, setSendingCode] = useState(false)
  const [codeCooldownSeconds, setCodeCooldownSeconds] = useState(0)
  const nextPath = resolveSafeNextPath(searchParams.get("next")) ?? APP_ROUTES.products

  useEffect(() => {
    if (codeCooldownSeconds <= 0) {
      return undefined
    }

    const timer = window.setTimeout(() => setCodeCooldownSeconds((seconds) => Math.max(0, seconds - 1)), ONE_SECOND_MS)
    return () => window.clearTimeout(timer)
  }, [codeCooldownSeconds])

  return (
    <Panel className="grid min-h-[720px] overflow-hidden lg:grid-cols-[0.92fr_1.08fr]">
      <div className="relative hidden overflow-hidden bg-[#EFF7EA] p-10 lg:block">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_18%_20%,rgba(16,166,106,0.16),transparent_28%),radial-gradient(circle_at_80%_82%,rgba(255,255,255,0.95),transparent_36%)]" />
        <div className="relative z-10 flex h-full flex-col justify-between">
          <div className="inline-flex w-fit items-center gap-2 rounded-full bg-white/75 px-4 py-2 text-sm font-semibold text-[#079669] shadow-sm">
            <ShoppingBag className="h-4 w-4" />
            ShelfFlow
          </div>
          <div className="space-y-5">
            <div className="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-4 py-2 text-sm font-semibold text-[#079669]">
              <Leaf className="h-4 w-4" />
              今天开始省钱不浪费
            </div>
            <h1 className="text-5xl font-semibold leading-tight tracking-tight text-slate-950">
              创建账号，
              <span className="block text-[#079669]">把附近好物带回家</span>
            </h1>
            <p className="max-w-sm text-base leading-7 text-slate-600">注册后可以保存自提信息、统一结算购物车，并追踪订单取货进度。</p>
          </div>
          <div className="rounded-[28px] bg-white/70 p-5 text-sm leading-6 text-slate-600 shadow-sm">
            低价不是将就，而是把即将被浪费的好商品，重新放回日常生活。
          </div>
        </div>
      </div>
      <div className="flex items-center p-6 sm:p-10">
        <div className="mx-auto w-full max-w-md">
          <div className="inline-flex items-center gap-2 rounded-full border border-emerald-100 bg-emerald-50/80 px-3 py-1 text-xs font-semibold text-[#079669]">
            <BadgeCheck className="h-3.5 w-3.5" />
            新用户注册
          </div>
          <div className="mt-5 space-y-2">
            <h2 className="text-3xl font-semibold tracking-tight text-slate-950">创建 ShelfFlow 账号</h2>
            <p className="text-sm leading-6 text-slate-500">用手机号或邮箱注册，登录后即可下单、自提和管理个人信息。</p>
          </div>
          <form
            className="mt-8 space-y-4"
            onSubmit={async (event) => {
              event.preventDefault()
              setError(null)

              const parsed = registerSchema.safeParse(form)
              if (!parsed.success) {
                setError(parsed.error.issues[0]?.message ?? "注册参数不合法")
                return
              }

              setSubmitting(true)
              try {
                await registerRequest(parsed.data)
                router.replace(nextPath)
                router.refresh()
              } catch (submitError) {
                setError(submitError instanceof Error ? submitError.message : "注册失败")
              } finally {
                setSubmitting(false)
              }
            }}
          >
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">手机号或邮箱</span>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Phone className="pointer-events-none absolute left-10 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-300" />
                <input className="h-13 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-16 text-sm outline-none transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-register-account" placeholder="13800000000 或 name@example.com" value={form.account} onChange={(event) => setForm((current) => ({ ...current, account: event.target.value }))} />
              </div>
            </label>
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">用户名</span>
              <div className="relative">
                <UserRound className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input className="h-13 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-11 text-sm outline-none transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-register-name" placeholder="请输入用户名" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
              </div>
            </label>
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">密码</span>
              <div className="relative">
                <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input className="h-13 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-11 text-sm outline-none transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-register-password" placeholder="至少 8 位，包含字母和数字" type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} />
              </div>
            </label>
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">确认密码</span>
              <div className="relative">
                <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input className="h-13 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-11 text-sm outline-none transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-register-confirm-password" placeholder="请再次输入密码" type="password" value={form.confirmPassword} onChange={(event) => setForm((current) => ({ ...current, confirmPassword: event.target.value }))} />
              </div>
            </label>
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">验证码</span>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <MessageSquareText className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input className="h-13 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-11 text-sm outline-none transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-register-code" inputMode="numeric" placeholder="输入验证码" value={form.verificationCode} onChange={(event) => setForm((current) => ({ ...current, verificationCode: event.target.value }))} />
                </div>
                <button
                  className="h-13 shrink-0 rounded-2xl border border-emerald-200 bg-white px-4 text-sm font-semibold text-[#079669] transition hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
                  data-testid="user-register-send-code"
                  disabled={sendingCode || codeCooldownSeconds > 0}
                  onClick={async () => {
                    setError(null)
                    setCodeMessage(null)
                    const parsedAccount = accountSchema.safeParse(form.account)
                    if (!parsedAccount.success) {
                      setError(parsedAccount.error.issues[0]?.message ?? "请输入有效的手机号或邮箱")
                      return
                    }
                    setSendingCode(true)
                    try {
                      const result = await sendVerificationCodeRequest({ account: parsedAccount.data, purpose: "register" })
                      setCodeMessage(result.debugCode ? `本地验证码：${result.debugCode}` : "验证码已发送，请查收。")
                      setCodeCooldownSeconds(VERIFICATION_CODE_RESEND_SECONDS)
                    } catch (sendError) {
                      setError(sendError instanceof Error ? sendError.message : "验证码发送失败")
                    } finally {
                      setSendingCode(false)
                    }
                  }}
                  type="button"
                >
                  {sendingCode ? "发送中" : codeCooldownSeconds > 0 ? `${codeCooldownSeconds}s` : "获取验证码"}
                </button>
              </div>
            </label>
            {codeMessage ? <div className="rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-[#079669]">{codeMessage}</div> : null}
            {error ? <InlineError message={error} /> : null}
            <button className="inline-flex h-14 w-full items-center justify-center rounded-2xl bg-[#079669] px-5 text-sm font-semibold text-white shadow-[0_18px_36px_rgba(7,150,105,0.24)] transition hover:bg-[#07845d] disabled:cursor-not-allowed disabled:bg-slate-300" data-testid="user-register-submit" disabled={submitting} type="submit">
              {submitting ? "注册中..." : "注册并登录"}
              <ArrowRight className="ml-2 h-4 w-4" />
            </button>
            <div className="text-sm text-slate-500">
              已有账号？
              <Link className="ml-1 font-semibold text-[#079669] transition hover:text-emerald-800" data-testid="user-register-login-link" href={buildLoginRedirectPath(nextPath)}>
                直接登录
              </Link>
            </div>
          </form>
        </div>
      </div>
    </Panel>
  )
}
