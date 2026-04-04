<!--
  * 业务级-任务规则配置表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 16:51:54
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
        <a-form-item label="面向C端展示的任务名称"  name="taskName">
          <a-input style="width: 100%" v-model:value="form.taskName" placeholder="面向C端展示的任务名称" />
        </a-form-item>
        <a-form-item label="关联模板Code"  name="templateCode">
          <a-input style="width: 100%" v-model:value="form.templateCode" placeholder="关联模板Code" />
        </a-form-item>
        <a-form-item label="【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)"  name="triggerEvent">
          <a-input style="width: 100%" v-model:value="form.triggerEvent" placeholder="【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)" />
        </a-form-item>
        <a-form-item label="【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)"  name="taskGroup">
          <a-input style="width: 100%" v-model:value="form.taskGroup" placeholder="【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)" />
        </a-form-item>
        <a-form-item label="【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)"  name="targetAudience">
          <a-input style="width: 100%" v-model:value="form.targetAudience" placeholder="【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)" />
        </a-form-item>
        <a-form-item label="【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)"  name="limitType">
          <a-input style="width: 100%" v-model:value="form.limitType" placeholder="【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)" />
        </a-form-item>
        <a-form-item label="配合频次类型，限制次数"  name="limitCount">
          <a-input-number style="width: 100%" v-model:value="form.limitCount" placeholder="配合频次类型，限制次数" />
        </a-form-item>
        <a-form-item label="业务规则参数 JSON"  name="ruleConfig">
          <a-textarea style="width: 100%" v-model:value="form.ruleConfig" placeholder="业务规则参数 JSON" />
        </a-form-item>
        <a-form-item label="前端排序权重，越大越靠前"  name="sortWeight">
          <a-input-number style="width: 100%" v-model:value="form.sortWeight" placeholder="前端排序权重，越大越靠前" />
        </a-form-item>
        <a-form-item label="【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线"  name="status">
          <a-input-number style="width: 100%" v-model:value="form.status" placeholder="【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线" />
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
  import { taskConfigApi } from '/@/api/business/marketing/task-config/task-config-api';
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
      taskName: undefined, //面向C端展示的任务名称
      templateCode: undefined, //关联模板Code
      triggerEvent: undefined, //【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)
      taskGroup: undefined, //【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)
      targetAudience: undefined, //【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)
      limitType: undefined, //【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)
      limitCount: undefined, //配合频次类型，限制次数
      ruleConfig: undefined, //业务规则参数 JSON
      sortWeight: undefined, //前端排序权重，越大越靠前
      status: undefined, //【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线
  };

  let form = reactive({ ...formDefault });

  const rules = {
      id: [{ required: true, message: 'id 必填' }],
      taskName: [{ required: true, message: '面向C端展示的任务名称 必填' }],
      templateCode: [{ required: true, message: '关联模板Code 必填' }],
      triggerEvent: [{ required: true, message: '【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义) 必填' }],
      limitType: [{ required: true, message: '【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制) 必填' }],
      limitCount: [{ required: true, message: '配合频次类型，限制次数 必填' }],
      ruleConfig: [{ required: true, message: '业务规则参数 JSON 必填' }],
      sortWeight: [{ required: true, message: '前端排序权重，越大越靠前 必填' }],
      status: [{ required: true, message: '【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线 必填' }],
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
        await taskConfigApi.update(form);
      } else {
        await taskConfigApi.add(form);
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
