package com.example.aiticket.system;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SystemAdminMapperXmlTest {
    @Test
    void systemAdminMapperDeclaresUserRolePermissionStatements() throws Exception {
        String mapper = Files.readString(Path.of("src/main/resources/mapper/SystemAdminMapper.xml"));

        assertThat(mapper).contains("listUsers");
        assertThat(mapper).contains("listRoles");
        assertThat(mapper).contains("listPermissions");
        assertThat(mapper).contains("listUserRoleIds");
        assertThat(mapper).contains("deleteUserRoles");
        assertThat(mapper).contains("insertUserRole");
        assertThat(mapper).contains("updateUserStatus");
        assertThat(mapper).contains("FROM sys_user");
        assertThat(mapper).contains("FROM sys_role");
        assertThat(mapper).contains("FROM sys_permission");
    }
}
