package com.example.aiticket.system;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemAdminServiceTest {
    @Test
    void listUsersNormalizesLimitAndAttachesRoleIds() {
        FakeSystemAdminMapper mapper = new FakeSystemAdminMapper();
        SystemAdminService service = new SystemAdminService(mapper);

        List<SystemUserSummary> users = service.listUsers(0);

        assertThat(mapper.lastUserLimit).isEqualTo(100);
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().roleIds()).containsExactly(2L, 3L);

        service.listUsers(300);
        assertThat(mapper.lastUserLimit).isEqualTo(200);
    }

    @Test
    void listRolesAndPermissionsReturnMapperData() {
        SystemAdminService service = new SystemAdminService(new FakeSystemAdminMapper());

        assertThat(service.listRoles()).extracting(SystemRoleSummary::roleCode).containsExactly("ADMIN");
        assertThat(service.listPermissions()).extracting(SystemPermissionSummary::permissionCode)
                .containsExactly("dashboard:view");
    }

    @Test
    void enableDisableAndReplaceRolesUseMapper() {
        FakeSystemAdminMapper mapper = new FakeSystemAdminMapper();
        SystemAdminService service = new SystemAdminService(mapper);

        service.disableUser(7L);
        assertThat(mapper.lastStatus).isEqualTo("DISABLED");

        service.enableUser(7L);
        assertThat(mapper.lastStatus).isEqualTo("ACTIVE");

        service.replaceUserRoles(7L, List.of(3L, 2L, 3L));
        assertThat(mapper.deletedRoleUserId).isEqualTo(7L);
        assertThat(mapper.insertedRoleIds).containsExactly(2L, 3L);
    }

    @Test
    void invalidStatusIsRejected() {
        SystemAdminService service = new SystemAdminService(new FakeSystemAdminMapper());

        assertThatThrownBy(() -> service.updateUserStatus(7L, "LOCKED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user status must be ACTIVE or DISABLED");
    }

    private static final class FakeSystemAdminMapper implements SystemAdminMapper {
        private int lastUserLimit;
        private String lastStatus;
        private Long deletedRoleUserId;
        private final List<Long> insertedRoleIds = new ArrayList<>();

        @Override
        public List<SystemUserSummary> listUsers(int limit) {
            lastUserLimit = limit;
            return List.of(new SystemUserSummary(7L, "agent", "演示坐席", "ACTIVE", List.of()));
        }

        @Override
        public List<SystemRoleSummary> listRoles() {
            return List.of(new SystemRoleSummary(2L, "ADMIN", "管理员", "ALL", "ACTIVE", 2));
        }

        @Override
        public List<SystemPermissionSummary> listPermissions() {
            return List.of(new SystemPermissionSummary(14L, "dashboard:view", "查看统计看板", "dashboard"));
        }

        @Override
        public List<Long> listUserRoleIds(Long userId) {
            return List.of(2L, 3L);
        }

        @Override
        public int updateUserStatus(Long userId, String status) {
            lastStatus = status;
            return 1;
        }

        @Override
        public int deleteUserRoles(Long userId) {
            deletedRoleUserId = userId;
            return 1;
        }

        @Override
        public int insertUserRole(Long userId, Long roleId) {
            insertedRoleIds.add(roleId);
            return 1;
        }
    }
}
