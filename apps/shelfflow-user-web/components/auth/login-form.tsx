"use client"

import { useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import Link from "next/link"

import { APP_ROUTES } from "@/lib/constants"
import { loginRequest } from "@/lib/client/api"
import { buildRegisterRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { loginSchema } from "@/lib/validation"
import { InlineError, Panel, SectionTitle } from "@/components/common/ui"

export function LoginForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ openId: "", password: "" })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const nextPath = resolveSafeNextPath(searchParams.get("next")) ?? APP_ROUTES.products

  return (
    <Panel className="p-6 sm:p-8">
      <SectionTitle title="登录用户端" description="使用已注册的账号和密码进入用户端。" />
      <div className="mt-4 rounded-xl border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-700">
        登录后会回到你刚才访问的页面；如果没有来源页面，会进入商品目录。
      </div>
      <form
        className="mt-6 space-y-4"
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
          <span className="text-sm font-medium text-slate-700">账号</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none ring-0 transition focus:border-emerald-400" data-testid="user-login-openid" placeholder="例如 sf-user-demo-01" value={form.openId} onChange={(event) => setForm((current) => ({ ...current, openId: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">密码</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none ring-0 transition focus:border-emerald-400" data-testid="user-login-password" placeholder="请输入密码" type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} />
        </label>
        {error ? <InlineError message={error} /> : null}
        <button className="w-full rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300" data-testid="user-login-submit" disabled={submitting} type="submit">
          {submitting ? "登录中..." : "登录"}
        </button>
        <div className="flex items-center justify-between text-sm text-slate-500">
          <Link className="font-medium text-emerald-700 transition hover:text-emerald-800" data-testid="user-login-register-link" href={buildRegisterRedirectPath(nextPath)}>
            还没有账号？去注册
          </Link>
          <Link className="font-medium text-slate-600 transition hover:text-slate-900" data-testid="user-login-forgot-link" href={`${APP_ROUTES.forgotPassword}?next=${encodeURIComponent(nextPath)}`}>
            找回密码
          </Link>
        </div>
      </form>
    </Panel>
  )
}
