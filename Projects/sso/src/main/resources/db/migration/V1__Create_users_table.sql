-- Create users table for SSO
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(255) NOT NULL DEFAULT 'system',
    update_time TIMESTAMP,
    update_by VARCHAR(255),
    deleted_time TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Create index for username
CREATE INDEX idx_users_username ON users(username);

-- Insert initial test users
INSERT INTO users (username, password, create_by) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'system'), -- password123
('admin', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfgqwAGmQvKqVfHqJqHqHqHqHqHqHqHq', 'system'), -- admin123
('superadmin', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfgqwAGmQvKqVfHqJqHqHqHqHqHqHqHq', 'system'); -- admin123 