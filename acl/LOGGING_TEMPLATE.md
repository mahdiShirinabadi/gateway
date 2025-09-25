# Comprehensive Logging Template for ACL System

## Production Logging Standards

In production environments, we need comprehensive logging to debug issues without code access. Here's the logging template for all methods:

## Logging Pattern

### 1. Method Start Logging
```java
log.info("=== ClassName.methodName() START ===");
log.info("Parameters: param1={}, param2={}, param3={}", param1, param2, param3);
```

### 2. Method End Logging (Success)
```java
log.info("Result: found {} items", result.size());
log.info("=== ClassName.methodName() END - SUCCESS ===");
```

### 3. Method End Logging (Error)
```java
log.error("=== ClassName.methodName() END - ERROR ===");
log.error("Error details: param1={}, error={}", param1, e.getMessage(), e);
```

### 4. Method End Logging (Not Found)
```java
log.warn("Entity not found: id={}", id);
log.info("=== ClassName.methodName() END - NOT FOUND ===");
```

## Complete Method Template

```java
public ReturnType methodName(ParamType param1, ParamType param2) {
    log.info("=== ClassName.methodName() START ===");
    log.info("Parameters: param1={}, param2={}", param1, param2);
    
    try {
        // Business logic here
        log.debug("Processing step 1: {}", step1Result);
        log.debug("Processing step 2: {}", step2Result);
        
        ReturnType result = performOperation();
        
        log.info("Operation completed successfully: result={}", result);
        log.info("=== ClassName.methodName() END - SUCCESS ===");
        return result;
        
    } catch (Exception e) {
        log.error("=== ClassName.methodName() END - ERROR ===");
        log.error("Error in methodName: param1={}, param2={}, error={}", 
                param1, param2, e.getMessage(), e);
        throw e;
    }
}
```

## Controller Method Template

```java
@PostMapping
public ResponseEntity<EntityType> createEntity(@RequestBody RequestType request) {
    log.info("=== ControllerName.createEntity() START ===");
    log.info("Request parameters: field1={}, field2={}", 
            request.field1(), request.field2());
    
    try {
        EntityType entity = service.createEntity(
                request.field1(),
                request.field2()
        );
        
        log.info("Entity created successfully: id={}, name={}", 
                entity.getId(), entity.getName());
        log.info("=== ControllerName.createEntity() END - SUCCESS ===");
        return ResponseEntity.ok(entity);
        
    } catch (Exception e) {
        log.error("=== ControllerName.createEntity() END - ERROR ===");
        log.error("Error creating entity: field1={}, error={}", 
                request.field1(), e.getMessage(), e);
        throw e;
    }
}
```

## Service Method Template

```java
public List<EntityType> getEntities(String param) {
    log.info("=== ServiceName.getEntities() START ===");
    log.info("Parameters: param={}", param);
    
    try {
        List<EntityType> entities = repository.findByParam(param);
        
        log.info("Found {} entities for param: {}", entities.size(), param);
        log.debug("Entity details: {}", entities.stream()
                .map(e -> e.getId() + ":" + e.getName())
                .collect(Collectors.toList()));
        
        log.info("=== ServiceName.getEntities() END - SUCCESS ===");
        return entities;
        
    } catch (Exception e) {
        log.error("=== ServiceName.getEntities() END - ERROR ===");
        log.error("Error getting entities: param={}, error={}", param, e.getMessage(), e);
        throw e;
    }
}
```

## Critical Methods That Need Extra Logging

