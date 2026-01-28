-- Increase shipping_number column length from 50 to 100 in mall_order_shipping table
ALTER TABLE mall_order_shipping 
MODIFY COLUMN shipping_number VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流单号';

