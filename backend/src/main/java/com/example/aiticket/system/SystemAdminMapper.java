package com.example.aiticket.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemAdminMapper {
    List<SystemUserSummary> listUsers(@Param("limit") int limit);

    List<SystemRoleSummary> listRoles();

    List<SystemPermissionSummary> listPermissions();

    List<Long> listUserRoleIds(@Param("userId") Long userId);

    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    int deleteUserRoles(@Param("userId") Long userId);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
