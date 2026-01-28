-- Annual Member Level Update Scheduled Task Configuration
-- Executes on January 1st every year, determines current year level based on previous year's points

-- Insert annual member level update scheduled task
-- status: '0' = enabled, '1' = paused
INSERT INTO sys_job (
    job_id, 
    job_name, 
    job_group, 
    invoke_target, 
    cron_expression, 
    misfire_policy, 
    concurrent, 
    status, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
) VALUES (
    1001,
    'Annual Member Level Update',
    'DEFAULT',
    'annualMemberLevelTask.executeAnnualLevelUpdate',
    '0 0 0 1 1 ?',
    '3',
    '1',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    'Executes on January 1st every year, determines current year level based on previous year points'
);

-- Insert test task (manual trigger, disabled by default)
-- status: '0' = enabled, '1' = paused
INSERT INTO sys_job (
    job_id, 
    job_name, 
    job_group, 
    invoke_target, 
    cron_expression, 
    misfire_policy, 
    concurrent, 
    status, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
) VALUES (
    1002,
    'Annual Member Level Update Test',
    'DEFAULT',
    'annualMemberLevelTask.testAnnualLevelUpdate',
    '0 0 12 * * ?',
    '3',
    '1',
    '1',
    'admin',
    NOW(),
    'admin',
    NOW(),
    'Test task, disabled by default, execute manually in Admin Panel when needed'
);
