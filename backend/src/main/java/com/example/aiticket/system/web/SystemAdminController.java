package com.example.aiticket.system.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.system.SystemAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class SystemAdminController {
    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public ApiResponse<List<SystemUserResponse>> users(@RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.ok(service.listUsers(limit).stream().map(SystemUserResponse::from).toList());
    }

    @PostMapping("/users/{userId}/enable")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public ApiResponse<Void> enableUser(@PathVariable Long userId) {
        service.enableUser(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/users/{userId}/disable")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public ApiResponse<Void> disableUser(@PathVariable Long userId) {
        service.disableUser(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResponse<Void> replaceUserRoles(@PathVariable Long userId,
                                              @RequestBody ReplaceUserRolesRequest request) {
        service.replaceUserRoles(userId, request.roleIds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResponse<List<SystemRoleResponse>> roles() {
        return ApiResponse.ok(service.listRoles().stream().map(SystemRoleResponse::from).toList());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResponse<List<SystemPermissionResponse>> permissions() {
        return ApiResponse.ok(service.listPermissions().stream().map(SystemPermissionResponse::from).toList());
    }
}
