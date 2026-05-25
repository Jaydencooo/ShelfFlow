import { redirect } from "next/navigation"

import { LoginForm } from "@/components/dashboard/login-form"
import { DASHBOARD_DEFAULT_ROUTE } from "@/lib/constants"
import { readSession } from "@/lib/server/session"

export default async function LoginPage() {
  const session = await readSession()

  if (session) {
    redirect(DASHBOARD_DEFAULT_ROUTE)
  }

  return (
    <div
      className="flex min-h-screen items-center justify-center px-4 py-12"
      style={{
        backgroundImage: "url('/images/gradient-background.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundAttachment: "fixed",
      }}
    >
      <div className="absolute inset-0 bg-white/10" />
      <div className="relative z-10 w-full max-w-md">
        <LoginForm />
      </div>
    </div>
  )
}
