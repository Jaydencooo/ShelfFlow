import { OrderDetailPanel } from "@/components/order/order-detail-panel"

export default async function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  return <OrderDetailPanel orderId={id} />
}
