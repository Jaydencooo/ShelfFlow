import { Suspense } from "react"

import { LoginForm } from "@/components/auth/login-form"

export default function LoginPage() {
  return (
    <div className="mx-auto max-w-md py-12">
      <Suspense fallback={<div className="rounded-2xl border border-white/70 bg-white/90 p-8 text-sm text-slate-500 shadow-sm">加载登录页中...</div>}>
        <LoginForm />
      </Suspense>
    </div>
  )
}
