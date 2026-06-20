<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ErrorState from '../../components/common/ErrorState.vue'
import LoadingState from '../../components/common/LoadingState.vue'
import {
  disableSystemUser,
  enableSystemUser,
  listSystemPermissions,
  listSystemRoles,
  listSystemUsers,
  replaceUserRoles,
  type SystemPermission,
  type SystemRole,
  type SystemUser
} from '../../api/systemAdmin'

const users = ref<SystemUser[]>([])
const roles = ref<SystemRole[]>([])
const permissions = ref<SystemPermission[]>([])
const selectedUserId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])
const loading = ref(true)
const error = ref('')
const savingRoles = ref(false)
const updatingUserId = ref<number | null>(null)

const selectedUser = computed(() => users.value.find((user) => user.id === selectedUserId.value) ?? null)
const permissionModules = computed(() => {
  const groups = new Map<string, SystemPermission[]>()

  for (const permission of permissions.value) {
    const module = permission.module || 'default'
    groups.set(module, [...(groups.get(module) ?? []), permission])
  }

  return Array.from(groups.entries()).map(([module, items]) => ({ module, items }))
})

function roleName(roleId: number) {
  return roles.value.find((role) => role.id === roleId)?.roleName ?? `角色 ${roleId}`
}

function selectUser(user: SystemUser) {
  selectedUserId.value = user.id
  selectedRoleIds.value = [...user.roleIds]
}

async function refreshUsers() {
  users.value = await listSystemUsers(100)

  if (!selectedUserId.value && users.value.length > 0) {
    selectUser(users.value[0])
  } else if (selectedUser.value) {
    selectedRoleIds.value = [...selectedUser.value.roleIds]
  }
}

async function loadSystemData() {
  loading.value = true
  error.value = ''

  try {
    const [userResult, roleResult, permissionResult] = await Promise.all([
      listSystemUsers(100),
      listSystemRoles(),
      listSystemPermissions()
    ])

    users.value = userResult
    roles.value = roleResult
    permissions.value = permissionResult

    if (users.value.length > 0) {
      selectUser(users.value[0])
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '系统管理数据加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleUser(user: SystemUser) {
  updatingUserId.value = user.id

  try {
    if (user.status === 'ACTIVE') {
      await disableSystemUser(user.id)
    } else {
      await enableSystemUser(user.id)
    }
    await refreshUsers()
  } finally {
    updatingUserId.value = null
  }
}

async function saveRoles() {
  if (!selectedUser.value || savingRoles.value) {
    return
  }

  savingRoles.value = true

  try {
    await replaceUserRoles(selectedUser.value.id, selectedRoleIds.value)
    await refreshUsers()
  } finally {
    savingRoles.value = false
  }
}

onMounted(loadSystemData)
</script>

<template>
  <section class="system-admin-view">
    <header class="workspace-page-header">
      <div>
        <p>System workspace</p>
        <h3>系统管理</h3>
      </div>
      <span>用户状态 · 角色分配 · 权限概览</span>
    </header>

    <LoadingState v-if="loading" message="正在加载系统管理数据" />
    <ErrorState v-else-if="error" :message="error" />

    <div v-else class="system-admin-layout">
      <main class="system-panel user-admin-panel">
        <div class="panel-heading">
          <span>用户</span>
          <strong>{{ users.length }}</strong>
        </div>
        <EmptyState v-if="users.length === 0" message="暂无用户" />
        <article
          v-for="user in users"
          v-else
          :key="user.id"
          class="system-user-row"
          :class="{ 'is-selected': user.id === selectedUserId }"
          @click="selectUser(user)"
        >
          <div>
            <strong>{{ user.username }}</strong>
            <span>{{ user.displayName }}</span>
          </div>
          <div class="system-user-roles">
            <mark v-for="roleId in user.roleIds" :key="roleId">{{ roleName(roleId) }}</mark>
          </div>
          <span class="system-status">{{ user.status }}</span>
          <button
            :data-testid="`disable-user-${user.id}`"
            type="button"
            :disabled="updatingUserId === user.id"
            @click.stop="toggleUser(user)"
          >
            {{ user.status === 'ACTIVE' ? '禁用' : '启用' }}
          </button>
        </article>
      </main>

      <aside class="system-admin-side">
        <section class="system-panel">
          <div class="panel-heading">
            <span>角色分配</span>
          </div>
          <p v-if="selectedUser" class="system-copy">当前用户：{{ selectedUser.displayName }}</p>
          <EmptyState v-else message="请选择用户" />
          <form v-if="selectedUser" class="role-assignment-form" @submit.prevent="saveRoles">
            <label v-for="role in roles" :key="role.id" class="role-check-row">
              <input
                v-model="selectedRoleIds"
                type="checkbox"
                :value="role.id"
                :data-testid="`role-checkbox-${role.id}`"
              />
              <span>
                <strong>{{ role.roleName }}</strong>
                <small>{{ role.roleCode }} · {{ role.status }}</small>
              </span>
            </label>
            <button data-testid="save-roles" type="button" :disabled="savingRoles" @click="saveRoles">
              {{ savingRoles ? '保存中...' : '保存角色' }}
            </button>
          </form>
        </section>

        <section class="system-panel">
          <div class="panel-heading">
            <span>角色</span>
            <strong>{{ roles.length }}</strong>
          </div>
          <article v-for="role in roles" :key="role.id" class="system-role-item">
            <strong>{{ role.roleName }}</strong>
            <span>{{ role.roleCode }} · {{ role.dataScope || '默认范围' }}</span>
          </article>
        </section>

        <section class="system-panel">
          <div class="panel-heading">
            <span>权限</span>
            <strong>{{ permissions.length }}</strong>
          </div>
          <article v-for="group in permissionModules" :key="group.module" class="permission-module">
            <h4>{{ group.module }}</h4>
            <span v-for="permission in group.items" :key="permission.id">
              {{ permission.permissionName }}
            </span>
          </article>
        </section>
      </aside>
    </div>
  </section>
</template>
