package com.eureka.acl.controller;

import com.eureka.acl.dto.GroupCreateRequest;
import com.eureka.acl.entity.Group;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Group Management", description = "مدیریت گروه‌ها")
public class GroupController {

    private final AclService aclService;

    @Operation(summary = "ایجاد گروه جدید", description = "یک گروه جدید را در سیستم ایجاد می‌کند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "گروه با موفقیت ایجاد شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "400", description = "درخواست نامعتبر", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Group> createGroup(
            @Parameter(description = "اطلاعات گروه جدید", required = true)
            @RequestBody GroupCreateRequest request) {
        
        log.info("Creating new group: {}", request.name());
        
        Group group = aclService.createGroup(
                request.name(),
                request.description(),
                request.isActive()
        );
        
        log.info("Group created successfully: {}", group.getName());
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "دریافت تمام گروه‌ها", description = "لیست تمام گروه‌های ثبت شده در سیستم را برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "لیست گروه‌ها با موفقیت دریافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Group>> listGroups() {
        log.info("Getting all groups");
        List<Group> groups = aclService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "دریافت گروه بر اساس نام", description = "گروه را بر اساس نام آن برمی‌گرداند")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "گروه با موفقیت یافت شد",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "404", description = "گروه یافت نشد", content = @Content),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور", content = @Content)
    })
    @GetMapping("/{name}")
    public ResponseEntity<Group> getGroupByName(
            @Parameter(description = "نام گروه", required = true)
            @PathVariable String name) {
        log.info("Getting group by name: {}", name);
        return aclService.getGroupByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
