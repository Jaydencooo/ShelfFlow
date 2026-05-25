import { Suspense } from "react"

import { ForgotPasswordForm } from "@/components/auth/forgot-password-form"

export default function ForgotPasswordPage() {
  return (
    <div className="mx-auto max-w-md py-12">
      <Suspense fallback={<div className="rounded-2xl border border-white/70 bg-white/90 p-8 text-sm text-slate-500 shadow-sm">加载密码重置页中...</div>}>
        <ForgotPasswordForm />
      </Suspense>
    </div>
  )
}
