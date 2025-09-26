package com.eureka.acl.service;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class AclService {
    
    private final ApiPermissionRepository apiPermissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRoleRepository groupRoleRepository;
    

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String fullName) {
        log.info("=== AclService.createUser() START ===");
        log.info("Parameters: username={}, email={}, fullName={}", username, email, fullName);
        
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            
            User savedUser = userRepository.save(user);
            log.info("User created successfully: id={}, username={}", savedUser.getId(), savedUser.getUsername());
            log.info("=== AclService.createUser() END - SUCCESS ===");
            return savedUser;
        } catch (Exception e) {
            log.error("=== AclService.createUser() END - ERROR ===");
            log.error("Error creating user: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        log.info("=== AclService.getAllUsers() START ===");
        
        try {
            List<User> users = userRepository.findAll();
            log.info("Found {} users", users.size());
            log.info("=== AclService.getAllUsers() END - SUCCESS ===");
            return users;
        } catch (Exception e) {
            log.error("=== AclService.getAllUsers() END - ERROR ===");
            log.error("Error getting all users: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        log.info("=== AclService.getUserByUsername() START ===");
        log.info("Parameters: username={}", username);
        
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                log.info("User found: id={}, username={}", user.get().getId(), user.get().getUsername());
            } else {
                log.warn("User not found: username={}", username);
            }
            log.info("=== AclService.getUserByUsername() END - SUCCESS ===");
            return user;
        } catch (Exception e) {
            log.error("=== AclService.getUserByUsername() END - ERROR ===");
            log.error("Error getting user by username: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Create a new role
     */
    public Role createRole(String name, String description) {
        log.info("=== AclService.createRole() START ===");
        log.info("Parameters: name={}, description={}", name, description);
        
        try {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            
            Role savedRole = roleRepository.save(role);
            log.info("Role created successfully: id={}, name={}", savedRole.getId(), savedRole.getName());
            log.info("=== AclService.createRole() END - SUCCESS ===");
            return savedRole;
        } catch (Exception e) {
            log.error("=== AclService.createRole() END - ERROR ===");
            log.error("Error creating role: name={}, error={}", name, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all roles
     */
    public List<Role> getAllRoles() {
        log.info("Getting all roles");
        return roleRepository.findAll();
    }
    
    /**
     * Get role by name
     */
    public Optional<Role> getRoleByName(String name) {
        log.info("Getting role by name: {}", name);
        return roleRepository.findByName(name);
    }
    
    /**
     * Get permissions for a role
     */
    public List<ApiPermission> getRolePermissions(String roleName) {
        log.info("Getting permissions for role: {}", roleName);
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            log.warn("Role not found: {}", roleName);
            return List.of();
        }
        
        Role role = roleOpt.get();
        return rolePermissionRepository.findPermissionsByRole(role);
    }
    
    /**
     * Assign permission to role (transactional)
     */
    @Transactional
    public RolePermission assignPermissionToRole(String roleName, String permissionName) {
        log.info("Assigning permission {} to role {}", permissionName, roleName);
        
        // Find role
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found: " + roleName);
        }
        
        // Find permission
        Optional<ApiPermission> permissionOpt = apiPermissionRepository.findSingleByName(permissionName);
        if (permissionOpt.isEmpty()) {
            throw new RuntimeException("Permission not found: " + permissionName);
        }
        
        Role role = roleOpt.get();
        ApiPermission permission = permissionOpt.get();
        
        // Check if already exists
        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            log.info("Permission already assigned to role");
            return rolePermissionRepository.findByRoleAndPermission(role, permission).orElse(null);
        }
        
        // Create new role permission
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        
        RolePermission saved = rolePermissionRepository.save(rolePermission);
        log.info("Permission assigned successfully to role");
        return saved;
    }
    
    /**
     * Get all role permissions
     */
    public List<RolePermission> getAllRolePermissions() {
        log.info("Getting all role permissions");
        return rolePermissionRepository.findAll();
    }
    
    /**
     * Get role permissions by role
     */
    public List<RolePermission> getRolePermissionsByRole(String roleName) {
        log.info("Getting role permissions for role: {}", roleName);
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            log.warn("Role not found: {}", roleName);
            return List.of();
        }
        
        return rolePermissionRepository.findByRoleId(roleOpt.get().getId());
    }
    
    /**
     * Get role permissions by permission
     */
    public List<RolePermission> getRolePermissionsByPermission(String permissionName) {
        log.info("Getting role permissions for permission: {}", permissionName);
        
        Optional<ApiPermission> permissionOpt = apiPermissionRepository.findSingleByName(permissionName);
        if (permissionOpt.isEmpty()) {
            log.warn("Permission not found: {}", permissionName);
            return List.of();
        }
        
        return rolePermissionRepository.findByPermissionId(permissionOpt.get().getId());
    }
    
    /**
     * Remove permission from role
     */
    public boolean removePermissionFromRole(String roleName, String permissionName) {
        log.info("Removing permission {} from role {}", permissionName, roleName);
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            log.warn("Role not found: {}", roleName);
            return false;
        }
        
        Optional<ApiPermission> permissionOpt = apiPermissionRepository.findSingleByName(permissionName);
        if (permissionOpt.isEmpty()) {
            log.warn("Permission not found: {}", permissionName);
            return false;
        }
        
        Role role = roleOpt.get();
        ApiPermission permission = permissionOpt.get();
        
        Optional<RolePermission> rolePermissionOpt = rolePermissionRepository.findByRoleAndPermission(role, permission);
        if (rolePermissionOpt.isEmpty()) {
            log.warn("Role permission not found");
            return false;
        }
        
        rolePermissionRepository.delete(rolePermissionOpt.get());
        log.info("Permission removed successfully from role");
        return true;
    }
    
    
    /**
     * Update role permissions (delete all existing + create new ones) - Transactional
     */
    @Transactional
    public List<RolePermission> updateRolePermissions(String roleName, List<String> permissionNames) {
        log.info("Updating permissions for role: {} with {} permissions", roleName, permissionNames.size());
        
        // Find role
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found: " + roleName);
        }
        
        Role role = roleOpt.get();
        
        // Delete all existing role permissions
        List<RolePermission> existingPermissions = rolePermissionRepository.findByRoleId(role.getId());
        rolePermissionRepository.deleteAll(existingPermissions);
        log.info("Deleted {} existing permissions for role: {}", existingPermissions.size(), roleName);
        
        // Create new role permissions
        List<RolePermission> newPermissions = new ArrayList<>();
        for (String permissionName : permissionNames) {
            Optional<ApiPermission> permissionOpt = apiPermissionRepository.findSingleByName(permissionName);
            if (permissionOpt.isPresent()) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permissionOpt.get());
                newPermissions.add(rolePermissionRepository.save(rolePermission));
            } else {
                log.warn("Permission not found: {}", permissionName);
            }
        }
        
        log.info("Created {} new permissions for role: {}", newPermissions.size(), roleName);
        return newPermissions;
    }
    
    
    // ==================== GROUP MANAGEMENT ====================
    
    /**
     * Create a new group
     */
    public Group createGroup(String name, String description, boolean isActive) {
        log.info("Creating group: {}", name);
        
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setActive(isActive);
        
        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully: {}", savedGroup.getName());
        return savedGroup;
    }
    
    /**
     * Get all groups
     */
    public List<Group> getAllGroups() {
        log.info("Getting all groups");
        return groupRepository.findAll();
    }
    
    /**
     * Get group by name
     */
    public Optional<Group> getGroupByName(String name) {
        log.info("Getting group by name: {}", name);
        return groupRepository.findByName(name);
    }
    
    /**
     * Assign user to group
     */
    @Transactional
    public UserGroup assignUserToGroup(String username, String groupName) {
        log.info("Assigning user {} to group {}", username, groupName);
        
        // Find user
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        
        // Find group
        Optional<Group> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group not found: " + groupName);
        }
        
        User user = userOpt.get();
        Group group = groupOpt.get();
        
        // Check if already exists
        if (userGroupRepository.existsByUserAndGroup(user, group)) {
            log.info("User already assigned to group");
            return userGroupRepository.findByUserAndGroup(user, group).orElse(null);
        }
        
        // Create new user group
        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        
        UserGroup saved = userGroupRepository.save(userGroup);
        log.info("User assigned successfully to group");
        return saved;
    }
    
    /**
     * Assign role to group
     */
    @Transactional
    public GroupRole assignRoleToGroup(String groupName, String roleName) {
        log.info("Assigning role {} to group {}", roleName, groupName);
        
        // Find group
        Optional<Group> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group not found: " + groupName);
        }
        
        // Find role
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found: " + roleName);
        }
        
        Group group = groupOpt.get();
        Role role = roleOpt.get();
        
        // Check if already exists
        if (groupRoleRepository.existsByGroupAndRole(group, role)) {
            log.info("Role already assigned to group");
            return groupRoleRepository.findByGroupAndRole(group, role).orElse(null);
        }
        
        // Create new group role
        GroupRole groupRole = new GroupRole();
        groupRole.setGroup(group);
        groupRole.setRole(role);
        
        GroupRole saved = groupRoleRepository.save(groupRole);
        log.info("Role assigned successfully to group");
        return saved;
    }
    
    /**
     * Get all user groups
     */
    public List<UserGroup> getAllUserGroups() {
        log.info("Getting all user groups");
        return userGroupRepository.findAll();
    }
    
    /**
     * Get all group roles
     */
    public List<GroupRole> getAllGroupRoles() {
        log.info("Getting all group roles");
        return groupRoleRepository.findAll();
    }
    
    /**
     * Get user groups by user
     */
    public List<UserGroup> getUserGroupsByUser(String username) {
        log.info("Getting user groups for user: {}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return List.of();
        }
        
        return userGroupRepository.findByUserId(userOpt.get().getId());
    }
    
    /**
     * Get group roles by group
     */
    public List<GroupRole> getGroupRolesByGroup(String groupName) {
        log.info("Getting group roles for group: {}", groupName);
        
        Optional<Group> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            log.warn("Group not found: {}", groupName);
            return List.of();
        }
        
        return groupRoleRepository.findByGroupId(groupOpt.get().getId());
    }
    
    /**
     * Update user groups (delete all existing + create new ones) - Transactional
     */
    @Transactional
    public List<UserGroup> updateUserGroups(String username, List<String> groupNames) {
        log.info("Updating groups for user: {} with {} groups", username, groupNames.size());
        
        // Find user
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        
        User user = userOpt.get();
        
        // Delete all existing user groups
        List<UserGroup> existingGroups = userGroupRepository.findByUserId(user.getId());
        userGroupRepository.deleteAll(existingGroups);
        log.info("Deleted {} existing groups for user: {}", existingGroups.size(), username);
        
        // Create new user groups
        List<UserGroup> newGroups = new ArrayList<>();
        for (String groupName : groupNames) {
            Optional<Group> groupOpt = groupRepository.findByName(groupName);
            if (groupOpt.isPresent()) {
                UserGroup userGroup = new UserGroup();
                userGroup.setUser(user);
                userGroup.setGroup(groupOpt.get());
                newGroups.add(userGroupRepository.save(userGroup));
            } else {
                log.warn("Group not found: {}", groupName);
            }
        }
        
        log.info("Created {} new groups for user: {}", newGroups.size(), username);
        return newGroups;
    }
    
    /**
     * Update group roles (delete all existing + create new ones) - Transactional
     */
    @Transactional
    public List<GroupRole> updateGroupRoles(String groupName, List<String> roleNames) {
        log.info("Updating roles for group: {} with {} roles", groupName, roleNames.size());
        
        // Find group
        Optional<Group> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group not found: " + groupName);
        }
        
        Group group = groupOpt.get();
        
        // Delete all existing group roles
        List<GroupRole> existingRoles = groupRoleRepository.findByGroupId(group.getId());
        groupRoleRepository.deleteAll(existingRoles);
        log.info("Deleted {} existing roles for group: {}", existingRoles.size(), groupName);
        
        // Create new group roles
        List<GroupRole> newRoles = new ArrayList<>();
        for (String roleName : roleNames) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isPresent()) {
                GroupRole groupRole = new GroupRole();
                groupRole.setGroup(group);
                groupRole.setRole(roleOpt.get());
                newRoles.add(groupRoleRepository.save(groupRole));
            } else {
                log.warn("Role not found: {}", roleName);
            }
        }
        
        log.info("Created {} new roles for group: {}", newRoles.size(), groupName);
        return newRoles;
    }
    
    /**
     * Get all roles for a user (through groups)
     */
    public List<Role> getUserRoles(String username) {
        log.info("=== AclService.getUserRoles() START ===");
        log.info("Parameters: username={}", username);
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("User not found: username={}", username);
                log.info("=== AclService.getUserRoles() END - USER NOT FOUND ===");
                return List.of();
            }
            
            User user = userOpt.get();
            Set<Group> userGroups = user.getGroups();
            
            if (userGroups == null || userGroups.isEmpty()) {
                log.warn("User {} has no groups assigned", username);
                log.info("=== AclService.getUserRoles() END - NO GROUPS ===");
                return List.of();
            }
            
            // Create defensive copy to avoid ConcurrentModificationException
            List<Group> groupsList = new ArrayList<>(userGroups);
            log.info("User {} belongs to {} groups: {}", username, groupsList.size(), 
                    groupsList.stream().map(Group::getName).collect(java.util.stream.Collectors.toList()));
            
            // Get all roles from all user's groups
            List<Role> userRoles = groupsList.stream()
                    .flatMap(group -> {
                        Set<Role> groupRoles = group.getRoles();
                        return groupRoles != null ? groupRoles.stream() : Stream.empty();
                    })
                    .distinct()
                    .toList();
            
            log.info("User {} has {} roles: {}", username, userRoles.size(), 
                    userRoles.stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));
            log.info("=== AclService.getUserRoles() END - SUCCESS ===");
            return userRoles;
        } catch (Exception e) {
            log.error("=== AclService.getUserRoles() END - ERROR ===");
            log.error("Error getting user roles: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all permissions for a user (from all their roles through groups)
     */
    public List<ApiPermission> getUserPermissions(String username) {
        log.info("=== AclService.getUserPermissions() START ===");
        log.info("Parameters: username={}", username);
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("User not found: username={}", username);
                log.info("=== AclService.getUserPermissions() END - USER NOT FOUND ===");
                return List.of();
            }
            
            User user = userOpt.get();
            Set<Group> userGroups = user.getGroups();
            
            if (userGroups == null || userGroups.isEmpty()) {
                log.warn("User {} has no groups assigned", username);
                log.info("=== AclService.getUserPermissions() END - NO GROUPS ===");
                return List.of();
            }
            
            log.info("User {} belongs to {} groups: {}", username, userGroups.size(),
                    userGroups.stream().map(Group::getName).collect(java.util.stream.Collectors.toList()));
            
            // Get all roles from all user's groups
            List<Role> userRoles = userGroups.stream()
                    .flatMap(group -> group.getRoles().stream())
                    .distinct()
                    .toList();
            
            if (userRoles.isEmpty()) {
                log.warn("User {} has no roles through groups", username);
                log.info("=== AclService.getUserPermissions() END - NO ROLES ===");
                return List.of();
            }
            
            log.info("User {} has {} roles: {}", username, userRoles.size(),
                    userRoles.stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));
            
            // Get all permissions from all user roles
            List<ApiPermission> permissions = userRoles.stream()
                    .flatMap(role -> rolePermissionRepository.findByRoleId(role.getId()).stream())
                    .map(RolePermission::getPermission)
                    .distinct()  // Remove duplicates
                    .toList();
            
            log.info("User {} has {} permissions: {}", username, permissions.size(),
                    permissions.stream().map(ApiPermission::getName).collect(java.util.stream.Collectors.toList()));
            log.info("=== AclService.getUserPermissions() END - SUCCESS ===");
            return permissions;
            
        } catch (Exception e) {
            log.error("=== AclService.getUserPermissions() END - ERROR ===");
            log.error("Error getting user permissions: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }
    
}