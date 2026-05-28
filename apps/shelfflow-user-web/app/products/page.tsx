import { redirect } from "next/navigation"

import { APP_ROUTES } from "@/lib/constants"

export default function ProductsPage() {
  redirect(APP_ROUTES.home)
}
