<!--
  * 设备
  *
  * 【这个页面补的是「数据看得见」那一半】t_device 从 2026-09-08 起就在写了，
  * 但一直没有后台入口 —— 也就是说这张表只有开发能用 SQL 看，
  * 而它要回答的那几个问题恰恰是运营和风控要看的。
  *
  * 【三个筛选项对应三个真实的问题】
  *   · 设备号   —— 这台设备是谁（从登录日志那一列点过来）
  *   · 签发IP   —— 这个 IP 领了多少台设备（识别批量领设备最直接的信号）
  *   · 处置档   —— 现在有多少台在观察 / 被封
  * 刻意没有按型号 / 系统版本筛：那几列是客户端自报、不验的，
  * 拿它们当筛选条件会让人以为那是可信数据。
  *
  * 【没有新增】一台设备的诞生只可能来自客户端调 /device/register。
  * 后台能做的只有查和处置。
  *
  * @Date  2026-09-10
-->
<template>
  <!---------- 查询表单form begin ----------->
  <a-form class="solvela-query-form">
    <a-row class="solvela-query-form-row">
      <a-form-item label="设备号" class="solvela-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.deviceId" placeholder="32 位，精确匹配" allow-clear @press-enter="onSearch" />
      </a-form-item>
      <a-form-item label="签发IP" class="solvela-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.registerIp" placeholder="精确匹配" allow-clear @press-enter="onSearch" />
      </a-form-item>
      <a-form-item label="设备端" class="solvela-query-form-item">
        <a-select style="width: 110px" v-model:value="queryForm.deviceType" :options="DEVICE_TYPE_OPTIONS" placeholder="全部" allow-clear />
      </a-form-item>
      <a-form-item label="处置档" class="solvela-query-form-item">
        <a-select style="width: 110px" v-model:value="queryForm.status" :options="DEVICE_STATUS_OPTIONS" placeholder="全部" allow-clear />
      </a-form-item>
      <a-form-item label="签发时间" class="solvela-query-form-item">
        <a-range-picker v-model:value="createTime" :presets="defaultTimeRanges" style="width: 230px" @change="onChangeCreateTime" />
      </a-form-item>
      <QueryActions @search="onSearch" @reset="resetQuery" />
    </a-row>
  </a-form>
  <!---------- 查询表单form end ----------->

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-table
      size="small"
      :dataSource="tableData"
      :columns="columns"
      :pagination="false"
      :loading="tableLoading"
      rowKey="id"
      bordered
    >
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'deviceId'">
          <a :title="record.deviceId" @click="openMembers(record)">{{ record.deviceId.slice(0, 12) }}…</a>
          <div class="cell-sub">{{ record.deviceType }}</div>
        </template>

        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="statusMeta(text).color">{{ statusMeta(text).desc }}</a-tag>
          <!--
            处置原因跟着档位走。只显示标签不显示原因的话，
            客服看到一个红色的「封禁」却不知道为什么，只能来问开发
          -->
          <div v-if="record.remark" class="cell-sub" :title="record.remark">{{ record.remark }}</div>
        </template>

        <template v-else-if="column.dataIndex === 'registerIp'">
          <div>{{ record.registerIp || '—' }}</div>
          <div class="cell-sub">{{ record.registerRegion }}</div>
        </template>

        <template v-else-if="column.dataIndex === 'model'">
          <!--
            ⚠️ 这三列是客户端自报、服务端不验的。标一句「自报」是刻意的：
            不标的话它们看起来和 device_id 一样可信，而它们完全可以是编的
          -->
          <div>{{ record.model || '—' }}</div>
          <div class="cell-sub">{{ [record.osVersion, record.appVersion].filter(Boolean).join(' / ') }}</div>
        </template>

        <template v-else-if="column.dataIndex === 'attestLevel'">
          <a-tag :color="attestMeta(text).color">{{ attestMeta(text).desc }}</a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'operator'">
          <span v-if="record.operator">{{ record.operator }}</span>
          <span v-else class="cell-sub">自动</span>
        </template>

        <template v-else-if="column.dataIndex === 'action'">
          <a @click="openMembers(record)">关联账号</a>
          <a-divider type="vertical" />
          <a @click="openDispose(record)">处置</a>
        </template>
      </template>
    </a-table>

    <TablePagination v-model:pageNum="queryForm.pageNum" v-model:pageSize="queryForm.pageSize" :total="total" @change="queryData" />
  </a-card>

  <!---------- 关联账号 begin ----------->
  <a-drawer v-model:open="membersOpen" title="这台设备碰过哪些账号" width="620">
    <a-alert
      v-if="members.length > 1"
      type="warning"
      show-icon
      :message="`这台设备关联了 ${members.length} 个账号`"
      description="一台设备下挂着多个账号，和多台设备各挂一个账号，是完全不同的两件事。数字本身不等于作弊 —— 家庭共用、门店演示都会这样，要结合登录时间的密集程度一起看。"
      style="margin-bottom: 12px"
    />
    <div class="cell-sub" style="margin-bottom: 8px">设备号：{{ currentDeviceId }}（最多列 50 个）</div>
    <a-table size="small" :dataSource="members" :columns="memberColumns" :pagination="false" :loading="membersLoading" rowKey="memberId" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'memberName'">
          <div>{{ record.memberName || '—' }}</div>
          <div class="cell-sub">{{ record.memberId }}</div>
        </template>
      </template>
    </a-table>
  </a-drawer>
  <!---------- 关联账号 end ----------->

  <!---------- 人工处置 begin ----------->
  <a-modal v-model:open="disposeOpen" title="人工处置" :confirm-loading="disposing" @ok="submitDispose">
    <a-form :label-col="{ span: 5 }">
      <a-form-item label="设备号">
        <span>{{ disposeForm.deviceId }}</span>
      </a-form-item>
      <a-form-item label="目标档位">
        <a-radio-group v-model:value="disposeForm.status" :options="DEVICE_STATUS_OPTIONS" />
      </a-form-item>
      <a-form-item label="处置原因">
        <a-input v-model:value="disposeForm.remark" placeholder="必填，事后回头看就靠这一句" allow-clear />
      </a-form-item>
    </a-form>
    <!--
      🔴 把后果写在按钮旁边，而不是只写在文档里。
      「封禁」和「观察」对用户的影响差得很远，而这个弹窗是唯一会被读到的地方。
    -->
    <a-alert v-if="disposeForm.status === DEVICE_STATUS_ENUM.BANNED.value" type="error" show-icon message="封禁后这台设备将无法注册和登录，且不会自动解除，只能人工解封。" />
    <a-alert v-else-if="disposeForm.status === DEVICE_STATUS_ENUM.OBSERVE.value" type="warning" show-icon message="观察档不拦人：这台设备照常能用，只是登录时要多验一道验证码。观察期满会自动回到正常。" />
    <a-alert v-else type="info" show-icon message="恢复正常，并立即解除观察期。" />
  </a-modal>
  <!---------- 人工处置 end ----------->
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/member/device-api';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import { DEVICE_TYPE_OPTIONS, metaOf } from '/@/constants/business/member/member-const';
  import { ATTEST_LEVEL_ENUM, DEVICE_STATUS_ENUM, DEVICE_STATUS_OPTIONS } from '/@/constants/business/member/device-const';
  import { solvelaSentry } from '/@/lib/solvela-sentry';
  import QueryActions from '/@/components/framework/query-actions/index.vue';
  import TablePagination from '/@/components/framework/table-pagination/index.vue';

  const columns = ref([
    { title: '设备号 / 端', dataIndex: 'deviceId', width: 170 },
    { title: '处置档 / 原因', dataIndex: 'status', width: 160 },
    { title: '签发IP / 归属地', dataIndex: 'registerIp', width: 190 },
    { title: '型号 / 系统（自报）', dataIndex: 'model', width: 190 },
    { title: '可信度', dataIndex: 'attestLevel', width: 110 },
    { title: '处置人', dataIndex: 'operator', width: 110 },
    { title: '最后活跃', dataIndex: 'lastActiveTime', width: 170, ellipsis: true },
    { title: '签发时间', dataIndex: 'createTime', width: 170, ellipsis: true },
    { title: '操作', dataIndex: 'action', width: 130, fixed: 'right' },
  ]);

  const memberColumns = ref([
    { title: '账号 / 会员号', dataIndex: 'memberName', width: 170 },
    { title: '昵称', dataIndex: 'nickname', width: 120, ellipsis: true },
    { title: '登录次数', dataIndex: 'loginCount', width: 90 },
    { title: '首次', dataIndex: 'firstLoginTime', width: 170, ellipsis: true },
    { title: '最近', dataIndex: 'lastLoginTime', width: 170, ellipsis: true },
  ]);

  function statusMeta(value) {
    return metaOf(DEVICE_STATUS_ENUM, value, { desc: value, color: 'default' });
  }

  function attestMeta(value) {
    return metaOf(ATTEST_LEVEL_ENUM, value, { desc: value, color: 'default' });
  }

  // ---------------------------- 查询 ----------------------------

  /**
   * 刻意<b>不</b>给默认时间范围。
   *
   * 与登录日志那个页面正好相反：那张表 append-only 且按月分区，不给范围就是全表扫描；
   * 而设备表一台设备只有一行、量小得多，运营打开它多半是为了查某一台或某个 IP，
   * 默认限成当天反而会让「昨天领的那台」查不出来。
   */
  const queryFormState = {
    deviceId: undefined,
    registerIp: undefined,
    deviceType: undefined,
    status: undefined,
    createTimeBegin: undefined,
    createTimeEnd: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const createTime = ref([]);
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function onChangeCreateTime(dates, dateStrings) {
    queryForm.createTimeBegin = dateStrings[0] || undefined;
    queryForm.createTimeEnd = dateStrings[1] || undefined;
  }

  function resetQuery() {
    const pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    createTime.value = [];
    queryData();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      const res = await deviceApi.queryPage(queryForm);
      tableData.value = res.list;
      total.value = res.total;
    } catch (e) {
      solvelaSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(queryData);

  // ---------------------------- 关联账号 ----------------------------

  const membersOpen = ref(false);
  const membersLoading = ref(false);
  const members = ref([]);
  const currentDeviceId = ref('');

  async function openMembers(record) {
    currentDeviceId.value = record.deviceId;
    members.value = [];
    membersOpen.value = true;
    membersLoading.value = true;
    try {
      members.value = (await deviceApi.listMembers(record.deviceId)) || [];
    } catch (e) {
      solvelaSentry.captureError(e);
    } finally {
      membersLoading.value = false;
    }
  }

  // ---------------------------- 人工处置 ----------------------------

  const disposeOpen = ref(false);
  const disposing = ref(false);
  const disposeForm = reactive({ deviceId: '', status: DEVICE_STATUS_ENUM.NORMAL.value, remark: '' });

  function openDispose(record) {
    disposeForm.deviceId = record.deviceId;
    // 默认停在【当前档位】，而不是默认选「封禁」——
    // 一个默认就指着最重那一档的弹窗，迟早会有人顺手点确定
    disposeForm.status = record.status;
    disposeForm.remark = '';
    disposeOpen.value = true;
  }

  async function submitDispose() {
    if (!disposeForm.remark || !disposeForm.remark.trim()) {
      // 前端先拦一次：服务端也校验，但让用户为了一句必填跑一趟往返没有道理
      message.warning('请填写处置原因');
      return;
    }
    disposing.value = true;
    try {
      const res = await deviceApi.dispose({
        deviceId: disposeForm.deviceId,
        status: disposeForm.status,
        remark: disposeForm.remark.trim(),
      });
      message.success(res || '已处置');
      disposeOpen.value = false;
      queryData();
    } catch (e) {
      solvelaSentry.captureError(e);
    } finally {
      disposing.value = false;
    }
  }
</script>

<style scoped>
  .cell-sub {
    color: rgba(0, 0, 0, 0.45);
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>
