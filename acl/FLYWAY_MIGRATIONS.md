# Flyway Migrations for ACL System

## Migration Overview

The ACL system uses a **group-based architecture** with a single comprehensive migration that creates the complete system from scratch.

## Migration Files

### V1__Create_complete_acl_system.sql
- **Purpose**: Complete ACL system creation with group-based architecture
- **Status**: ✅ Current - Single comprehensive migration
- **Creates**:
  - Core tables (users, roles, projects, api_permissions, role_permissions)
  - Group-based tables (groups, user_groups, group_roles)
  - Performance indexes
  - Initial data (users, roles, groups, permissions)
  - User-group assignments
  - Group-role assignments
  - Role-permission assignments
  - Comprehensive documentation

## Database Schema

### Final Architecture (Group-Based)
```
users → user_groups → groups → group_roles → roles → role_permissions → api_permissions
```

## Key Features

### 1. Core Tables
- **`users`** - System users with email and full name
- **`roles`** - User roles (ADMIN, USER, GUEST)
- **`projects`** - Registered services/projects
- **`api_permissions`** - Unified API permissions
- **`role_permissions`** - Role-permission mappings

### 2. Group-Based Tables
- **`groups`** - Organizational units (admin_group, user_group, etc.)
- **`user_groups`** - User-Group relationships
- **`group_roles`** - Group-Role relationships

### 3. No Direct User-Role Assignments
- **`user_roles`** table completely removed
- All user-role assignments go through groups
- Cleaner, more scalable architecture

## Migration Process

1. **Single Migration** - V1__Create_complete_acl_system.sql creates everything
2. **No user_roles table** - Completely removed from the system
3. **Group-based from start** - No migration needed, built with best practices

## Benefits of New Architecture

### 1. **Flexible Organization**
- Users belong to multiple groups
- Groups can have multiple roles
- Easy to manage large user bases

### 2. **Scalable Management**
- Add/remove users from groups
- Add/remove roles from groups
- Bulk permission changes

### 3. **Cleaner Design**
- No unnecessary `is_primary` fields
- Standard group-based permissions
- Follows enterprise patterns

## Usage Examples

### Create Group
```sql
INSERT INTO groups (name, description, is_active) 
VALUES ('developer_group', 'Developer group', true);
```

### Assign User to Group
```sql
INSERT INTO user_groups (user_id, group_id) 
VALUES (1, 1);
```

### Assign Role to Group
```sql
INSERT INTO group_roles (group_id, role_id) 
VALUES (1, 1);
```

### Get User Permissions (Through Groups)
```sql
SELECT DISTINCT p.*
FROM users u
JOIN user_groups ug ON u.id = ug.user_id
JOIN group_roles gr ON ug.group_id = gr.group_id
JOIN role_permissions rp ON gr.role_id = rp.role_id
JOIN api_permissions p ON rp.permission_id = p.id
WHERE u.username = 'john_doe';
```

## Rollback Considerations

If rollback is needed:
1. **V5** - Restore old user-role assignments
2. **V4** - Remove groups and data
3. **V3** - Restore is_primary fields
4. **V2** - Remove group tables and user fields
5. **V1** - Keep initial schema

## Testing

After running migrations:
1. Verify all tables exist
2. Check initial data is populated
3. Test group-based permission checking
4. Verify old direct assignments are removed
5. Test new group-based API endpoints
