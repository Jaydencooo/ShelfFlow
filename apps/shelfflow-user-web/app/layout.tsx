import type { Metadata } from "next"

import "./globals.css"

import { AppHeader } from "@/components/common/app-header"

export const metadata: Metadata = {
  title: "ShelfFlow 用户端",
  description: "ShelfFlow 临期商品用户端"
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <div className="min-h-screen bg-[#F7FAF6] bg-[radial-gradient(circle_at_18%_0%,rgba(16,166,106,0.10),transparent_30%),radial-gradient(circle_at_88%_8%,rgba(221,236,211,0.65),transparent_25%),linear-gradient(180deg,#FAFBF8_0%,#F7FAF6_54%,#FFFFFF_100%)] text-slate-950">
          <AppHeader />
          <main className="mx-auto max-w-[1280px] px-4 py-8 sm:px-6 lg:px-8">{children}</main>
        </div>
      </body>
    </html>
  )
}
