"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { useSearchParams } from "next/navigation"
import { LockKeyhole, MessageSquareText } from "lucide-react"

import { APP_ROUTES, ONE_SECOND_MS, VERIFICATION_CODE_RESEND_SECONDS } from "@/lib/constants"
import { resetPasswordRequest, sendVerificationCodeRequest } from "@/lib/client/api"
import { buildLoginRedirectPath, resolveSafeNextPath } from "@/lib/navigation"
import { accountSchema, resetPasswordSchema } from "@/lib/validation"
import { InlineError, Panel, SectionTitle } from "@/components/common/ui"

export function ForgotPasswordForm() {
  const searchParams = useSearchParams()
  const [form, setForm] = useState({ account: "", newPassword: "", confirmPassword: "", verificationCode: "" })
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
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
    <Panel className="p-6 sm:p-8">
      <SectionTitle title="找回密码" description="通过手机号或邮箱验证码重置密码，重置后可直接去登录。" />
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
          <span className="text-sm font-medium text-slate-700">手机号或邮箱</span>
          <input className="w-full rounded-xl border border-slate-200 px-4 py-3 outline-none transition focus:border-emerald-400" data-testid="user-reset-account" placeholder="13800000000 或 name@example.com" value={form.account} onChange={(event) => setForm((current) => ({ ...current, account: event.target.value }))} />
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">新密码</span>
          <div className="relative">
            <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input className="w-full rounded-xl border border-slate-200 px-4 py-3 pl-11 outline-none transition focus:border-emerald-400" data-testid="user-reset-password" placeholder="至少 8 位，包含字母和数字" type="password" value={form.newPassword} onChange={(event) => setForm((current) => ({ ...current, newPassword: event.target.value }))} />
          </div>
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">确认新密码</span>
          <div className="relative">
            <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input className="w-full rounded-xl border border-slate-200 px-4 py-3 pl-11 outline-none transition focus:border-emerald-400" data-testid="user-reset-confirm-password" placeholder="请再次输入新密码" type="password" value={form.confirmPassword} onChange={(event) => setForm((current) => ({ ...current, confirmPassword: event.target.value }))} />
          </div>
        </label>
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">验证码</span>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <MessageSquareText className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input className="w-full rounded-xl border border-slate-200 px-4 py-3 pl-11 outline-none transition focus:border-emerald-400" data-testid="user-reset-code" inputMode="numeric" placeholder="输入验证码" value={form.verificationCode} onChange={(event) => setForm((current) => ({ ...current, verificationCode: event.target.value }))} />
            </div>
            <button
              className="rounded-xl border border-emerald-200 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
              data-testid="user-reset-send-code"
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
                  const result = await sendVerificationCodeRequest({ account: parsedAccount.data, purpose: "reset_password" })
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
        {error ? <InlineError message={error} /> : null}
        {codeMessage ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{codeMessage}</div> : null}
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
