import { redirect } from "next/navigation"

import { APP_ROUTES } from "@/lib/constants"

export default function IndexPage() {
  redirect(APP_ROUTES.products)
}
