-- 更新 mall_member_benefit 表的 des 字段
-- 根据 member_benefit-0909-all-dump.sql 中的最新数据更新描述信息

USE medusa;

-- 显示更新前的数据
SELECT 'BEFORE UPDATE - Current member benefit descriptions:' AS info;
SELECT `level_id`, `level_name`, `point`, `des` FROM `mall_member_benefit` ORDER BY `level_id`;

-- 更新 Bronze 级别描述
UPDATE `mall_member_benefit`
SET `des` = 'Exclusive discounts\n Access to community support',
    `update_time` = NOW()
WHERE `level_name` = 'Bronze';

-- 更新 Silver 级别描述
UPDATE `mall_member_benefit`
SET `des` = '$10 Discount\n Live chat support',
    `update_time` = NOW()
WHERE `level_name` = 'Silver';

-- 更新 Gold 级别描述
UPDATE `mall_member_benefit`
SET `des` = '$20 Discount \n Live chat support',
    `update_time` = NOW()
WHERE `level_name` = 'Gold';

-- 更新 Platinum 级别描述
UPDATE `mall_member_benefit`
SET `des` = '3% Discount \n Free shipping\n Dedicated Account Manager',
    `update_time` = NOW()
WHERE `level_name` = 'Platinum';

-- 更新 Diamond 级别描述
UPDATE `mall_member_benefit`
SET `des` = '5% Discount \n Free shipping\n Dedicated Account Manager',
    `update_time` = NOW()
WHERE `level_name` = 'Diamond';

-- 显示更新后的数据
SELECT 'AFTER UPDATE - Updated member benefit descriptions:' AS info;
SELECT `level_id`, `level_name`, `point`, `des`, `update_time` FROM `mall_member_benefit` ORDER BY `level_id`;

-- 验证更新数量
SELECT 'Update Summary:' AS info;
SELECT 
    COUNT(*) AS total_records_updated,
    'All member benefit descriptions updated successfully!' AS message
FROM `mall_member_benefit`
WHERE `update_time` >= DATE_SUB(NOW(), INTERVAL 1 MINUTE);

-- 详细显示每个等级的权益描述（格式化显示）
SELECT 'Detailed Member Benefit Descriptions:' AS info;
SELECT 
    `level_name` AS 'Level',
    `point` AS 'Points Required',
    REPLACE(`des`, '\n', ' | ') AS 'Benefits Description'
FROM `mall_member_benefit` 
ORDER BY `point` ASC;

-- 输出完成信息
SELECT 'Member benefit descriptions updated successfully!' AS message;
SELECT 'Source: member_benefit-0909-all-dump.sql' AS source_file;
SELECT NOW() AS update_timestamp; 

-- 完整更新 mall_shipping_method 表的 code 和 name 字段
-- 根据新的命名规则更新配送方式代码缩写和名称

USE medusa;

-- 先显示当前的配送方式数据
SELECT 'BEFORE UPDATE - Current shipping methods:' AS info;
SELECT `code`, `name`, `fee` FROM `mall_shipping_method` ORDER BY `fee` ASC;

-- 更新 RPWG -> RSP (Regular Stealth Post)
UPDATE `mall_shipping_method` 
SET `code` = 'RSP', 
    `name` = 'Regular Stealth Post',
    `update_time` = NOW() 
WHERE `code` = 'RPWG';

-- 更新 EPWG -> ESP (Express Stealth Post)
UPDATE `mall_shipping_method` 
SET `code` = 'ESP', 
    `name` = 'Express Stealth Post',
    `update_time` = NOW() 
WHERE `code` = 'EPWG';

-- 更新 FP -> SSP (Super Stealth Post)
UPDATE `mall_shipping_method` 
SET `code` = 'SSP', 
    `name` = 'Super Stealth Post (Recommended)',
    `update_time` = NOW() 
WHERE `code` = 'FP';

-- 同时确保其他配送方式名称也是最新的
UPDATE `mall_shipping_method` 
SET `name` = 'Regular Post', 
    `update_time` = NOW() 
WHERE `code` = 'RP';

UPDATE `mall_shipping_method` 
SET `name` = 'Express Post', 
    `update_time` = NOW() 
WHERE `code` = 'EP';

-- 验证更新结果
SELECT 'AFTER UPDATE - Updated shipping methods:' AS info;
SELECT `code`, `name`, `fee`, `status`, `update_time` 
FROM `mall_shipping_method` 
ORDER BY `fee` ASC;

-- 输出完成信息
SELECT 'Shipping method codes and names updated successfully!' AS message;
SELECT 'Code mapping: RPWG->RSP, EPWG->ESP, FP->SSP' AS code_mapping;
SELECT 'All names updated to remove price display' AS name_update; 
