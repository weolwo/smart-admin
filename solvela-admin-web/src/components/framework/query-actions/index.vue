<!--
  * 查询表单尾部的「查询 / 重置」两个按钮。
  *
  * 这一段此前在 27 个列表页里逐字重复。重复本身不致命，致命的是它改不动：
  * 想给「查询」加个 loading、想把「重置」换成图标按钮、想让两个按钮在窄屏换行，
  * 都要改 27 个文件，而漏掉哪个不会有任何提示 —— 表现是同一个后台里
  * 有的页面查询按钮转圈、有的不转，看起来像 bug 其实是漏改。
  *
  * 刻意只收这两个按钮，不把整个查询表单收进来：
  * 每张表的筛选项本来就该长得不一样，那部分不是重复，是各自的业务。
  *
  * @Author:    alaric
  * @Date:      2026-09-06
-->
<template>
  <a-form-item class="solvela-query-form-item">
    <a-button type="primary" :loading="loading" @click="emit('search')">
      <template #icon>
        <SearchOutlined />
      </template>
      {{ searchText }}
    </a-button>
    <a-button class="solvela-margin-left10" :disabled="loading" @click="emit('reset')">
      <template #icon>
        <ReloadOutlined />
      </template>
      {{ resetText }}
    </a-button>
    <!-- 少数页面在两个按钮右边还挂了别的东西（导出、批量操作），留个口子 -->
    <slot />
  </a-form-item>
</template>

<script setup>
  import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';

  defineProps({
    /** 查询中：按钮转圈并禁用重置，避免连点打出两次请求 */
    loading: { type: Boolean, default: false },
    searchText: { type: String, default: '查询' },
    resetText: { type: String, default: '重置' },
  });

  const emit = defineEmits(['search', 'reset']);
</script>
