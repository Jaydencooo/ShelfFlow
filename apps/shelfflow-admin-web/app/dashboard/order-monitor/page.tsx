import { OrderFulfillmentPanel } from "@/components/dashboard/order-fulfillment-panel"

export default function OrderMonitorPage() {
  return (
    <OrderFulfillmentPanel
      description="监控用户订单、支付状态、自提信息和订单明细，数据来自当前 Java 订单服务。"
      mode="monitor"
      title="订单管理"
    />
  )
}
