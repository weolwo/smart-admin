<!--
  * 运行时-会员任务进度实例表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 17:03:33
  * @Copyright  weolwo
-->
<template>
  <a-modal :title="form.id ? '编辑' : '添加'" :width="100" :open="visibleFlag" @cancel="onClose" :maskClosable="false" :destroyOnClose="true">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="id" name="id">
        <a-input-number style="width: 100%" v-model:value="form.id" placeholder="id" />
      </a-form-item>
      <a-form-item label="租户ID" name="tenantId">
        <a-input style="width: 100%" v-model:value="form.tenantId" placeholder="租户ID" />
      </a-form-item>
      <a-form-item label="会员名" name="memberName">
        <a-input style="width: 100%" v-model:value="form.memberName" placeholder="会员名" />
      </a-form-item>
      <a-form-item label="关联的任务配置ID" name="taskConfigId">
        <a-input-number style="width: 100%" v-model:value="form.taskConfigId" placeholder="关联的任务配置ID" />
      </a-form-item>
      <a-form-item label="业务期数标识(防重用)：NONE, 日期(20260402)" name="periodKey">
        <a-input style="width: 100%" v-model:value="form.periodKey" placeholder="业务期数标识(防重用)：NONE, 日期(20260402)" />
      </a-form-item>
      <a-form-item label="该实例生效开始时间" name="validStartTime">
        <a-date-picker
          show-time
          valueFormat="YYYY-MM-DD HH:mm:ss"
          v-model:value="form.validStartTime"
          style="width: 100%"
          placeholder="该实例生效开始时间"
        />
      </a-form-item>
      <a-form-item label="该实例失效过期时间" name="validEndTime">
        <a-date-picker
          show-time
          valueFormat="YYYY-MM-DD HH:mm:ss"
          v-model:value="form.validEndTime"
          style="width: 100%"
          placeholder="该实例失效过期时间"
        />
      </a-form-item>
      <a-form-item label="当前进度值：如已签到 3.0000 天" name="currentMetric">
        <a-input-number style="width: 100%" v-model:value="form.currentMetric" placeholder="当前进度值：如已签到 3.0000 天" />
      </a-form-item>
      <a-form-item label="【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期" name="status">
        <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期" />
      </a-form-item>
      <a-form-item label="接取任务时的规则快照" name="ruleSnapshot">
        <a-textarea style="width: 100%" v-model:value="form.ruleSnapshot" placeholder="接取任务时的规则快照" />
      </a-form-item>
      <a-form-item label="接取任务时的奖励快照" name="prizeSnapshot">
        <a-textarea style="width: 100%" v-model:value="form.prizeSnapshot" placeholder="接取任务时的奖励快照" />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="onClose">取消</a-button>
        <a-button type="primary" @click="onSubmit">保存</a-button>
      </a-space>
    </template>
  </a-modal>
</template>
<script setup>
  import { reactive, ref, nextTick } from 'vue';
  import _ from 'lodash';
  import { message } from 'ant-design-vue';
  import { SmartLoading } from '/src/components/framework/smart-loading';
  import { taskRecordApi } from '/src/api/business/task/task-record/task-record-api';
  import { smartSentry } from '/src/lib/smart-sentry';

  // ------------------------ 事件 ------------------------

  const emits = defineEmits(['reloadList']);

  // ------------------------ 显示与隐藏 ------------------------
  // 是否显示
  const visibleFlag = ref(false);

  function show(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    // 使用字典时把下面这注释修改成自己的字典字段 有多个字典字段就复制多份同理修改 不然打开表单时不显示字典初始值
    // if (form.status && form.status.length > 0) {
    //   form.status = form.status.map((e) => e.valueCode);
    // }
    visibleFlag.value = true;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onClose() {
    Object.assign(form, formDefault);
    visibleFlag.value = false;
  }

  // ------------------------ 表单 ------------------------

  // 组件ref
  const formRef = ref();

  const formDefault = {
    id: undefined, //id
    tenantId: undefined, //租户ID
    memberName: undefined, //会员名
    taskConfigId: undefined, //关联的任务配置ID
    periodKey: undefined, //业务期数标识(防重用)：NONE, 日期(20260402)
    validStartTime: undefined, //该实例生效开始时间
    validEndTime: undefined, //该实例失效过期时间
    currentMetric: undefined, //当前进度值：如已签到 3.0000 天
    status: undefined, //【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期
    ruleSnapshot: undefined, //接取任务时的规则快照
    prizeSnapshot: undefined, //接取任务时的奖励快照
  };

  let form = reactive({ ...formDefault });

  const rules = {
    id: [{ required: true, message: 'id 必填' }],
    tenantId: [{ required: true, message: '租户ID 必填' }],
    memberName: [{ required: true, message: '会员名 必填' }],
    taskConfigId: [{ required: true, message: '关联的任务配置ID 必填' }],
    periodKey: [{ required: true, message: '业务期数标识(防重用)：NONE, 日期(20260402) 必填' }],
    validStartTime: [{ required: true, message: '该实例生效开始时间 必填' }],
    validEndTime: [{ required: true, message: '该实例失效过期时间 必填' }],
    ruleSnapshot: [{ required: true, message: '接取任务时的规则快照 必填' }],
    prizeSnapshot: [{ required: true, message: '接取任务时的奖励快照 必填' }],
  };

  // 点击确定，验证表单
  async function onSubmit() {
    try {
      await formRef.value.validateFields();
      save();
    } catch (err) {
      message.error('参数验证错误，请仔细填写表单数据!');
    }
  }

  // 新建、编辑API
  async function save() {
    SmartLoading.show();
    try {
      if (form.id) {
        await taskRecordApi.update(form);
      } else {
        await taskRecordApi.add(form);
      }
      message.success('操作成功');
      emits('reloadList');
      onClose();
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      SmartLoading.hide();
    }
  }

  defineExpose({
    show,
  });
</script>
