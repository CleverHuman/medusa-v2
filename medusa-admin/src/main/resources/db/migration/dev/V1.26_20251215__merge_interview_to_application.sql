-- =============================================
-- Merge Interview Schedule Menu into Vendor Application
-- =============================================
-- This migration hides the standalone Interview Schedule menu
-- as it has been integrated into the Vendor Application page

-- Find Vendor Management Menu
SELECT @vendor_menu_id := menu_id 
FROM sys_menu 
WHERE menu_name = 'Vendor Management' 
  AND menu_type = 'M' 
LIMIT 1;

-- Hide the standalone Interview Schedule menu
UPDATE sys_menu 
SET visible = '1', 
    status = '1'
WHERE menu_name = 'Interview Schedule' 
  AND parent_id = @vendor_menu_id
  AND path = 'interview'
  AND component = 'mall/vendor/interview/index';

-- Display result
SELECT CONCAT('✅ Interview Schedule menu hidden (integrated into Vendor Application)') as Status;
SELECT 'Interview Schedule functionality is now available in Vendor Application > Interview Schedule tab' as Note;
