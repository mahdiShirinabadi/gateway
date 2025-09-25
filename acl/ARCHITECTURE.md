# ACL System Architecture - Group-Based Design

## New Architecture (Group-Based)

```
User → UserGroup → Group → GroupRole → Role → RolePermission → ApiPermission
```

## Database Tables

### Core Tables
- **users** - User information
- **groups** - Group definitions
- **roles** - Role definitions
- **api_permissions** - API permission definitions

### Relationship Tables
- **user_groups** - User-Group assignments (UserGroup entity)
- **group_roles** - Group-Role assignments (GroupRole entity)
- **role_permissions** - Role-Permission assignments (RolePermission entity)

## Benefits of Group-Based Design

### 1. **Flexible Organization**
- Users belong to multiple groups
- Groups can have multiple roles
- Easy to manage large user bases

### 2. **Scalable Management**
- Add/remove users from groups
- Add/remove roles from groups
- Bulk permission changes

### 3. **Hierarchical Permissions**
```
User "john" → Group "admin_group" → Roles ["admin", "manager"] → Permissions ["read", "write", "delete"]
```

## Example Usage

### Create Groups
```java
POST /api/groups
{
  "name": "admin_group",
  "description": "Administrator group",
  "isActive": true
}
```

### Assign Roles to Groups
```java
POST /api/group-roles
{
  "groupName": "admin_group",
  "roleName": "admin"
}
```

### Assign Users to Groups
```java
POST /api/user-groups
{
  "username": "john_doe",
  "groupName": "admin_group",
  "isPrimary": true
}
```

### Get User Permissions (Through Groups)
```java
GET /api/users/john_doe/permissions
// Returns all permissions from all roles in all groups
```

## Migration from Direct User-Role

### Before (Direct)
```
User → UserRole → Role
```

### After (Group-Based)
```
User → UserGroup → Group → GroupRole → Role
```

## Key Changes Made

1. **User Entity**: Changed from `Set<Role> roles` to `Set<Group> groups`
2. **AclService**: Updated methods to use group-based approach
3. **UnifiedAclService**: Updated permission checking to use groups
4. **New Controllers**: GroupController, UserGroupController, GroupRoleController

## Transactional Updates

- **Update User Groups**: `PUT /api/user-groups/user/{username}/groups`
- **Update Group Roles**: `PUT /api/group-roles/group/{groupName}/roles`
- **Update Role Permissions**: `PUT /api/role-permissions/role/{roleName}/permissions`

All updates use transactional delete-all + create-new approach for data consistency.
