import { redirect } from "next/navigation"

import { DASHBOARD_DEFAULT_ROUTE, DASHBOARD_ROUTES } from "@/lib/constants"
import { readSession } from "@/lib/server/session"

export default async function HomePage() {
  const session = await readSession()
  redirect(session ? DASHBOARD_DEFAULT_ROUTE : DASHBOARD_ROUTES.login)
}
