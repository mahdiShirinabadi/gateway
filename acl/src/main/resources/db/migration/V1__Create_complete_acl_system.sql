-- ==============================================
-- ACL Database Schema - Complete System Setup
-- This migration creates the complete ACL system with group-based architecture
-- ==============================================

-- ==============================================
-- 1. CREATE CORE TABLES
-- ==============================================

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create users table (with email and full_name from start)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    full_name VARCHAR(255),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    base_url VARCHAR(500),
    version VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create unified api_permissions table
CREATE TABLE api_permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,                    -- permission name (SERVICE1_HELLO_ACCESS)
    project_id BIGINT NOT NULL,                   -- foreign key to projects table
    api_path VARCHAR(255) NOT NULL,               -- API path (/hello)
    http_method VARCHAR(10) NOT NULL,             -- HTTP method (GET, POST)
    description VARCHAR(500) NOT NULL,            -- description
    persian_name VARCHAR(255) NOT NULL,          -- persian name
    is_critical BOOLEAN NOT NULL DEFAULT FALSE,   -- is critical permission
    is_public BOOLEAN NOT NULL DEFAULT FALSE,     -- is public API
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- Create role_permissions table (many-to-many relationship)
CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES api_permissions(id),
    UNIQUE(role_id, permission_id)  -- Prevent duplicate role-permission assignments
);

-- ==============================================
-- 2. CREATE GROUP-BASED ARCHITECTURE TABLES
-- ==============================================

-- Create groups table
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create user_groups table (User-Group relationships)
CREATE TABLE user_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES groups(id),
    UNIQUE(user_id, group_id)
);

-- Create group_roles table (Group-Role relationships)
CREATE TABLE group_roles (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    UNIQUE(group_id, role_id)
);

-- ==============================================
-- 3. CREATE INDEXES FOR PERFORMANCE
-- ==============================================

-- Core table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_api_permissions_name ON api_permissions(name);
CREATE INDEX idx_api_permissions_project_id ON api_permissions(project_id);
CREATE INDEX idx_api_permissions_api_path ON api_permissions(api_path);
CREATE INDEX idx_api_permissions_http_method ON api_permissions(http_method);
CREATE INDEX idx_api_permissions_is_public ON api_permissions(is_public);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Group-based architecture indexes
CREATE INDEX idx_groups_name ON groups(name);
CREATE INDEX idx_groups_is_active ON groups(is_active);
CREATE INDEX idx_user_groups_user_id ON user_groups(user_id);
CREATE INDEX idx_user_groups_group_id ON user_groups(group_id);
CREATE INDEX idx_group_roles_group_id ON group_roles(group_id);
CREATE INDEX idx_group_roles_role_id ON group_roles(role_id);

-- ==============================================
-- 4. INSERT INITIAL DATA
-- ==============================================

-- Create default roles
INSERT INTO roles (name, description, create_by) VALUES
('ADMIN', 'Administrator role with full access', 'system'),
('USER', 'Regular user role with limited access', 'system'),
('GUEST', 'Guest role with minimal access', 'system');

-- Create default users with email and full name
INSERT INTO users (username, email, full_name, create_by) VALUES
('admin', 'admin@example.com', 'Administrator User', 'system'),
('user', 'user@example.com', 'Regular User', 'system'),
('guest', 'guest@example.com', 'Guest User', 'system');

-- Create default projects
INSERT INTO projects (name, description, base_url, version, create_by) VALUES
('service1', 'Service1 API', 'http://localhost:8082', '1.0.0', 'system'),
('gateway', 'API Gateway', 'http://localhost:8080', '1.0.0', 'system'),
('sso', 'Single Sign-On Service', 'http://localhost:8081', '1.0.0', 'system'),
('acl', 'Access Control List Service', 'http://localhost:8083', '1.0.0', 'system');

-- Create default groups
INSERT INTO groups (name, description, is_active, create_by) VALUES
('admin_group', 'Administrator group with full access', TRUE, 'system'),
('user_group', 'Regular user group with limited access', TRUE, 'system'),
('guest_group', 'Guest group with minimal access', TRUE, 'system'),
('manager_group', 'Manager group with management access', TRUE, 'system');

