-- Insert initial roles
INSERT INTO roles (name, create_by) VALUES 
('USER', 'system'),
('ADMIN', 'system'),
('SUPER_ADMIN', 'system');

-- Insert initial permissions
INSERT INTO permissions (name, project_name, is_critical, persian_name, create_by) VALUES 
('SERVICE1_HELLO_ACCESS', 'service1', false, 'دسترسی به صفحه سلام', 'system'),
('SERVICE1_ADMIN_ACCESS', 'service1', true, 'دسترسی ادمین به سرویس 1', 'system'),
('SERVICE1_ALL_ACCESS', 'service1', true, 'دسترسی کامل به سرویس 1', 'system');

-- Insert role-permission assignments
INSERT INTO role_permissions (role_id, permission_id, create_by) 
SELECT r.id, p.id, 'system'
FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name = 'SERVICE1_HELLO_ACCESS';

INSERT INTO role_permissions (role_id, permission_id, create_by) 
SELECT r.id, p.id, 'system'
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name = 'SERVICE1_HELLO_ACCESS';

INSERT INTO role_permissions (role_id, permission_id, create_by) 
SELECT r.id, p.id, 'system'
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name = 'SERVICE1_ADMIN_ACCESS';

INSERT INTO role_permissions (role_id, permission_id, create_by) 
SELECT r.id, p.id, 'system'
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.name = 'SERVICE1_ALL_ACCESS';

-- Insert initial users
INSERT INTO users (username, role_id, create_by) 
SELECT 'testuser', r.id, 'system'
FROM roles r WHERE r.name = 'USER';

INSERT INTO users (username, role_id, create_by) 
SELECT 'admin', r.id, 'system'
FROM roles r WHERE r.name = 'ADMIN';

INSERT INTO users (username, role_id, create_by) 
SELECT 'superadmin', r.id, 'system'
FROM roles r WHERE r.name = 'SUPER_ADMIN'; 