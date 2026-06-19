# 后端认证与 RBAC 基础设计

## 1. 阶段目标

本阶段建设后端认证与权限基础能力，为后续知识库、AI 问答、工单协同、统计管理和前端动态菜单提供统一身份与权限入口。阶段交付重点是登录、JWT 鉴权、角色权限加载、方法级权限控制和可扩展 RBAC 数据模型，不在本阶段实现完整后台用户管理、角色分配页面或复杂数据权限过滤。

本阶段完成后，后续业务接口可以通过 `@PreAuthorize` 按权限编码控制访问，例如知识库上传使用 `knowledge:document:upload`，工单处理使用 `ticket:process`，统计看板使用 `dashboard:view`。

## 2. 范围

### 2.1 本阶段实现

1. 建立 `sys_user`、`sys_role`、`sys_permission`、`sys_menu`、`sys_user_role`、`sys_role_permission` 和 `audit_log` 表。
2. 使用 BCrypt 存储用户密码。
3. 实现用户名密码登录，登录成功后签发 JWT。
4. JWT 中只保存稳定身份摘要，包括用户 ID、用户名、令牌版本和过期时间。
5. 实现 JWT 过滤器，解析 `Authorization: Bearer <token>` 并装载当前用户权限。
6. 支持 `token_version`，用于改密码、禁用账号或强制下线时让旧 token 失效。
7. 初始化四类角色：`SUPER_ADMIN`、`ADMIN`、`AGENT`、`USER`。
8. 初始化覆盖第一版业务范围的权限编码。
9. 初始化基础菜单数据，供 `/api/auth/me` 返回，后续前端动态路由可复用。
10. 记录登录成功、登录失败和访问拒绝等基础审计日志。
11. 提供 `/api/auth/login`、`/api/auth/me`、`/api/auth/ping`、`/api/admin/ping`。
12. 使用测试覆盖登录、JWT 校验、无 token 拒绝、非法 token 拒绝、权限不足拒绝和管理员权限通过。

### 2.2 本阶段不实现

1. 用户管理 CRUD。
2. 角色管理 CRUD。
3. 权限分配 CRUD。
4. 菜单树管理 CRUD。
5. 前端登录页和动态路由。
6. Redis 权限摘要缓存。
7. 部门管理。
8. 本人、本部门、全部数据范围过滤。
9. 登录限流。

这些能力通过当前数据结构和服务边界预留扩展点，在后续阶段按业务优先级补齐。

## 3. 架构边界

认证权限代码按职责拆分为三个边界。

`auth` 负责登录、当前用户查询和认证相关接口。它接收用户名密码，调用用户查询服务和密码校验，生成 JWT，并返回当前用户身份、角色、权限和菜单摘要。

`security` 负责 Spring Security 配置、JWT 签发解析、JWT 过滤器、认证异常响应和方法级权限启用。业务模块不直接依赖 JWT 细节，只通过 Spring Security 的 `Authentication` 或封装后的当前用户对象获取身份。

`system` 或 `user` 负责用户、角色、权限、菜单和审计日志的数据访问。后续用户管理、角色管理、菜单管理接口也应落在这个边界内，避免知识库、工单和 AI 模块直接操作权限表。

后续业务模块只依赖两类稳定接口：

1. 当前用户上下文：读取 `userId`、`username`、角色编码、权限编码、部门 ID 和数据范围。
2. 权限注解：通过 `@PreAuthorize("hasAuthority('permission:code')")` 声明接口访问规则。

## 4. 数据模型

### 4.1 `sys_user`

用户账号表，保存登录和身份基础信息。

字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `NUMBER(19)` | 主键 |
| `username` | `VARCHAR2(64)` | 登录名，唯一 |
| `password_hash` | `VARCHAR2(100)` | BCrypt 密码哈希 |
| `display_name` | `VARCHAR2(100)` | 显示名称 |
| `email` | `VARCHAR2(120)` | 邮箱，可为空 |
| `phone` | `VARCHAR2(32)` | 手机号，可为空 |
| `status` | `VARCHAR2(20)` | `ACTIVE`、`DISABLED` |
| `department_id` | `NUMBER(19)` | 预留部门 ID，本阶段不建部门表 |
| `token_version` | `NUMBER(10)` | token 版本，默认 0 |
| `last_login_at` | `TIMESTAMP` | 最近登录时间 |
| `created_at` | `TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 更新时间 |

扩展说明：

`department_id` 用于后续多部门流转和部门级数据权限。当前阶段只保存字段，不做外键约束，避免部门模块未建设时阻塞认证阶段。

`token_version` 用于后续强制下线、改密码失效旧 token 和禁用账号。JWT 中携带签发时的 `tokenVersion`，请求时与数据库当前值比较，不一致则拒绝访问。

### 4.2 `sys_role`

角色表，保存系统角色和后续数据权限扩展信息。

字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `NUMBER(19)` | 主键 |
| `role_code` | `VARCHAR2(64)` | 角色编码，唯一 |
| `role_name` | `VARCHAR2(100)` | 角色名称 |
| `data_scope` | `VARCHAR2(30)` | `ALL`、`DEPARTMENT`、`SELF` |
| `status` | `VARCHAR2(20)` | `ACTIVE`、`DISABLED` |
| `sort_order` | `NUMBER(10)` | 排序 |
| `created_at` | `TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 更新时间 |

