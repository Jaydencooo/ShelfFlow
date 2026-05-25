"use client"

import Link from "next/link"
import { useState } from "react"
import { useSearchParams } from "next/navigation"

import { APP_ROUTES } from "@/lib/constants"
import { resetPasswordRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { resetPasswordSchema } from "@/lib/validation"
import { InlineError, Panel, SectionTitle } from "@/components/common/ui"

export function ForgotPasswordForm() {
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ openId: "", phone: "", newPassword: "" })
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const nextPath = resolveSafeNextPath(searchParams.get("next")) ?? APP_ROUTES.products

  return (
    <Panel className="p-6 sm:p-8">
      <SectionTitle title="找回密码" description="通过账号和手机号重置测试密码，重置后可直接去登录。" />
      <form
        className="mt-6 space-y-4"
        onSubmit={async (event) => {
          event.preventDefault()
          setError(null)
          setSuccess(null)

          const parsed = resetPasswordSchema.safeParse(form)
          if (!parsed.success) {
            setError(parsed.error.issues[0]?.message ?? "重置参数不合法")
            return
          }

          setSubmitting(true)
          try {
            await resetPasswordRequest(parsed.data)
            setSuccess("密码已重置，现在可以直接登录。")
          } catch (submitError) {
            setError(submitError instanceof Error ? submitError.message : "密码重置失败")
          } finally {
            setSubmitting(false)
          }
        }}
      >
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">账号</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-reset-openid" placeholder="请输入注册账号" value={form.openId} onChange={(event) => setForm((current) => ({ ...current, openId: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">手机号</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-reset-phone" placeholder="13800000000" value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">新密码</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-reset-password" placeholder="至少 8 位，包含字母和数字" type="password" value={form.newPassword} onChange={(event) => setForm((current) => ({ ...current, newPassword: event.target.value }))} />
        </label>
        {error ? <InlineError message={error} /> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}
        <button className="w-full rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300" data-testid="user-reset-submit" disabled={submitting} type="submit">
          {submitting ? "重置中..." : "重置密码"}
        </button>
        <div className="text-sm text-slate-500">
          想起密码了？
          <Link className="ml-1 font-medium text-emerald-700 transition hover:text-emerald-800" data-testid="user-reset-login-link" href={buildLoginRedirectPath(nextPath)}>
            返回登录
          </Link>
        </div>
      </form>
    </Panel>
  )
}
