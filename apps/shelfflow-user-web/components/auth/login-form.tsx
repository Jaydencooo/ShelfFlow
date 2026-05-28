"use client"

import { useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import Link from "next/link"
import { ArrowRight, Leaf, LockKeyhole, Mail, Phone, ShieldCheck, Store } from "lucide-react"

import { APP_ROUTES } from "@/lib/constants"
import { loginRequest } from "@/lib/client/api"
import { buildRegisterRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { loginSchema } from "@/lib/validation"
import { InlineError, Panel } from "@/components/common/ui"

export function LoginForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ account: "", password: "" })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const nextPath = resolveSafeNextPath(searchParams.get("next")) ?? APP_ROUTES.home

  return (
    <Panel className="grid min-h-[640px] overflow-hidden lg:grid-cols-[1.04fr_0.96fr]">
      <div className="relative hidden overflow-hidden bg-[#EEF7EE] p-10 lg:block">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_72%_18%,rgba(16,166,106,0.18),transparent_30%),radial-gradient(circle_at_14%_84%,rgba(255,255,255,0.95),transparent_34%)]" />
        <div className="relative z-10 flex h-full flex-col justify-between">
          <div className="inline-flex w-fit items-center gap-2 rounded-full bg-white/70 px-4 py-2 text-sm font-semibold text-[#079669] shadow-sm">
            <Store className="h-4 w-4" />
            ShelfFlow
          </div>
          <div className="max-w-md space-y-5">
            <div className="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-4 py-2 text-sm font-semibold text-[#079669]">
              <Leaf className="h-4 w-4" />
              社区自提 · 临期好物
            </div>
            <h1 className="text-5xl font-semibold leading-tight tracking-tight text-slate-950">
              把临期好物
              <span className="block text-[#079669]">变成日常补给</span>
            </h1>
            <p className="text-base leading-7 text-slate-600">省钱不浪费，附近自提更安心。登录后继续加购、结算和查看自提进度。</p>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {["临期特惠", "社区自提", "库存可见"].map((item) => (
              <div className="rounded-3xl bg-white/72 p-4 text-sm font-semibold text-slate-700 shadow-sm" key={item}>{item}</div>
            ))}
          </div>
        </div>
        <div className="absolute bottom-24 right-12 h-64 w-64 rounded-full bg-white/55 shadow-[0_28px_80px_rgba(15,23,42,0.12)]" />
        <div className="absolute bottom-32 right-20 h-40 w-40 rounded-[40px] bg-[#079669]/15" />
      </div>
      <div className="flex items-center p-6 sm:p-10">
        <div className="mx-auto w-full max-w-md">
          <div className="inline-flex items-center gap-2 rounded-full border border-emerald-100 bg-emerald-50/80 px-3 py-1 text-xs font-semibold text-[#079669]">
            <ShieldCheck className="h-3.5 w-3.5" />
            欢迎回来
          </div>
          <div className="mt-5 space-y-2">
            <h2 className="text-3xl font-semibold tracking-tight text-slate-950">登录 ShelfFlow</h2>
            <p className="text-sm leading-6 text-slate-500">使用手机号或邮箱登录，继续挑选附近可自提的临期好物。</p>
          </div>
          <form
            className="mt-8 space-y-5"
            onSubmit={async (event) => {
              event.preventDefault()
              setError(null)

              const parsed = loginSchema.safeParse(form)
              if (!parsed.success) {
                setError(parsed.error.issues[0]?.message ?? "登录参数不合法")
                return
              }

              setSubmitting(true)
              try {
                await loginRequest(parsed.data)
                router.replace(nextPath)
                router.refresh()
              } catch (submitError) {
                setError(submitError instanceof Error ? submitError.message : "登录失败")
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
                <input className="h-14 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-16 text-sm outline-none ring-0 transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-login-account" placeholder="13800000000 或 name@example.com" value={form.account} onChange={(event) => setForm((current) => ({ ...current, account: event.target.value }))} />
              </div>
            </label>
            <label className="block space-y-2">
              <span className="text-sm font-semibold text-slate-700">密码</span>
              <div className="relative">
                <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input className="h-14 w-full rounded-2xl border border-slate-900/[0.08] bg-white/88 px-4 pl-11 text-sm outline-none ring-0 transition focus:border-emerald-300 focus:bg-white focus:shadow-[0_0_0_4px_rgba(16,166,106,0.10)]" data-testid="user-login-password" placeholder="请输入密码" type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} />
              </div>
            </label>
            {error ? <InlineError message={error} /> : null}
            <button className="inline-flex h-14 w-full items-center justify-center rounded-2xl bg-[#079669] px-5 text-sm font-semibold text-white shadow-[0_18px_36px_rgba(7,150,105,0.24)] transition hover:bg-[#07845d] disabled:cursor-not-allowed disabled:bg-slate-300" data-testid="user-login-submit" disabled={submitting} type="submit">
              {submitting ? "登录中..." : "登录并继续"}
              <ArrowRight className="ml-2 h-4 w-4" />
            </button>
            <div className="flex items-center justify-between text-sm text-slate-500">
              <Link className="font-semibold text-[#079669] transition hover:text-emerald-800" data-testid="user-login-register-link" href={buildRegisterRedirectPath(nextPath)}>
                还没有账号？去注册
              </Link>
              <Link className="font-medium text-slate-600 transition hover:text-slate-900" data-testid="user-login-forgot-link" href={`${APP_ROUTES.forgotPassword}?next=${encodeURIComponent(nextPath)}`}>
                找回密码
              </Link>
            </div>
          </form>
        </div>
      </div>
    </Panel>
  )
}
