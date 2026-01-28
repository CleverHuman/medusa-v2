-- Add address_line3 column to mall_order_shipping table
ALTER TABLE mall_order_shipping 
ADD COLUMN address_line3 VARCHAR(255) DEFAULT NULL COMMENT 'Address Line 3' AFTER address_line2;

