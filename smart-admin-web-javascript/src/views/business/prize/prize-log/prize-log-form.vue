<!--
  * 资产域-奖励发放执行明细与快照表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 18:42:42
  * @Copyright  weolwo
-->
<template>
  <a-modal :title="form.id ? '编辑' : '添加'" :width="800" :open="visibleFlag" @cancel="onClose" :maskClosable="false" :destroyOnClose="true">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="id" name="id">
        <a-input-number style="width: 100%" v-model:value="form.id" placeholder="id" />
      </a-form-item>
      <a-form-item label="租户ID" name="tenantId">
        <a-input style="width: 100%" v-model:value="form.tenantId" placeholder="租户ID" />
      </a-form-item>
      <a-form-item label="关联的提案ID (t_promotion_proposal.id)" name="proposalId">
        <a-input-number style="width: 100%" v-model:value="form.proposalId" placeholder="关联的提案ID (t_promotion_proposal.id)" />
      </a-form-item>
      <a-form-item label="会员名" name="memberName">
        <a-input style="width: 100%" v-model:value="form.memberName" placeholder="会员名" />
      </a-form-item>
      <a-form-item label="关联的大礼包ID" name="prizeGroupId">
        <a-input style="width: 100%" v-model:value="form.prizeGroupId" placeholder="关联的大礼包ID" />
      </a-form-item>
      <a-form-item label="触发此发放的具体奖品明细项ID (t_prize_config.id)" name="prizeConfigId">
        <a-input-number style="width: 100%" v-model:value="form.prizeConfigId" placeholder="触发此发放的具体奖品明细项ID (t_prize_config.id)" />
      </a-form-item>
      <a-form-item label="实际扣减的兜底优惠池ID" name="promotionConfigId">
        <a-input style="width: 100%" v-model:value="form.promotionConfigId" placeholder="实际扣减的兜底优惠池ID" />
      </a-form-item>
      <a-form-item label="奖品级别：1(一等奖), 0(无级别)" name="prizeLevel">
        <a-input-number style="width: 100%" v-model:value="form.prizeLevel" placeholder="奖品级别：1(一等奖), 0(无级别)" />
      </a-form-item>
      <a-form-item label="奖品名称快照" name="prizeName">
        <a-input style="width: 100%" v-model:value="form.prizeName" placeholder="奖品名称快照" />
      </a-form-item>
      <a-form-item label="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL" name="prizeType">
        <a-input style="width: 100%" v-model:value="form.prizeType" placeholder="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL" />
      </a-form-item>
      <a-form-item label="发放的具体值(积分数/券ID)" name="prizeValue">
        <a-input style="width: 100%" v-model:value="form.prizeValue" placeholder="发放的具体值(积分数/券ID)" />
      </a-form-item>
      <a-form-item label="【字典】执行状态：0-发放中, 1-成功, 2-失败" name="status">
        <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】执行状态：0-发放中, 1-成功, 2-失败" />
      </a-form-item>
      <a-form-item label="下游外部单号" name="externalBizNo">
        <a-input style="width: 100%" v-model:value="form.externalBizNo" placeholder="下游外部单号" />
      </a-form-item>
      <a-form-item label="异常原因" name="remark">
        <a-input style="width: 100%" v-model:value="form.remark" placeholder="异常原因" />
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
  import { prizeLogApi } from '/src/api/business/prize/prize-log/prize-log-api';
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
    proposalId: undefined, //关联的提案ID (t_promotion_proposal.id)
    memberName: undefined, //会员名
    prizeGroupId: undefined, //关联的大礼包ID
    prizeConfigId: undefined, //触发此发放的具体奖品明细项ID (t_prize_config.id)
    promotionConfigId: undefined, //实际扣减的兜底优惠池ID
    prizeLevel: undefined, //奖品级别：1(一等奖), 0(无级别)
    prizeName: undefined, //奖品名称快照
    prizeType: undefined, //【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL
    prizeValue: undefined, //发放的具体值(积分数/券ID)
    status: undefined, //【字典】执行状态：0-发放中, 1-成功, 2-失败
    externalBizNo: undefined, //下游外部单号
    remark: undefined, //异常原因
  };

  let form = reactive({ ...formDefault });

  const rules = {
    id: [{ required: true, message: 'id 必填' }],
    tenantId: [{ required: true, message: '租户ID 必填' }],
    proposalId: [{ required: true, message: '关联的提案ID (t_promotion_proposal.id) 必填' }],
    memberName: [{ required: true, message: '会员名 必填' }],
    prizeGroupId: [{ required: true, message: '关联的大礼包ID 必填' }],
    prizeConfigId: [{ required: true, message: '触发此发放的具体奖品明细项ID (t_prize_config.id) 必填' }],
    promotionConfigId: [{ required: true, message: '实际扣减的兜底优惠池ID 必填' }],
    prizeLevel: [{ required: true, message: '奖品级别：1(一等奖), 0(无级别) 必填' }],
    prizeName: [{ required: true, message: '奖品名称快照 必填' }],
    prizeType: [{ required: true, message: '【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL 必填' }],
    prizeValue: [{ required: true, message: '发放的具体值(积分数/券ID) 必填' }],
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
        await prizeLogApi.update(form);
      } else {
        await prizeLogApi.add(form);
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
