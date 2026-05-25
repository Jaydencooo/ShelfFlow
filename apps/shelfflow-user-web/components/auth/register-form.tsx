"use client"

import Link from "next/link"
import { useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"

import { APP_ROUTES } from "@/lib/constants"
import { registerRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { registerSchema } from "@/lib/validation"
import { InlineError, Panel, SectionTitle } from "@/components/common/ui"

export function RegisterForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ openId: "", name: "", phone: "", password: "" })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const nextPath = resolveSafeNextPath(searchParams.get("next")) ?? APP_ROUTES.products

  return (
    <Panel className="p-6 sm:p-8">
      <SectionTitle title="注册用户账号" description="创建一个可登录、可找回密码的正式测试账号。" />
      <form
        className="mt-6 space-y-4"
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
          <span className="text-sm font-medium text-slate-700">账号</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-register-openid" placeholder="例如 sf-user-demo-03" value={form.openId} onChange={(event) => setForm((current) => ({ ...current, openId: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">昵称</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-register-name" placeholder="请输入昵称" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">手机号</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-register-phone" placeholder="13800000000" value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">密码</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-register-password" placeholder="至少 8 位，包含字母和数字" type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} />
        </label>
        {error ? <InlineError message={error} /> : null}
        <button className="w-full rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300" data-testid="user-register-submit" disabled={submitting} type="submit">
          {submitting ? "注册中..." : "注册并登录"}
        </button>
        <div className="text-sm text-slate-500">
          已有账号？
          <Link className="ml-1 font-medium text-emerald-700 transition hover:text-emerald-800" data-testid="user-register-login-link" href={buildLoginRedirectPath(nextPath)}>
            直接登录
          </Link>
        </div>
      </form>
    </Panel>
  )
}
