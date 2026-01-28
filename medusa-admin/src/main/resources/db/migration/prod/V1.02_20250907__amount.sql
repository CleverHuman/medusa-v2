-- 修改Product Amount字段支持小数
-- 将mall_product2表的model字段从INT改为DECIMAL(10,2)

USE medusa;

-- 修改model字段为DECIMAL类型，支持小数
ALTER TABLE `mall_product2` 
MODIFY COLUMN `model` DECIMAL(10,2) NOT NULL COMMENT 'Product amount specification (supports decimal, e.g., 0.25, 0.5, 100.00)';

-- 验证修改结果
DESCRIBE `mall_product2`;