package com.example.aiticket.admin;

import com.example.aiticket.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminPingController {
    @PreAuthorize("hasAuthority('system:user:manage')")
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("admin-pong");
    }
}
