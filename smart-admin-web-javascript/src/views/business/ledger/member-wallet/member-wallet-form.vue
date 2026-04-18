<!--
  * 账务域-会员资金/积分主账表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 17:17:33
  * @Copyright  weolwo
-->
<template>
  <a-modal :title="form.id ? '编辑' : '添加'" :width="100" :open="visibleFlag" @cancel="onClose" :maskClosable="false" :destroyOnClose="true">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="if" name="id">
        <a-input-number style="width: 100%" v-model:value="form.id" placeholder="if" />
      </a-form-item>
      <a-form-item label="租户ID" name="tenantId">
        <a-input style="width: 100%" v-model:value="form.tenantId" placeholder="租户ID" />
      </a-form-item>
      <a-form-item label="会员名" name="memberName">
        <a-input style="width: 100%" v-model:value="form.memberName" placeholder="会员名" />
      </a-form-item>
      <a-form-item label="积分余额" name="scoreBalance">
        <a-input-number style="width: 100%" v-model:value="form.scoreBalance" placeholder="积分余额" />
      </a-form-item>
      <a-form-item label="现金余额" name="cashBalance">
        <a-input-number style="width: 100%" v-model:value="form.cashBalance" placeholder="现金余额" />
      </a-form-item>
      <a-form-item label="【字典】状态：0-冻结, 1-正常" name="status">
        <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】状态：0-冻结, 1-正常" />
      </a-form-item>
      <a-form-item label="乐观锁版本号" name="version">
        <a-input-number style="width: 100%" v-model:value="form.version" placeholder="乐观锁版本号" />
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
  import { memberWalletApi } from '/src/api/business/ledger/member-wallet/member-wallet-api';
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
    id: undefined, //if
    tenantId: undefined, //租户ID
    memberName: undefined, //会员名
    scoreBalance: undefined, //积分余额
    cashBalance: undefined, //现金余额
    status: undefined, //【字典】状态：0-冻结, 1-正常
    version: undefined, //乐观锁版本号
  };

  let form = reactive({ ...formDefault });

  const rules = {
    id: [{ required: true, message: 'if 必填' }],
    tenantId: [{ required: true, message: '租户ID 必填' }],
    memberName: [{ required: true, message: '会员名 必填' }],
    scoreBalance: [{ required: true, message: '积分余额 必填' }],
    cashBalance: [{ required: true, message: '现金余额 必填' }],
    status: [{ required: true, message: '【字典】状态：0-冻结, 1-正常 必填' }],
    version: [{ required: true, message: '乐观锁版本号 必填' }],
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
        await memberWalletApi.update(form);
      } else {
        await memberWalletApi.add(form);
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