-- Create default API permissions for service1
INSERT INTO api_permissions (name, project_id, api_path, http_method, description, persian_name, is_critical, is_public, create_by) VALUES
('SERVICE1_HELLO_ACCESS', (SELECT id FROM projects WHERE name = 'service1'), '/hello', 'GET', 'Access to hello endpoint', 'دسترسی به صفحه سلام', false, false, 'system'),
('SERVICE1_USERS_ACCESS', (SELECT id FROM projects WHERE name = 'service1'), '/users', 'GET', 'Access to users endpoint', 'دسترسی به لیست کاربران', false, false, 'system'),
('SERVICE1_USERS_CREATE', (SELECT id FROM projects WHERE name = 'service1'), '/users', 'POST', 'Create user endpoint', 'ایجاد کاربر جدید', true, false, 'system'),
('SERVICE1_ADMIN_ACCESS', (SELECT id FROM projects WHERE name = 'service1'), '/admin', 'GET', 'Admin panel access', 'دسترسی به پنل مدیریت', true, false, 'system');

-- Create default API permissions for gateway
INSERT INTO api_permissions (name, project_id, api_path, http_method, description, persian_name, is_critical, is_public, create_by) VALUES
('GATEWAY_HEALTH_ACCESS', (SELECT id FROM projects WHERE name = 'gateway'), '/health', 'GET', 'Gateway health check', 'بررسی وضعیت Gateway', false, true, 'system'),
('GATEWAY_ROUTES_ACCESS', (SELECT id FROM projects WHERE name = 'gateway'), '/routes', 'GET', 'Gateway routes info', 'اطلاعات مسیرهای Gateway', false, false, 'system');

-- Create default API permissions for SSO
INSERT INTO api_permissions (name, project_id, api_path, http_method, description, persian_name, is_critical, is_public, create_by) VALUES
('SSO_LOGIN_ACCESS', (SELECT id FROM projects WHERE name = 'sso'), '/login', 'POST', 'SSO login endpoint', 'ورود به سیستم', false, true, 'system'),
('SSO_LOGOUT_ACCESS', (SELECT id FROM projects WHERE name = 'sso'), '/logout', 'POST', 'SSO logout endpoint', 'خروج از سیستم', false, true, 'system'),
('SSO_VALIDATE_ACCESS', (SELECT id FROM projects WHERE name = 'sso'), '/validate', 'POST', 'Token validation', 'اعتبارسنجی توکن', false, false, 'system');

-- Create default API permissions for ACL
INSERT INTO api_permissions (name, project_id, api_path, http_method, description, persian_name, is_critical, is_public, create_by) VALUES
('ACL_CHECK_ACCESS', (SELECT id FROM projects WHERE name = 'acl'), '/check', 'POST', 'ACL permission check', 'بررسی مجوز ACL', false, false, 'system'),
('ACL_REGISTER_ACCESS', (SELECT id FROM projects WHERE name = 'acl'), '/register', 'POST', 'ACL registration', 'ثبت نام ACL', false, false, 'system'),
('ACL_HEALTH_ACCESS', (SELECT id FROM projects WHERE name = 'acl'), '/health', 'GET', 'ACL health check', 'بررسی وضعیت ACL', false, true, 'system');

-- ==============================================
-- 5. ASSIGN USERS TO GROUPS
-- ==============================================

INSERT INTO user_groups (user_id, group_id, create_by) VALUES
-- Admin user to admin_group and user_group
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM groups WHERE name = 'admin_group'), 'system'),
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM groups WHERE name = 'user_group'), 'system'),
-- Regular user to user_group
((SELECT id FROM users WHERE username = 'user'), (SELECT id FROM groups WHERE name = 'user_group'), 'system'),
-- Guest user to guest_group
((SELECT id FROM users WHERE username = 'guest'), (SELECT id FROM groups WHERE name = 'guest_group'), 'system');

-- ==============================================
-- 6. ASSIGN ROLES TO GROUPS
-- ==============================================

