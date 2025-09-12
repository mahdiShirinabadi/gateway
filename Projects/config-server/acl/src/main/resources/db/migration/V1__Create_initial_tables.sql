-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create permissions table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    is_critical BOOLEAN NOT NULL DEFAULT FALSE,
    persian_name VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create role_permissions table
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
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create project_apis table
CREATE TABLE project_apis (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    api_path VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    permission_name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- Create indexes for better performance
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_project_name ON permissions(project_name);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_project_apis_project_id ON project_apis(project_id);
CREATE INDEX idx_project_apis_permission_name ON project_apis(permission_name); 