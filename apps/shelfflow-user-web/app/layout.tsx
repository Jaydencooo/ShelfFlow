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
        <div className="min-h-screen bg-[radial-gradient(circle_at_top,rgba(16,185,129,0.12),transparent_32%),linear-gradient(180deg,#fbfdfb_0%,#f6f7f8_100%)]">
          <AppHeader />
          <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">{children}</main>
        </div>
      </body>
    </html>
  )
}