初始化角色：

| 角色编码 | 角色名称 | 数据范围 |
| --- | --- | --- |
| `SUPER_ADMIN` | 超级管理员 | `ALL` |
| `ADMIN` | 管理员 | `ALL` |
| `AGENT` | 坐席或工程师 | `SELF` |
| `USER` | 普通用户 | `SELF` |

`data_scope` 在本阶段不参与 SQL 过滤，只作为后续工单列表、知识库管理、统计接口的数据范围扩展点。

### 4.3 `sys_permission`

权限表，按业务资源和动作编码，不绑定具体 URL。

字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `NUMBER(19)` | 主键 |
| `permission_code` | `VARCHAR2(100)` | 权限编码，唯一 |
| `permission_name` | `VARCHAR2(120)` | 权限名称 |
| `module` | `VARCHAR2(50)` | 所属模块 |
| `description` | `VARCHAR2(255)` | 说明 |
| `status` | `VARCHAR2(20)` | `ACTIVE`、`DISABLED` |
| `created_at` | `TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 更新时间 |

初始权限编码：

| 模块 | 权限编码 | 说明 |
| --- | --- | --- |
| 系统 | `system:user:manage` | 管理用户 |
| 系统 | `system:role:manage` | 管理角色和权限 |
| 系统 | `system:menu:view` | 查看菜单 |
| 知识库 | `knowledge:document:upload` | 上传知识库文档 |
| 知识库 | `knowledge:document:manage` | 管理知识库文档 |
| 知识库 | `knowledge:document:view` | 查看知识库内容 |
| AI 问答 | `ai:chat:ask` | 发起 AI 问答 |
| AI 问答 | `ai:chat:history:view` | 查看本人问答历史 |
| 工单 | `ticket:create` | 创建工单 |
| 工单 | `ticket:view:own` | 查看本人工单 |
| 工单 | `ticket:assign` | 分配工单 |
| 工单 | `ticket:process` | 处理工单 |
| 工单 | `ticket:manage` | 管理工单 |
| 统计 | `dashboard:view` | 查看统计看板 |

权限编码保持业务语义稳定。后续接口路径或控制器拆分不应改变权限编码。

### 4.4 `sys_menu`

菜单表用于前端动态菜单和管理端导航扩展。本阶段建表和初始化基础数据，`/api/auth/me` 返回当前用户可访问菜单摘要，但不实现菜单 CRUD。

字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `NUMBER(19)` | 主键 |
| `parent_id` | `NUMBER(19)` | 父菜单 ID，可为空 |
| `menu_code` | `VARCHAR2(80)` | 菜单编码，唯一 |
| `menu_name` | `VARCHAR2(100)` | 菜单名称 |
| `route_path` | `VARCHAR2(200)` | 前端路由路径 |
| `component` | `VARCHAR2(200)` | 前端组件路径，可为空 |
| `permission_code` | `VARCHAR2(100)` | 访问菜单所需权限，可为空 |
| `icon` | `VARCHAR2(80)` | 图标名，可为空 |
| `sort_order` | `NUMBER(10)` | 排序 |
| `visible` | `NUMBER(1)` | 是否显示 |
| `status` | `VARCHAR2(20)` | `ACTIVE`、`DISABLED` |
| `created_at` | `TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 更新时间 |

初始菜单可包括：

| 菜单编码 | 菜单名称 | 路由 | 权限 |
| --- | --- | --- | --- |
| `chat` | AI 问答 | `/chat` | `ai:chat:ask` |
| `tickets` | 我的工单 | `/tickets` | `ticket:view:own` |
| `agent-workbench` | 坐席工作台 | `/agent/tickets` | `ticket:process` |
| `knowledge` | 知识库管理 | `/admin/knowledge` | `knowledge:document:manage` |
| `users` | 用户权限管理 | `/admin/users` | `system:user:manage` |
| `dashboard` | 统计看板 | `/admin/dashboard` | `dashboard:view` |

