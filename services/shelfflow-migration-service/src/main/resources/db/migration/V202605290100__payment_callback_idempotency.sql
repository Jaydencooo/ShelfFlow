ALTER TABLE `user_order_payment`
  ADD COLUMN `external_trade_no` varchar(128) DEFAULT NULL COMMENT '外部支付平台交易号' AFTER `idempotency_key`,
  ADD COLUMN `callback_event_id` varchar(128) DEFAULT NULL COMMENT '支付平台回调事件 ID' AFTER `external_trade_no`,
  ADD COLUMN `callback_time` datetime DEFAULT NULL COMMENT '支付回调处理时间' AFTER `paid_time`;

ALTER TABLE `user_order_payment`
  ADD UNIQUE KEY `uk_user_order_payment_external_trade` (`external_trade_no`),
  ADD UNIQUE KEY `uk_user_order_payment_callback_event` (`callback_event_id`);
