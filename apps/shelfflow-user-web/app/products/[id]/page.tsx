import { ProductDetailPanel } from "@/components/catalog/product-detail-panel"

export default async function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  return <ProductDetailPanel productId={id} />
}
