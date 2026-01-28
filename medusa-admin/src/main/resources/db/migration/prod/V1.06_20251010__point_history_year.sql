-- 为会员积分历史记录表添加年份字段
-- 用于支持按年份区分积分，以便每年1月1日更新会员等级

ALTER TABLE `mall_member_point_history`
ADD COLUMN `year` INT NULL COMMENT '年份' AFTER `platform`;

-- 为现有记录设置默认年份（根据create_time提取年份）
UPDATE `mall_member_point_history`
SET `year` = YEAR(create_time)
WHERE `year` IS NULL;

