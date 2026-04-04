<!--
  * 资产域-统一发奖提案控制表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 18:46:32
  * @Copyright  weolwo
-->
<template>
  <a-modal
      :title="form.id ? '编辑' : '添加'"
      :width="100"
      :open="visibleFlag"
      @cancel="onClose"
      :maskClosable="false"
      :destroyOnClose="true"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" >
        <a-form-item label="id"  name="id">
          <a-input-number style="width: 100%" v-model:value="form.id" placeholder="id" />
        </a-form-item>
        <a-form-item label="会员名"  name="memberName">
          <a-input style="width: 100%" v-model:value="form.memberName" placeholder="会员名" />
        </a-form-item>
        <a-form-item label="来源任务实例ID"  name="taskRecordId">
          <a-input-number style="width: 100%" v-model:value="form.taskRecordId" placeholder="来源任务实例ID" />
        </a-form-item>
        <a-form-item label="关联的优惠配置ID"  name="promotionConfigId">
          <a-input style="width: 100%" v-model:value="form.promotionConfigId" placeholder="关联的优惠配置ID" />
        </a-form-item>
        <a-form-item label="【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截"  name="status">
          <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截" />
        </a-form-item>
        <a-form-item label="对应任务的哪个阶段"  name="stageLevel">
          <a-input-number style="width: 100%" v-model:value="form.stageLevel" placeholder="对应任务的哪个阶段" />
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
  import { promotionProposalApi } from '/@/api/business/marketing/promotion-proposal/promotion-proposal-api';
  import { smartSentry } from '/@/lib/smart-sentry';

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
      memberName: undefined, //会员名
      taskRecordId: undefined, //来源任务实例ID
      promotionConfigId: undefined, //关联的优惠配置ID
      status: undefined, //【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截
      stageLevel: undefined, //对应任务的哪个阶段
  };

  let form = reactive({ ...formDefault });

  const rules = {
      id: [{ required: true, message: 'id 必填' }],
      memberName: [{ required: true, message: '会员名 必填' }],
      taskRecordId: [{ required: true, message: '来源任务实例ID 必填' }],
      promotionConfigId: [{ required: true, message: '关联的优惠配置ID 必填' }],
      status: [{ required: true, message: '【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截 必填' }],
      stageLevel: [{ required: true, message: '对应任务的哪个阶段 必填' }],
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
        await promotionProposalApi.update(form);
      } else {
        await promotionProposalApi.add(form);
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
