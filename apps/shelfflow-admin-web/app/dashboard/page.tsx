import { redirect } from "next/navigation"

import { DASHBOARD_ROUTES } from "@/lib/constants"

export default function DashboardIndexPage() {
  redirect(DASHBOARD_ROUTES.overview)
}
