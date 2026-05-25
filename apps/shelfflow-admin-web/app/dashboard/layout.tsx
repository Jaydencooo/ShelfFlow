import type { ReactNode } from "react"
import { redirect } from "next/navigation"

import { DashboardShell } from "@/components/dashboard/dashboard-shell"
import { DASHBOARD_ROUTES } from "@/lib/constants"
import { readSession } from "@/lib/server/session"

export default async function DashboardLayout({ children }: { children: ReactNode }) {
  const session = await readSession()

  if (!session) {
    redirect(DASHBOARD_ROUTES.login)
  }

  return (
    <DashboardShell
      name={session.displayName}
      userName={session.username}
    >
      {children}
    </DashboardShell>
  )
}
