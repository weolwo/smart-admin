<!--
  * 列表页底部的分页条。
  *
  * 这一段此前在 28 个列表页里逐字重复，连 `共${total}条` 那句文案都一样。
  * 收进来的直接理由：分页参数是【翻页时唯一会变的东西】，而每个页面都自己
  * 用 v-model 双向绑在 queryForm 上、再各自记得在 @change 里重新查一次。
  * 忘了接 @showSizeChange 的表现是「改了每页条数但列表没变」，
  * 而这个漏接在 28 份复制里出现过就再也发现不了。
  *
  * 用法（页面里 queryForm 是 reactive 的）：
  *   <TablePagination v-model:pageNum="queryForm.pageNum"
  *                    v-model:pageSize="queryForm.pageSize"
  *                    :total="total" @change="queryData" />
  *
  * @Author:    alaric
  * @Date:      2026-09-06
-->
<template>
  <div class="solvela-query-table-page">
    <a-pagination
      showSizeChanger
      showQuickJumper
      show-less-items
      :pageSizeOptions="PAGE_SIZE_OPTIONS"
      :defaultPageSize="pageSize"
      :current="pageNum"
      :pageSize="pageSize"
      :total="total"
      :show-total="showTotal"
      @change="onChange"
      @showSizeChange="onShowSizeChange"
    />
  </div>
</template>

<script setup>
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';

  const props = defineProps({
    pageNum: { type: Number, required: true },
    pageSize: { type: Number, required: true },
    total: { type: Number, default: 0 },
  });

  const emit = defineEmits(['update:pageNum', 'update:pageSize', 'change']);

  function showTotal(total) {
    return `共${total}条`;
  }

  /*
   * 🔴 先把 pageNum / pageSize 同步回父组件，再发 change。
   * 反过来的话父组件在 change 里拿 queryForm 发请求，读到的还是上一页的页码 ——
   * 表现是「点第 2 页，列表还是第 1 页的内容，再点一次才对」。
   */
  function onChange(page, size) {
    emit('update:pageNum', page);
    emit('update:pageSize', size);
    emit('change', page, size);
  }

  /*
   * 改每页条数时页码必须回到 1：留在第 5 页而每页从 10 变 50，
   * 请求的是第 201~250 条，多半直接落在空数据上，用户看到的是「怎么没了」。
   * ant-design-vue 的 showSizeChange 已经把 current 传成 1，这里照转即可。
   */
  function onShowSizeChange(current, size) {
    emit('update:pageNum', current);
    emit('update:pageSize', size);
    emit('change', current, size);
  }
</script>