INSERT INTO group_roles (group_id, role_id, create_by) VALUES
-- admin_group gets ADMIN and USER roles
((SELECT id FROM groups WHERE name = 'admin_group'), (SELECT id FROM roles WHERE name = 'ADMIN'), 'system'),
((SELECT id FROM groups WHERE name = 'admin_group'), (SELECT id FROM roles WHERE name = 'USER'), 'system'),
-- user_group gets USER role
((SELECT id FROM groups WHERE name = 'user_group'), (SELECT id FROM roles WHERE name = 'USER'), 'system'),
-- guest_group gets GUEST role
((SELECT id FROM groups WHERE name = 'guest_group'), (SELECT id FROM roles WHERE name = 'GUEST'), 'system'),
-- manager_group gets USER role (can be extended with manager-specific roles)
((SELECT id FROM groups WHERE name = 'manager_group'), (SELECT id FROM roles WHERE name = 'USER'), 'system');

-- ==============================================
-- 7. ASSIGN PERMISSIONS TO ROLES
-- ==============================================

-- ADMIN role gets all permissions
INSERT INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 'system'
FROM roles r, api_permissions p
WHERE r.name = 'ADMIN';

-- USER role gets basic permissions (no admin access)
INSERT INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 'system'
FROM roles r, api_permissions p
WHERE r.name = 'USER'
AND p.name NOT LIKE '%ADMIN%'
AND p.name NOT LIKE '%REGISTER%';

-- GUEST role gets only public permissions
INSERT INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 'system'
FROM roles r, api_permissions p
WHERE r.name = 'GUEST'
AND p.is_public = true;

-- ==============================================
-- 8. ADD COMMENTS FOR DOCUMENTATION
-- ==============================================

COMMENT ON TABLE roles IS 'User roles in the system';
COMMENT ON TABLE users IS 'System users with email and full name';
COMMENT ON TABLE projects IS 'Registered projects/services';
COMMENT ON TABLE api_permissions IS 'Unified table for API permissions and endpoints';
COMMENT ON TABLE role_permissions IS 'Many-to-many relationship between roles and permissions';
COMMENT ON TABLE groups IS 'Groups for organizing users and roles - replaces direct user-role assignments';
COMMENT ON TABLE user_groups IS 'User-Group assignments - users belong to groups';
COMMENT ON TABLE group_roles IS 'Group-Role assignments - groups have roles';

COMMENT ON COLUMN api_permissions.name IS 'Permission name (e.g., SERVICE1_HELLO_ACCESS)';
COMMENT ON COLUMN api_permissions.project_id IS 'Foreign key to projects table';
COMMENT ON COLUMN api_permissions.api_path IS 'API endpoint path (e.g., /hello)';
COMMENT ON COLUMN api_permissions.http_method IS 'HTTP method (GET, POST, PUT, DELETE)';
COMMENT ON COLUMN api_permissions.is_critical IS 'Whether this permission is critical for security';
COMMENT ON COLUMN api_permissions.is_public IS 'Whether this API is publicly accessible';

COMMENT ON COLUMN groups.name IS 'Group name (e.g., admin_group, manager_group)';
COMMENT ON COLUMN groups.description IS 'Group description';
COMMENT ON COLUMN groups.is_active IS 'Whether the group is active';
COMMENT ON COLUMN user_groups.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN user_groups.group_id IS 'Foreign key to groups table';
COMMENT ON COLUMN group_roles.group_id IS 'Foreign key to groups table';
COMMENT ON COLUMN group_roles.role_id IS 'Foreign key to roles table';

-- ==============================================
-- 9. VERIFICATION QUERIES (for testing)
-- ==============================================

-- This query shows the complete user permission flow through groups:
/*
SELECT DISTINCT 
    u.username,
    u.email,
    u.full_name,
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
*/

-- ==============================================
-- 10. SYSTEM ARCHITECTURE SUMMARY
-- ==============================================

/*
FINAL ARCHITECTURE:
User → UserGroup → Group → GroupRole → Role → RolePermission → ApiPermission

BENEFITS:
1. No direct user-role assignments (user_roles table removed)
2. Group-based organization for better scalability
3. Clean separation of concerns
4. Easy to manage large user bases
5. Hierarchical permission structure

TABLES CREATED:
- roles (core roles)
- users (with email and full_name)
- projects (registered services)
- api_permissions (unified permissions)
- role_permissions (role-permission mapping)
- groups (organizational units)
- user_groups (user-group assignments)
- group_roles (group-role assignments)

NO user_roles TABLE - Completely removed!
*/
