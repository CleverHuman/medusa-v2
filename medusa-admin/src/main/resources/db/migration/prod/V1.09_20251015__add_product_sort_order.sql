-- Add sort_order column to mall_product table
-- This allows admin to control the display order of products on the shopfront

ALTER TABLE mall_product ADD COLUMN sort_order INT DEFAULT 0 COMMENT 'Sort order for display (smaller number = higher priority)';

-- Set initial sort_order values based on current ID order
-- This ensures existing products maintain their current display order
UPDATE mall_product SET sort_order = id WHERE sort_order IS NULL OR sort_order = 0;

-- Create index for better performance when sorting
CREATE INDEX idx_mall_product_sort_order ON mall_product(sort_order);

-- Add comment to the table
ALTER TABLE mall_product COMMENT = 'Product master table with sort order support';
