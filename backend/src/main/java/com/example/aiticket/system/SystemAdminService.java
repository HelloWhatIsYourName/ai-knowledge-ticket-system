package com.example.aiticket.system;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SystemAdminService {
    private final SystemAdminMapper mapper;

    public SystemAdminService(SystemAdminMapper mapper) {
        this.mapper = mapper;
    }

    public List<SystemUserSummary> listUsers(int limit) {
        return mapper.listUsers(normalizedLimit(limit)).stream()
                .map(user -> new SystemUserSummary(user.id(), user.username(), user.displayName(), user.status(),
                        mapper.listUserRoleIds(user.id())))
                .toList();
    }

    public List<SystemRoleSummary> listRoles() {
        return mapper.listRoles();
    }

    public List<SystemPermissionSummary> listPermissions() {
        return mapper.listPermissions();
    }

    public void enableUser(Long userId) {
        updateUserStatus(userId, "ACTIVE");
    }

    public void disableUser(Long userId) {
        updateUserStatus(userId, "DISABLED");
    }

    public void updateUserStatus(Long userId, String status) {
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new IllegalArgumentException("user status must be ACTIVE or DISABLED");
        }
        mapper.updateUserStatus(userId, status);
    }

    @Transactional
    public void replaceUserRoles(Long userId, List<Long> roleIds) {
        mapper.deleteUserRoles(userId);
        if (roleIds == null) {
            return;
        }
        roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(roleId -> mapper.insertUserRole(userId, roleId));
    }

    private int normalizedLimit(int limit) {
        if (limit <= 0) {
            return 100;
        }
        return Math.min(limit, 200);
    }
}
