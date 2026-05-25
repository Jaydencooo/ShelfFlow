import { Suspense } from "react"

import { RegisterForm } from "@/components/auth/register-form"

export default function RegisterPage() {
  return (
    <div className="mx-auto max-w-md py-12">
      <Suspense fallback={<div className="rounded-2xl border border-white/70 bg-white/90 p-8 text-sm text-slate-500 shadow-sm">加载注册页中...</div>}>
        <RegisterForm />
      </Suspense>
    </div>
  )
}
