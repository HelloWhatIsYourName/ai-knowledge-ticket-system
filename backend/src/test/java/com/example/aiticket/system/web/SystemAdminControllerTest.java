package com.example.aiticket.system.web;

import com.example.aiticket.system.SystemAdminService;
import com.example.aiticket.system.SystemPermissionSummary;
import com.example.aiticket.system.SystemRoleSummary;
import com.example.aiticket.system.SystemUserSummary;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemAdminControllerTest {
    @Test
    void endpointsKeepExpectedPermissions() throws Exception {
        assertThat(method("users", int.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:user:manage')");
        assertThat(method("enableUser", Long.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:user:manage')");
        assertThat(method("disableUser", Long.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:user:manage')");
        assertThat(method("replaceUserRoles", Long.class, ReplaceUserRolesRequest.class)
                .getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:role:manage')");
        assertThat(method("roles").getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:role:manage')");
        assertThat(method("permissions").getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('system:role:manage')");
    }

    @Test
    void endpointsMapResponsesWithoutSensitiveFields() {
        FakeSystemAdminService service = new FakeSystemAdminService();
        SystemAdminController controller = new SystemAdminController(service);

        List<SystemUserResponse> users = controller.users(10).data();
        controller.disableUser(7L);
        controller.enableUser(7L);
        controller.replaceUserRoles(7L, new ReplaceUserRolesRequest(List.of(2L, 3L)));

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().roleIds()).containsExactly(2L);
        assertThat(service.lastUserLimit).isEqualTo(10);
        assertThat(service.lastDisabledUserId).isEqualTo(7L);
        assertThat(service.lastEnabledUserId).isEqualTo(7L);
        assertThat(service.lastRoleUserId).isEqualTo(7L);
        assertThat(service.lastRoleIds).containsExactly(2L, 3L);
        assertThat(controller.roles().data()).hasSize(1);
        assertThat(controller.permissions().data()).hasSize(1);
        assertThat(Arrays.stream(SystemUserResponse.class.getRecordComponents()).map(RecordComponent::getName))
                .doesNotContain("passwordHash", "tokenVersion");
    }

    private Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return SystemAdminController.class.getMethod(name, parameterTypes);
    }

    private static final class FakeSystemAdminService extends SystemAdminService {
        private int lastUserLimit;
        private Long lastDisabledUserId;
        private Long lastEnabledUserId;
        private Long lastRoleUserId;
        private List<Long> lastRoleIds;

        private FakeSystemAdminService() {
            super(null);
        }

        @Override
        public List<SystemUserSummary> listUsers(int limit) {
            lastUserLimit = limit;
            return List.of(new SystemUserSummary(7L, "agent", "演示坐席", "ACTIVE", List.of(2L)));
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
        public void disableUser(Long userId) {
            lastDisabledUserId = userId;
        }

        @Override
        public void enableUser(Long userId) {
            lastEnabledUserId = userId;
        }

        @Override
        public void replaceUserRoles(Long userId, List<Long> roleIds) {
            lastRoleUserId = userId;
            lastRoleIds = roleIds;
        }
    }
}
