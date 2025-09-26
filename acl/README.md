# ACL Service - Role-Based Access Control with Audit

This service provides role-based access control (RBAC) functionality with comprehensive audit trails for the microservices architecture.

## Database Schema

### 1. Permissions Table
```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    is_critical BOOLEAN NOT NULL,
    persian_name VARCHAR(255) NOT NULL,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255)
);
```

### 2. Roles Table
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255)
);
```

### 3. Role_Permissions Table (Many-to-Many)
```sql
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
```

### 4. Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### 5. Projects Table
```sql
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255)
);
```

### 6. Project_APIs Table
```sql
CREATE TABLE project_apis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    api_path VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    permission_name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    update_time DATETIME,
    update_by VARCHAR(255),
    deleted_time DATETIME,
    deleted_by VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);
```

## API Endpoints

### Permission Management

#### Check Permission
```bash
POST /api/acl/check
Content-Type: application/json

{
    "username": "testuser",
    "permissionName": "SERVICE1_HELLO_ACCESS"
}
```

**Response:**
```json
{
    "username": "testuser",
    "permission": "SERVICE1_HELLO_ACCESS",
    "allowed": true
}
```

#### Get User Permissions
```bash
GET /api/acl/permissions/{username}
```

**Response:**
```json
[
    {
        "id": 1,
        "name": "SERVICE1_HELLO_ACCESS",
        "projectName": "service1",
        "critical": false,
        "persianName": "دسترسی به صفحه سلام",
        "createTime": "2024-01-01T10:00:00",
        "createBy": "system",
        "updateTime": null,
        "updateBy": null
    }
]
```

### Role Management

#### Add Role
```bash
POST /api/acl/roles
Content-Type: application/json

{
    "name": "USER"
}
```

#### Assign Role to User
```bash
POST /api/acl/assign-role
Content-Type: application/json

{
    "username": "testuser",
    "roleName": "USER"
}
```

### Permission Management

#### Add Permission
```bash
POST /api/acl/permissions
Content-Type: application/json

{
    "name": "SERVICE1_HELLO_ACCESS",
    "projectName": "service1",
    "critical": false,
    "persianName": "دسترسی به صفحه سلام"
}
```

#### Assign Permission to Role
```bash
POST /api/acl/assign-permission
Content-Type: application/json

{
    "roleName": "USER",
    "permissionName": "SERVICE1_HELLO_ACCESS"
}
```

### Project Registration

#### Register Project
```bash
POST /api/project-registration/register
Content-Type: application/json

{
    "name": "service1",
    "description": "Service1 - Sample Microservice",
    "baseUrl": "http://localhost:8082",
    "version": "1.0.0"
}
```

#### Register Project APIs
```bash
POST /api/project-registration/service1/apis
Content-Type: application/json

[
    {
        "apiPath": "/app1/hello",
        "httpMethod": "GET",
        "permissionName": "SERVICE1_HELLO_ACCESS",
        "description": "Hello endpoint for basic users",
        "isPublic": false,
        "isCritical": false,
        "persianName": "دسترسی به صفحه سلام"
    },
    {
        "apiPath": "/app1/admin",
        "httpMethod": "GET",
        "permissionName": "SERVICE1_ADMIN_ACCESS",
        "description": "Admin endpoint for administrators",
        "isPublic": false,
        "isCritical": true,
        "persianName": "دسترسی ادمین به سرویس 1"
    }
]
```

#### Get Project APIs
```bash
GET /api/project-registration/service1/apis
```

#### Get All Projects
```bash
GET /api/project-registration/projects
```

## Default Data

### Roles
- **USER** - Basic user role
- **ADMIN** - Administrator role
- **SUPER_ADMIN** - Super administrator role

### Permissions
- **SERVICE1_HELLO_ACCESS** - Access to hello endpoint
- **SERVICE1_ADMIN_ACCESS** - Access to admin endpoints
- **SERVICE1_ALL_ACCESS** - Full access to service1

### Users
- **testuser** → USER role
- **admin** → ADMIN role
- **superadmin** → SUPER_ADMIN role

### Role-Permission Assignments
- **USER** can access: SERVICE1_HELLO_ACCESS
- **ADMIN** can access: SERVICE1_HELLO_ACCESS, SERVICE1_ADMIN_ACCESS
- **SUPER_ADMIN** can access: SERVICE1_ALL_ACCESS

## Testing the Flow

### 1. Test User Access (Should Succeed)
```bash
# Login to get token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'

