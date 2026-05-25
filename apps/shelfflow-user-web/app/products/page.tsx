import { Suspense } from "react"

import { ProductCatalogPanel } from "@/components/catalog/product-catalog-panel"

export default function ProductsPage() {
  return (
    <Suspense fallback={<div className="rounded-2xl border border-white/70 bg-white/90 p-8 text-sm text-slate-500 shadow-sm">加载商品目录中...</div>}>
      <ProductCatalogPanel />
    </Suspense>
  )
}
