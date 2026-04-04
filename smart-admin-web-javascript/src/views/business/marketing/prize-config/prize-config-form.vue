<!--
  * 业务级-发奖规则与奖品明细表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 18:39:36
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
        <a-form-item label="关联的上层奖励包ID (t_prize_group.id)"  name="prizeGroupId">
          <a-input style="width: 100%" v-model:value="form.prizeGroupId" placeholder="关联的上层奖励包ID (t_prize_group.id)" />
        </a-form-item>
        <a-form-item label="绑定的底层优惠兜底配置ID (指向 t_promotion_config)"  name="promotionConfigId">
          <a-input style="width: 100%" v-model:value="form.promotionConfigId" placeholder="绑定的底层优惠兜底配置ID (指向 t_promotion_config)" />
        </a-form-item>
        <a-form-item label="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL"  name="prizeType">
          <a-input style="width: 100%" v-model:value="form.prizeType" placeholder="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL" />
        </a-form-item>
        <a-form-item label="奖品展示名称：如“双11特等奖”或“100积分”"  name="prizeName">
          <a-input style="width: 100%" v-model:value="form.prizeName" placeholder="奖品展示名称：如“双11特等奖”或“100积分”" />
        </a-form-item>
        <a-form-item label="【字典】发奖机制：FIXED, RANDOM, PROBABILITY"  name="grantMode">
          <a-input style="width: 100%" v-model:value="form.grantMode" placeholder="【字典】发奖机制：FIXED, RANDOM, PROBABILITY" />
        </a-form-item>
        <a-form-item label="奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)"  name="prizeLevel">
          <a-input-number style="width: 100%" v-model:value="form.prizeLevel" placeholder="奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)" />
        </a-form-item>
        <a-form-item label="发放数量下限(固定发放时与上限一致)"  name="prizeAmountMin">
          <a-input-number style="width: 100%" v-model:value="form.prizeAmountMin" placeholder="发放数量下限(固定发放时与上限一致)" />
        </a-form-item>
        <a-form-item label="发放数量上限"  name="prizeAmountMax">
          <a-input-number style="width: 100%" v-model:value="form.prizeAmountMax" placeholder="发放数量上限" />
        </a-form-item>
        <a-form-item label="前端展示排序权重"  name="sortWeight">
          <a-input-number style="width: 100%" v-model:value="form.sortWeight" placeholder="前端展示排序权重" />
        </a-form-item>
        <a-form-item label="【字典】状态：0-停用, 1-启用"  name="status">
          <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】状态：0-停用, 1-启用" />
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
  import { prizeConfigApi } from '/@/api/business/marketing/prize-config/prize-config-api';
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
      prizeGroupId: undefined, //关联的上层奖励包ID (t_prize_group.id)
      promotionConfigId: undefined, //绑定的底层优惠兜底配置ID (指向 t_promotion_config)
      prizeType: undefined, //【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL
      prizeName: undefined, //奖品展示名称：如“双11特等奖”或“100积分”
      grantMode: undefined, //【字典】发奖机制：FIXED, RANDOM, PROBABILITY
      prizeLevel: undefined, //奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)
      prizeAmountMin: undefined, //发放数量下限(固定发放时与上限一致)
      prizeAmountMax: undefined, //发放数量上限
      sortWeight: undefined, //前端展示排序权重
      status: undefined, //【字典】状态：0-停用, 1-启用
  };

  let form = reactive({ ...formDefault });

  const rules = {
      id: [{ required: true, message: 'id 必填' }],
      prizeGroupId: [{ required: true, message: '关联的上层奖励包ID (t_prize_group.id) 必填' }],
      promotionConfigId: [{ required: true, message: '绑定的底层优惠兜底配置ID (指向 t_promotion_config) 必填' }],
      prizeType: [{ required: true, message: '【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL 必填' }],
      prizeName: [{ required: true, message: '奖品展示名称：如“双11特等奖”或“100积分” 必填' }],
      grantMode: [{ required: true, message: '【字典】发奖机制：FIXED, RANDOM, PROBABILITY 必填' }],
      prizeLevel: [{ required: true, message: '奖品级别：1(一等奖), 2(二等奖), 0(普通奖品) 必填' }],
      prizeAmountMin: [{ required: true, message: '发放数量下限(固定发放时与上限一致) 必填' }],
      prizeAmountMax: [{ required: true, message: '发放数量上限 必填' }],
      sortWeight: [{ required: true, message: '前端展示排序权重 必填' }],
      status: [{ required: true, message: '【字典】状态：0-停用, 1-启用 必填' }],
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
        await prizeConfigApi.update(form);
      } else {
        await prizeConfigApi.add(form);
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