# Access hello endpoint with token
curl http://localhost:8080/service1/app1/hello \
  -H "Authorization: Bearer <token>"
```

### 2. Test Admin Access (Should Succeed)
```bash
# Login as admin
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Access admin endpoint
curl http://localhost:8080/service1/app1/admin \
  -H "Authorization: Bearer <token>"
```

### 3. Test Unauthorized Access (Should Fail)
```bash
# Try to access admin endpoint with user token
curl http://localhost:8080/service1/app1/admin \
  -H "Authorization: Bearer <user-token>"
# Expected: 403 Forbidden
```

## Permission Mapping in Gateway

The gateway maps paths to permission names:

| Path | Permission | Description |
|------|------------|-------------|
| `/service1/app1/hello` | `SERVICE1_HELLO_ACCESS` | Basic user access |
| `/service1/app1/admin` | `SERVICE1_ADMIN_ACCESS` | Admin access |
| `/service1/**` | `SERVICE1_ALL_ACCESS` | Full service access |

## Adding New Permissions

1. **Create Permission:**
```bash
curl -X POST http://localhost:8083/api/acl/permissions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SERVICE2_ACCESS",
    "projectName": "service2",
    "critical": false,
    "persianName": "دسترسی به سرویس 2"
  }'
```

2. **Assign to Role:**
```bash
curl -X POST http://localhost:8083/api/acl/assign-permission \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "USER",
    "permissionName": "SERVICE2_ACCESS"
  }'
```

3. **Update Gateway Mapping:**
Add the permission mapping in `AuthenticationFilter.getPermissionNameForPath()` method.

## Security Features

- **Role-based Access Control** - Users are assigned roles, roles have permissions
- **Permission Granularity** - Fine-grained permission control
- **Critical Permissions** - Mark sensitive permissions as critical
- **Persian Support** - Persian names for permissions
- **Project Organization** - Permissions organized by project
- **Real-time Validation** - Permissions checked in real-time
- **Comprehensive Audit Trail** - All operations are audited with timestamps and user tracking
- **Soft Delete Support** - Records can be marked as deleted without physical removal

## Audit Features

### Audit Fields
All tables include the following audit fields:
- `create_time` - When the record was created
- `created_by` - Who created the record
- `update_time` - When the record was last updated
- `update_by` - Who last updated the record
- `deleted_time` - When the record was deleted (soft delete)
- `deleted_by` - Who deleted the record

### Audit Configuration
- Spring Data JPA Auditing is enabled
- Automatic timestamp and user tracking
- Auditor provider uses Spring Security context

## Database Relationships

```
Users (1) ←→ (1) Roles (1) ←→ (Many) RolePermissions (Many) ←→ (1) Permissions
Projects (1) ←→ (Many) ProjectAPIs (Many) ←→ (1) Permissions
```

- Each user has exactly one role
- Each role can have multiple permissions
- Each permission can be assigned to multiple roles
- Each project can have multiple APIs
- Each API is associated with a permission

## Monitoring

The service includes comprehensive logging:
- Permission check attempts
- Role assignments
- Permission assignments
- User creation
- Project registrations
- API registrations
- Error conditions
- Audit trail events

All operations are logged with appropriate log levels for monitoring and debugging.

## Project Registration Flow

1. **Service Startup**: When a service starts, it automatically registers with ACL
2. **Project Registration**: Service registers its project details (name, description, base URL, version)
3. **API Registration**: Service registers all its APIs with permission mappings
4. **Permission Creation**: ACL automatically creates permissions for new APIs
5. **Gateway Integration**: Gateway uses registered APIs for permission mapping

### Example: Service1 Registration
```java
// Service1 automatically registers on startup
@EventListener(ApplicationReadyEvent.class)
public void registerApisOnStartup() {
    // Register project
    registerProject();
    
    // Register APIs
    registerApis();
}
```

This ensures that all services are automatically discovered and their permissions are properly managed by the ACL service. 