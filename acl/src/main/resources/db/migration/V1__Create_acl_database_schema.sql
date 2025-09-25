-- ACL Database Schema - Complete Initial Setup
-- This migration creates all necessary tables for the ACL system

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

-- Create users table (without role_id - using many-to-many)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create user_roles table (many-to-many relationship)
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    UNIQUE(user_id, role_id)  -- Prevent duplicate user-role assignments
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

-- Create unified api_permissions table (combines permissions and project_apis)
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

-- Create indexes for better performance
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
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_is_primary ON user_roles(is_primary);

-- Insert initial data
-- Create default roles
INSERT INTO roles (name, description, create_by) VALUES 
('ADMIN', 'Administrator role with full access', 'system'),
('USER', 'Regular user role with limited access', 'system'),
('GUEST', 'Guest role with minimal access', 'system');

-- Create default users
INSERT INTO users (username, create_by) VALUES 
('admin', 'system'),
('user', 'system'),
('guest', 'system');

-- Assign roles to users (multiple roles support)
INSERT INTO user_roles (user_id, role_id, is_primary, create_by) VALUES 
-- Admin user gets ADMIN role (primary)
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ADMIN'), true, 'system'),
-- User gets USER role (primary)
((SELECT id FROM users WHERE username = 'user'), (SELECT id FROM roles WHERE name = 'USER'), true, 'system'),
-- Guest gets GUEST role (primary)
((SELECT id FROM users WHERE username = 'guest'), (SELECT id FROM roles WHERE name = 'GUEST'), true, 'system'),
-- Admin also gets USER role (secondary)
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'USER'), false, 'system');

-- Create default projects
INSERT INTO projects (name, description, base_url, version, create_by) VALUES 
('service1', 'Service1 API', 'http://localhost:8082', '1.0.0', 'system'),
('gateway', 'API Gateway', 'http://localhost:8080', '1.0.0', 'system'),
('sso', 'Single Sign-On Service', 'http://localhost:8081', '1.0.0', 'system'),
('acl', 'Access Control List Service', 'http://localhost:8083', '1.0.0', 'system');

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

-- Assign permissions to roles
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

-- Add comments for documentation
COMMENT ON TABLE roles IS 'User roles in the system';
COMMENT ON TABLE users IS 'System users with assigned roles';
COMMENT ON TABLE projects IS 'Registered projects/services';
COMMENT ON TABLE api_permissions IS 'Unified table for API permissions and endpoints';
COMMENT ON TABLE role_permissions IS 'Many-to-many relationship between roles and permissions';

COMMENT ON COLUMN api_permissions.name IS 'Permission name (e.g., SERVICE1_HELLO_ACCESS)';
COMMENT ON COLUMN api_permissions.project_id IS 'Foreign key to projects table';
COMMENT ON COLUMN api_permissions.api_path IS 'API endpoint path (e.g., /hello)';
COMMENT ON COLUMN api_permissions.http_method IS 'HTTP method (GET, POST, PUT, DELETE)';
COMMENT ON COLUMN api_permissions.is_critical IS 'Whether this permission is critical for security';
COMMENT ON COLUMN api_permissions.is_public IS 'Whether this API is publicly accessible';
