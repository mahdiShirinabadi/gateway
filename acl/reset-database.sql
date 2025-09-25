-- ==============================================
-- Database Reset Script for ACL System
-- This script clears the database and resets Flyway
-- ==============================================

-- 1. Drop all tables in correct order (respecting foreign keys)
DROP TABLE IF EXISTS group_roles CASCADE;
DROP TABLE IF EXISTS user_groups CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS api_permissions CASCADE;
DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS projects CASCADE;

-- 2. Drop Flyway schema history table
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- 3. Drop any sequences that might exist
DROP SEQUENCE IF EXISTS roles_id_seq CASCADE;
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS projects_id_seq CASCADE;
DROP SEQUENCE IF EXISTS api_permissions_id_seq CASCADE;
DROP SEQUENCE IF EXISTS role_permissions_id_seq CASCADE;
DROP SEQUENCE IF EXISTS groups_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_groups_id_seq CASCADE;
DROP SEQUENCE IF EXISTS group_roles_id_seq CASCADE;

-- 4. Drop any indexes that might exist
DROP INDEX IF EXISTS idx_users_username CASCADE;
DROP INDEX IF EXISTS idx_roles_name CASCADE;
DROP INDEX IF EXISTS idx_projects_name CASCADE;
DROP INDEX IF EXISTS idx_api_permissions_name CASCADE;
DROP INDEX IF EXISTS idx_api_permissions_project_id CASCADE;
DROP INDEX IF EXISTS idx_api_permissions_api_path CASCADE;
DROP INDEX IF EXISTS idx_api_permissions_http_method CASCADE;
DROP INDEX IF EXISTS idx_api_permissions_is_public CASCADE;
DROP INDEX IF EXISTS idx_role_permissions_role_id CASCADE;
DROP INDEX IF EXISTS idx_role_permissions_permission_id CASCADE;
DROP INDEX IF EXISTS idx_groups_name CASCADE;
DROP INDEX IF EXISTS idx_groups_is_active CASCADE;
DROP INDEX IF EXISTS idx_user_groups_user_id CASCADE;
DROP INDEX IF EXISTS idx_user_groups_group_id CASCADE;
DROP INDEX IF EXISTS idx_group_roles_group_id CASCADE;
DROP INDEX IF EXISTS idx_group_roles_role_id CASCADE;

-- 5. Verify cleanup
SELECT 'Database cleared successfully' as status;
