-- 为 mall_order 表添加 customer_comment 字段
-- 用于存储客户在下单时填写的订单说明/备注

ALTER TABLE mall_order 
ADD COLUMN customer_comment VARCHAR(1000) NULL COMMENT '客户备注（顾客在下单时填写的订单说明）' 
AFTER remark;

