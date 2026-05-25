import { redirect } from "next/navigation"

import { DASHBOARD_ROUTES } from "@/lib/constants"

export default function OrdersPage() {
  redirect(DASHBOARD_ROUTES.orderMonitor)
}
