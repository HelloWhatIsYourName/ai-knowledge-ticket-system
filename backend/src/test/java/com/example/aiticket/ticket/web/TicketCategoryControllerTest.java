package com.example.aiticket.ticket.web;

import com.example.aiticket.ticket.domain.TicketCategory;
import com.example.aiticket.ticket.service.TicketCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketCategoryControllerTest {
    @Test
    void endpointsKeepExpectedPermissions() throws Exception {
        assertManagePermission(method("list", boolean.class));
        assertManagePermission(method("create", CreateTicketCategoryRequest.class));
        assertManagePermission(method("update", Long.class, UpdateTicketCategoryRequest.class));
        assertManagePermission(method("enable", Long.class));
        assertManagePermission(method("disable", Long.class));
    }

    @Test
    void endpointsMapRequestsAndResponses() {
        FakeTicketCategoryService service = new FakeTicketCategoryService();
        TicketCategoryController controller = new TicketCategoryController(service);

        List<TicketCategoryResponse> categories = controller.list(false).data();
        TicketCategoryResponse created = controller.create(new CreateTicketCategoryRequest("售后问题", null, 2, true)).data();
        TicketCategoryResponse updated = controller.update(100L, new UpdateTicketCategoryRequest("账号问题", null, 3)).data();
        TicketCategoryResponse enabled = controller.enable(100L).data();
        TicketCategoryResponse disabled = controller.disable(100L).data();

        assertThat(categories).hasSize(1);
        assertThat(service.lastIncludeDisabled).isFalse();
        assertThat(created.name()).isEqualTo("售后问题");
        assertThat(updated.sortOrder()).isEqualTo(3);
        assertThat(enabled.enabled()).isTrue();
        assertThat(disabled.enabled()).isFalse();
    }

    private void assertManagePermission(Method method) {
        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('ticket:manage')");
    }

    private Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return TicketCategoryController.class.getMethod(name, parameterTypes);
    }

    private static final class FakeTicketCategoryService extends TicketCategoryService {
        private Boolean lastIncludeDisabled;

        private FakeTicketCategoryService() {
            super(null);
        }

        @Override
        public List<TicketCategory> list(boolean includeDisabled) {
            lastIncludeDisabled = includeDisabled;
            return List.of(category(1L, "通用问题", 1, true));
        }

        @Override
        public TicketCategory create(String name, Long parentId, Integer sortOrder, Boolean enabled) {
            return category(100L, name, sortOrder, enabled == null || enabled);
        }

        @Override
        public TicketCategory update(Long id, String name, Long parentId, Integer sortOrder) {
            return category(id, name, sortOrder, true);
        }

        @Override
        public TicketCategory enable(Long id) {
            return category(id, "通用问题", 1, true);
        }

        @Override
        public TicketCategory disable(Long id) {
            return category(id, "通用问题", 1, false);
        }

        private TicketCategory category(Long id, String name, Integer sortOrder, boolean enabled) {
            return new TicketCategory(id, name, null, sortOrder, enabled, null, null);
        }
    }
}
