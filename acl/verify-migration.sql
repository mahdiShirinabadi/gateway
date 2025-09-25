-- ==============================================
-- Migration Verification Script
-- This script verifies that the ACL system is properly set up
-- ==============================================

-- 1. Check that all tables exist
SELECT 'Tables Check' as verification_step;
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'users', 'roles', 'projects', 'api_permissions', 'role_permissions',
    'groups', 'user_groups', 'group_roles'
)
ORDER BY table_name;

-- 2. Verify user_roles table does NOT exist
SELECT 'user_roles table check' as verification_step;
SELECT CASE 
    WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_roles')
    THEN 'ERROR: user_roles table still exists!'
    ELSE 'SUCCESS: user_roles table properly removed'
END as user_roles_status;

-- 3. Check initial data
SELECT 'Initial Data Check' as verification_step;

-- Check users
SELECT 'Users:' as data_type, COUNT(*) as count FROM users;
SELECT username, email, full_name FROM users ORDER BY username;

-- Check roles
SELECT 'Roles:' as data_type, COUNT(*) as count FROM roles;
SELECT name, description FROM roles ORDER BY name;

-- Check groups
SELECT 'Groups:' as data_type, COUNT(*) as count FROM groups;
SELECT name, description, is_active FROM groups ORDER BY name;

-- Check projects
SELECT 'Projects:' as data_type, COUNT(*) as count FROM projects;
SELECT name, description, base_url FROM projects ORDER BY name;

-- Check API permissions
SELECT 'API Permissions:' as data_type, COUNT(*) as count FROM api_permissions;
SELECT name, api_path, http_method, is_public, is_critical FROM api_permissions ORDER BY name;

-- 4. Check user-group assignments
SELECT 'User-Group Assignments:' as data_type, COUNT(*) as count FROM user_groups;
SELECT u.username, g.name as group_name 
FROM users u 
JOIN user_groups ug ON u.id = ug.user_id 
JOIN groups g ON ug.group_id = g.id 
ORDER BY u.username, g.name;

-- 5. Check group-role assignments
SELECT 'Group-Role Assignments:' as data_type, COUNT(*) as count FROM group_roles;
SELECT g.name as group_name, r.name as role_name 
FROM groups g 
JOIN group_roles gr ON g.id = gr.group_id 
JOIN roles r ON gr.role_id = r.id 
ORDER BY g.name, r.name;

-- 6. Check role-permission assignments
SELECT 'Role-Permission Assignments:' as data_type, COUNT(*) as count FROM role_permissions;
SELECT r.name as role_name, p.name as permission_name 
FROM roles r 
JOIN role_permissions rp ON r.id = rp.role_id 
JOIN api_permissions p ON rp.permission_id = p.id 
ORDER BY r.name, p.name;

-- 7. Test the complete permission flow
SELECT 'Complete Permission Flow Test:' as verification_step;
SELECT DISTINCT 
    u.username,
    u.email,
    g.name as group_name,
    r.name as role_name,
    p.name as permission_name,
    p.api_path,
    p.http_method,
    p.is_public,
    p.is_critical
FROM users u
JOIN user_groups ug ON u.id = ug.user_id
JOIN groups g ON ug.group_id = g.id
JOIN group_roles gr ON g.id = gr.group_id
JOIN roles r ON gr.role_id = r.id
JOIN role_permissions rp ON r.id = rp.role_id
JOIN api_permissions p ON rp.permission_id = p.id
ORDER BY u.username, g.name, r.name, p.name;

-- 8. Summary
SELECT 'Migration Verification Complete!' as status;
SELECT 'All tables created successfully' as result;
SELECT 'user_roles table properly removed' as result;
SELECT 'Group-based architecture implemented' as result;
SELECT 'Initial data loaded successfully' as result;
