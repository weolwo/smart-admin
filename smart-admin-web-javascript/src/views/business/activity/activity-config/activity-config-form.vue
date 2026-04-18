<!--
  * 活动配置
  *
  * @Author:    weolwo
  * @Date:      2026-04-18 19:31:49
  * @Copyright  weolwo
-->
<template>
  <a-modal :title="form.id ? '编辑' : '添加'" :width="800" :open="visibleFlag" @cancel="onClose" :maskClosable="false" :destroyOnClose="true">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="id" name="id">
        <a-input-number style="width: 100%" v-model:value="form.id" placeholder="id" />
      </a-form-item>
      <a-form-item label="租户id" name="tenantId">
        <SmartEnumSelect width="100%" v-model:value="form.tenantId" enum-name="" placeholder="租户id" />
      </a-form-item>
      <a-form-item label="活动编码" name="activityCode">
        <a-input style="width: 100%" v-model:value="form.activityCode" placeholder="活动编码" />
      </a-form-item>
      <a-form-item label="活动名称" name="activityName">
        <a-input style="width: 100%" v-model:value="form.activityName" placeholder="活动名称" />
      </a-form-item>
      <a-form-item label="活动类型" name="activityType">
        <a-input style="width: 100%" v-model:value="form.activityType" placeholder="活动类型" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-input-number style="width: 100%" v-model:value="form.status" placeholder="状态：0-未开始, 1-上线, 2-下线" />
      </a-form-item>
      <a-form-item label="活动开始时间" name="startTime">
        <a-date-picker show-time valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="form.startTime" style="width: 100%" placeholder="活动开始时间" />
      </a-form-item>
      <a-form-item label="活动结束时间" name="endTime">
        <a-date-picker show-time valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="form.endTime" style="width: 100%" placeholder="活动结束时间" />
      </a-form-item>
      <a-form-item label="规则脚本id" name="scriptId">
        <a-input style="width: 100%" v-model:value="form.scriptId" placeholder="规则脚本id" />
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
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { activityConfigApi } from '/@/api/business/activity/activity-config/activity-config-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

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
    tenantId: undefined, //租户id
    activityCode: undefined, //活动编码
    activityName: undefined, //活动名称
    activityType: undefined, //活动类型
    status: undefined, //状态：0-未开始, 1-上线, 2-下线
    startTime: undefined, //活动开始时间
    endTime: undefined, //活动结束时间
    scriptId: undefined, //规则脚本id
  };

  let form = reactive({ ...formDefault });

  const rules = {
    id: [{ required: true, message: 'id 必填' }],
    tenantId: [{ required: true, message: '租户id 必填' }],
    activityCode: [{ required: true, message: '活动编码 必填' }],
    activityName: [{ required: true, message: '活动名称 必填' }],
    startTime: [{ required: true, message: '活动开始时间 必填' }],
    endTime: [{ required: true, message: '活动结束时间 必填' }],
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
        await activityConfigApi.update(form);
      } else {
        await activityConfigApi.add(form);
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