### 1. Permission Checking Methods
```java
public boolean hasPermission(String username, String permission) {
    log.info("=== AclService.hasPermission() START ===");
    log.info("Parameters: username={}, permission={}", username, permission);
    
    try {
        // Log user groups
        Set<Group> userGroups = user.getGroups();
        log.info("User {} belongs to {} groups: {}", username, userGroups.size(),
                userGroups.stream().map(Group::getName).collect(Collectors.toList()));
        
        // Log user roles
        List<Role> userRoles = getUserRoles(username);
        log.info("User {} has {} roles: {}", username, userRoles.size(),
                userRoles.stream().map(Role::getName).collect(Collectors.toList()));
        
        // Log permission check result
        boolean hasPermission = checkPermission(username, permission);
        log.info("Permission check result: username={}, permission={}, result={}", 
                username, permission, hasPermission);
        
        log.info("=== AclService.hasPermission() END - SUCCESS ===");
        return hasPermission;
        
    } catch (Exception e) {
        log.error("=== AclService.hasPermission() END - ERROR ===");
        log.error("Error checking permission: username={}, permission={}, error={}", 
                username, permission, e.getMessage(), e);
        throw e;
    }
}
```

### 2. Transactional Methods
```java
@Transactional
public List<EntityType> updateEntities(String param, List<String> newValues) {
    log.info("=== ServiceName.updateEntities() START ===");
    log.info("Parameters: param={}, newValues={}", param, newValues);
    
    try {
        // Log existing entities
        List<EntityType> existing = repository.findByParam(param);
        log.info("Found {} existing entities to update", existing.size());
        
        // Delete existing
        repository.deleteAll(existing);
        log.info("Deleted {} existing entities", existing.size());
        
        // Create new entities
        List<EntityType> newEntities = new ArrayList<>();
        for (String value : newValues) {
            EntityType entity = new EntityType();
            entity.setValue(value);
            newEntities.add(repository.save(entity));
        }
        
        log.info("Created {} new entities", newEntities.size());
        log.info("=== ServiceName.updateEntities() END - SUCCESS ===");
        return newEntities;
        
    } catch (Exception e) {
        log.error("=== ServiceName.updateEntities() END - ERROR ===");
        log.error("Error updating entities: param={}, error={}", param, e.getMessage(), e);
        throw e;
    }
}
```

## Log Levels Usage

### INFO Level
- Method start/end markers
- Parameter values
- Result summaries
- Business logic flow

### DEBUG Level
- Detailed processing steps
- Intermediate values
- Loop iterations
- Database query details

### WARN Level
- Not found scenarios
- Validation failures
- Business rule violations
- Performance issues

### ERROR Level
- Exceptions and errors
- Critical failures
- System errors
- Stack traces

## Production Log Analysis

### Common Log Patterns to Look For

1. **Method Start/End Pairs**
   ```
   === AclService.getUserRoles() START ===
   === AclService.getUserRoles() END - SUCCESS ===
   ```

2. **Error Patterns**
   ```
   === AclService.getUserRoles() START ===
   === AclService.getUserRoles() END - ERROR ===
   ```

3. **Permission Check Flow**
   ```
   User john belongs to 2 groups: [admin_group, user_group]
   User john has 3 roles: [ADMIN, USER, MANAGER]
   Permission check result: username=john, permission=READ_USERS, result=true
   ```

4. **Transaction Flow**
   ```
   Found 5 existing entities to update
   Deleted 5 existing entities
   Created 3 new entities
   ```

## Log File Structure

### Recommended Log Format
```
2024-01-15 10:30:45.123 INFO  [http-nio-8080-exec-1] c.e.a.s.AclService - === AclService.getUserRoles() START ===
2024-01-15 10:30:45.124 INFO  [http-nio-8080-exec-1] c.e.a.s.AclService - Parameters: username=john_doe
2024-01-15 10:30:45.125 INFO  [http-nio-8080-exec-1] c.e.a.s.AclService - User john_doe belongs to 2 groups: [admin_group, user_group]
2024-01-15 10:30:45.126 INFO  [http-nio-8080-exec-1] c.e.a.s.AclService - User john_doe has 3 roles: [ADMIN, USER, MANAGER]
2024-01-15 10:30:45.127 INFO  [http-nio-8080-exec-1] c.e.a.s.AclService - === AclService.getUserRoles() END - SUCCESS ===
```

This comprehensive logging approach ensures that production issues can be debugged effectively through log analysis alone.
