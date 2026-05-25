import type { Metadata } from "next"
import { Inter } from "next/font/google"

import "./globals.css"

const inter = Inter({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-inter",
  weight: ["400", "600", "700"],
})

export const metadata: Metadata = {
  title: {
    default: "ShelfFlow 管理端",
    template: "%s | ShelfFlow 管理端"
  },
  description: "ShelfFlow 管理端，负责临期库存商品的批次、定价与履约运营。",
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="zh-CN" className={`${inter.variable} antialiased`}>
      <body className="font-sans">{children}</body>
    </html>
  )
}
