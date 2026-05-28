import { Suspense } from "react"

import { LoginForm } from "@/components/auth/login-form"

export default function LoginPage() {
  return (
    <div className="mx-auto max-w-6xl py-8 lg:py-12">
      <Suspense fallback={<div className="rounded-[28px] border border-slate-900/[0.08] bg-white/85 p-8 text-sm text-slate-500 shadow-sm">加载登录页中...</div>}>
        <LoginForm />
      </Suspense>
    </div>
  )
}
