package com.eureka.acl.controller;

import com.eureka.acl.dto.UserGroupAssignRequest;
import com.eureka.acl.entity.UserGroup;
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
@RequestMapping("/api/user-groups")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User-Group Management", description = "مدیریت تخصیص کاربر به گروه")
public class UserGroupController {

    private final AclService aclService;

    @Operation(summary = "تخصیص کاربر به گروه", description = "یک کاربر را به یک گروه اختصاص می‌دهد")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "کاربر با موفقیت به گروه اختصاص داده شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGroup.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "کاربر یا گروه یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserGroup> assignUserToGroup(
            @Parameter(description = "اطلاعات تخصیص کاربر به گروه", required = true)
            @RequestBody UserGroupAssignRequest request) {
        
        log.info("Assigning user {} to group {}", request.username(), request.groupName());
        
        UserGroup userGroup = aclService.assignUserToGroup(
                request.username(),
                request.groupName()
        );
        
        log.info("User assigned successfully to group");
        return ResponseEntity.ok(userGroup);
    }

    @Operation(summary = "به‌روزرسانی گروه‌های کاربر", description = "تمام گروه‌های موجود کاربر را حذف کرده و گروه‌های جدید را ایجاد می‌کند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "گروه‌های کاربر با موفقیت به‌روزرسانی شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGroup.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "404", description = "کاربر یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PutMapping("/user/{username}/groups")
    public ResponseEntity<List<UserGroup>> updateUserGroups(
            @Parameter(description = "نام کاربری", required = true) @PathVariable String username,
            @Parameter(description = "لیست نام‌های گروه", required = true) @RequestBody List<String> groupNames) {
        
        log.info("Updating groups for user: {} with {} groups", username, groupNames.size());
        List<UserGroup> userGroups = aclService.updateUserGroups(username, groupNames);
        log.info("Updated {} groups for user: {}", userGroups.size(), username);
        return ResponseEntity.ok(userGroups);
    }

    @Operation(summary = "دریافت تمام تخصیص‌های کاربر-گروه", description = "لیست تمام تخصیص‌های کاربران و گروه‌ها را برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "لیست تخصیص‌ها با موفقیت دریافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGroup.class))),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserGroup>> listUserGroups() {
        log.info("Getting all user groups");
        List<UserGroup> userGroups = aclService.getAllUserGroups();
        return ResponseEntity.ok(userGroups);
    }

    @Operation(summary = "دریافت تخصیص‌های گروه برای یک کاربر", description = "لیست تمام گروه‌های اختصاص داده شده به یک کاربر خاص را برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تخصیص‌های گروه کاربر با موفقیت دریافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGroup.class))),
            @ApiResponse(responseCode = "404", description = "کاربر یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserGroup>> getUserGroupsByUser(
            @Parameter(description = "نام کاربری", required = true)
            @PathVariable String username) {
        log.info("Getting user groups for user: {}", username);
        List<UserGroup> userGroups = aclService.getUserGroupsByUser(username);
        return ResponseEntity.ok(userGroups);
    }
}
