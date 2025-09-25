package com.eureka.acl.controller;

import com.eureka.acl.dto.GroupRoleAssignRequest;
import com.eureka.acl.entity.GroupRole;
import com.eureka.acl.service.AclService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-roles")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Group-Role Management", description = "مدیریت تخصیص نقش به گروه")
public class GroupRoleController {

    private final AclService aclService;

    @Operation(summary = "تخصیص نقش به گروه", description = "یک نقش را به یک گروه اختصاص می‌دهد")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "نقش با موفقیت به گروه اختصاص داده شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupRole.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "گروه یا نقش یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GroupRole> assignRoleToGroup(
            @Parameter(description = "اطلاعات تخصیص نقش به گروه", required = true)
            @RequestBody GroupRoleAssignRequest request) {
        
        log.info("Assigning role {} to group {}", request.roleName(), request.groupName());
        
        GroupRole groupRole = aclService.assignRoleToGroup(
                request.groupName(),
                request.roleName()
        );
        
        log.info("Role assigned successfully to group");
        return ResponseEntity.ok(groupRole);
    }

    @Operation(summary = "به‌روزرسانی نقش‌های گروه", description = "تمام نقش‌های موجود گروه را حذف کرده و نقش‌های جدید را ایجاد می‌کند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "نقش‌های گروه با موفقیت به‌روزرسانی شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupRole.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "گروه یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PutMapping("/group/{groupName}/roles")
    public ResponseEntity<List<GroupRole>> updateGroupRoles(
            @Parameter(description = "نام گروه", required = true) @PathVariable String groupName,
            @Parameter(description = "لیست نام‌های نقش", required = true) @RequestBody List<String> roleNames) {
        
        log.info("Updating roles for group: {} with {} roles", groupName, roleNames.size());
        List<GroupRole> groupRoles = aclService.updateGroupRoles(groupName, roleNames);
        log.info("Updated {} roles for group: {}", groupRoles.size(), groupName);
        return ResponseEntity.ok(groupRoles);
    }

    @Operation(summary = "دریافت تمام تخصیص‌های گروه-نقش", description = "لیست تمام تخصیص‌های گروه‌ها و نقش‌ها را برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "لیست تخصیص‌ها با موفقیت دریافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupRole.class))),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<GroupRole>> listGroupRoles() {
        log.info("Getting all group roles");
        List<GroupRole> groupRoles = aclService.getAllGroupRoles();
        return ResponseEntity.ok(groupRoles);
    }

    @Operation(summary = "دریافت تخصیص‌های نقش برای یک گروه", description = "لیست تمام نقش‌های اختصاص داده شده به یک گروه خاص را برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تخصیص‌های نقش گروه با موفقیت دریافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupRole.class))),
            @ApiResponse(responseCode = "404", description = "گروه یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<GroupRole>> getGroupRolesByGroup(
            @Parameter(description = "نام گروه", required = true)
            @PathVariable String groupName) {
        log.info("Getting group roles for group: {}", groupName);
        List<GroupRole> groupRoles = aclService.getGroupRolesByGroup(groupName);
        return ResponseEntity.ok(groupRoles);
    }
}
