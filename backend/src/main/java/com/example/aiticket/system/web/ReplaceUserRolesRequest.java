package com.example.aiticket.system.web;

import java.util.List;

public record ReplaceUserRolesRequest(
        List<Long> roleIds
) {
}