### 4.5 关系表

`sys_user_role` 保存用户和角色关系，包含 `user_id`、`role_id`、`created_at`，并对 `user_id, role_id` 建唯一约束。

`sys_role_permission` 保存角色和权限关系，包含 `role_id`、`permission_id`、`created_at`，并对 `role_id, permission_id` 建唯一约束。

本阶段不新增 `sys_role_menu`。菜单是否可见根据菜单绑定的 `permission_code` 和用户权限计算。后续如果菜单与权限需要独立管理，可以新增 `sys_role_menu`，不影响当前权限判断。

### 4.6 `audit_log`

审计日志表记录关键安全和业务操作。

字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `NUMBER(19)` | 主键 |
| `actor_user_id` | `NUMBER(19)` | 操作人 ID，可为空 |
| `actor_username` | `VARCHAR2(64)` | 操作用户名，可为空 |
| `action` | `VARCHAR2(80)` | 操作编码 |
| `target_type` | `VARCHAR2(80)` | 操作对象类型 |
| `target_id` | `VARCHAR2(80)` | 操作对象 ID，可为空 |
| `result` | `VARCHAR2(20)` | `SUCCESS`、`FAILURE`、`DENIED` |
| `message` | `VARCHAR2(500)` | 说明 |
| `ip_address` | `VARCHAR2(64)` | 请求 IP，可为空 |
| `user_agent` | `VARCHAR2(500)` | User-Agent，可为空 |
| `created_at` | `TIMESTAMP` | 创建时间 |

本阶段记录：

1. `AUTH_LOGIN_SUCCESS`
2. `AUTH_LOGIN_FAILURE`
3. `AUTH_ACCESS_DENIED`

后续知识库上传、角色权限变更、工单状态变更和模型配置变更复用该表。

## 5. 认证与授权流程

### 5.1 登录流程

1. 客户端提交 `POST /api/auth/login`，请求体包含 `username` 和 `password`。
2. 后端按用户名查询 `sys_user`。
3. 用户不存在、状态不是 `ACTIVE` 或密码不匹配时，返回统一登录失败响应，不暴露具体原因。
4. 登录成功后加载用户角色、权限和菜单。
5. 更新 `last_login_at`。
6. 写入 `AUTH_LOGIN_SUCCESS` 审计日志。
7. 签发 JWT，返回 token、过期时间、用户摘要、角色编码、权限编码和菜单摘要。

登录失败时写入 `AUTH_LOGIN_FAILURE` 审计日志。为了避免账号枚举，接口响应统一为“用户名或密码错误”。

### 5.2 JWT 请求流程

1. 客户端在请求头携带 `Authorization: Bearer <token>`。
2. JWT 过滤器校验签名和过期时间。
3. 从 token 中读取 `userId`、`username` 和 `tokenVersion`。
4. 查询用户当前状态和当前 `token_version`。
5. 用户不存在、被禁用或 token 版本不一致时拒绝访问。
6. 加载当前用户权限编码，构造 Spring Security `Authentication`。
7. 控制器或服务方法通过 `@PreAuthorize` 判断权限。

本阶段每次请求直接查询数据库加载权限，保证实现简单和行为可靠。后续可在 Redis 中缓存用户权限摘要，缓存 key 可设计为 `auth:permissions:{userId}:{tokenVersion}`，不会改变接口契约。

### 5.3 当前用户接口

`GET /api/auth/me` 返回当前登录用户的身份和权限摘要：

```json
{
  "id": 1,
  "username": "admin",
  "displayName": "系统管理员",
  "roles": ["ADMIN"],
  "permissions": ["knowledge:document:manage", "ticket:assign"],
  "menus": [
    {
      "code": "knowledge",
      "name": "知识库管理",
      "path": "/admin/knowledge",
      "icon": "Folder"
    }
  ]
}
```

后续前端接入时可直接用 `menus` 渲染基础导航，也可以根据 `permissions` 控制按钮显示。

## 6. 接口设计

### 6.1 `POST /api/auth/login`

请求：

```json
{
  "username": "admin",
  "password": "Admin_123456"
}
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt-token",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "displayName": "系统管理员"
    },
    "roles": ["ADMIN"],
    "permissions": ["system:user:manage", "knowledge:document:manage"],
    "menus": []
  }
}
```

### 6.2 `GET /api/auth/me`

需要登录。返回当前用户、角色、权限和菜单摘要。

### 6.3 `GET /api/auth/ping`

需要登录。用于验证 JWT 鉴权链路。

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": "pong"
}
```

### 6.4 `GET /api/admin/ping`

需要 `system:user:manage` 或等价管理员权限。用于验证方法级权限。

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": "admin-pong"
}
```

## 7. 配置

`application.yml` 新增：

```yaml
security:
  jwt:
    secret: ${APP_JWT_SECRET:dev-only-change-me-to-a-long-random-secret}
    expires-in-seconds: ${APP_JWT_EXPIRES_IN_SECONDS:7200}
```

生产和演示环境应通过环境变量配置更长、更随机的密钥。默认密钥只用于本地开发。

## 8. 初始化数据

Flyway `V2__auth_rbac.sql` 初始化：

1. 四类角色。
2. 第一版权限编码。
3. 基础菜单。
4. 角色权限关系。
5. 超级管理员账号。
6. 管理员、坐席、普通用户演示账号可选初始化，便于接口测试和答辩演示。

演示账号建议：

| 用户名 | 角色 | 用途 |
| --- | --- | --- |
| `superadmin` | `SUPER_ADMIN` | 系统级配置和全部权限 |
| `admin` | `ADMIN` | 知识库、工单和统计管理 |
| `agent` | `AGENT` | 坐席处理工单 |
| `user` | `USER` | 普通用户问答和提交工单 |

密码哈希必须是 BCrypt 结果，迁移脚本中不得保存明文密码。

## 9. 测试策略

### 9.1 单元测试

1. BCrypt 密码校验：明文密码能匹配哈希，错误密码不能匹配。
2. JWT 服务：能生成 token，能解析用户 ID、用户名和 token 版本。
3. JWT 过期：过期 token 被拒绝。
4. 当前用户权限聚合：用户角色和权限能正确去重。
5. 菜单过滤：只返回用户拥有权限对应的可见菜单。

### 9.2 Web 层测试

1. 正确用户名密码登录成功。
2. 错误密码登录失败，响应不暴露用户是否存在。
3. 不带 token 访问 `/api/auth/ping` 返回 401。
4. 非法 token 访问 `/api/auth/ping` 返回 401。
5. 普通用户 token 访问 `/api/auth/ping` 成功。
6. 普通用户 token 访问 `/api/admin/ping` 返回 403。
7. 管理员 token 访问 `/api/admin/ping` 成功。

### 9.3 数据库迁移验证

运行 `mvn test` 时至少验证迁移脚本语法和核心权限查询逻辑。若本地 Oracle 未启动，Web 层测试可使用 MockMvc 和测试替身验证 Security 行为；真实 Oracle 联调放在后续集成验证步骤。

## 10. 可扩展性校准

本设计对项目计划书中的扩展点做如下对应：

| 项目计划扩展点 | 本阶段处理 |
| --- | --- |
| 权限服务独立 | 认证、Security、系统用户权限边界分离 |
| 菜单模型 | 建 `sys_menu`，返回当前用户菜单摘要，不做 CRUD |
| 数据权限 | `sys_role.data_scope` 预留 `ALL`、`DEPARTMENT`、`SELF` |
| 多部门流转 | `sys_user.department_id` 预留，不建部门模块 |
| Redis 权限摘要缓存 | JWT 只放稳定身份，后续可加 Redis 缓存权限而不改接口 |
| 可审计性 | 建 `audit_log` 并记录认证相关事件 |
| 知识库和工单权限接入 | 初始化业务权限编码，后续接口直接使用 `@PreAuthorize` |
| 第一版范围控制 | 不做 CRUD、不做前端、不做复杂数据范围 SQL |

## 11. 验收标准

1. 数据库迁移后存在完整 RBAC 基础表和审计日志表。
2. 系统有可登录的管理员账号。
3. 登录成功返回 JWT、用户信息、角色、权限和菜单摘要。
4. 无 token 或非法 token 访问受保护接口返回 401。
5. 普通用户访问管理员验证接口返回 403。
6. 管理员访问管理员验证接口成功。
7. 禁用用户或 token 版本不一致时旧 token 失效。
8. 后续业务模块可以通过权限编码接入方法级权限控制。
9. 当前阶段没有引入用户管理、角色管理、菜单管理等超出范围的 CRUD。
