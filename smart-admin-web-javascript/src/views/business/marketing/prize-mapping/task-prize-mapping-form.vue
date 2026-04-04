<!--
  * 业务级-任务阶段与奖励包映射表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 17:01:05
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
        <a-form-item label="关联的任务配置ID (t_task_config.id)"  name="taskConfigId">
          <a-input-number style="width: 100%" v-model:value="form.taskConfigId" placeholder="关联的任务配置ID (t_task_config.id)" />
        </a-form-item>
        <a-form-item label="任务阶段：单次任务填1，阶梯任务填 1, 2, 3..."  name="stageLevel">
          <a-input-number style="width: 100%" v-model:value="form.stageLevel" placeholder="任务阶段：单次任务填1，阶梯任务填 1, 2, 3..." />
        </a-form-item>
        <a-form-item label="达标阈值快照：如 "100.00" 或 "3""  name="stageThreshold">
          <a-input style="width: 100%" v-model:value="form.stageThreshold" placeholder="达标阈值快照：如 "100.00" 或 "3"" />
        </a-form-item>
        <a-form-item label="触发的奖励包ID (t_prize_group.id)"  name="prizeGroupId">
          <a-input style="width: 100%" v-model:value="form.prizeGroupId" placeholder="触发的奖励包ID (t_prize_group.id)" />
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
  import { taskPrizeMappingApi } from '/@/api/business/marketing/task-prize-mapping/task-prize-mapping-api';
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
      taskConfigId: undefined, //关联的任务配置ID (t_task_config.id)
      stageLevel: undefined, //任务阶段：单次任务填1，阶梯任务填 1, 2, 3...
      stageThreshold: undefined, //达标阈值快照：如 "100.00" 或 "3"
      prizeGroupId: undefined, //触发的奖励包ID (t_prize_group.id)
  };

  let form = reactive({ ...formDefault });

  const rules = {
      id: [{ required: true, message: 'id 必填' }],
      taskConfigId: [{ required: true, message: '关联的任务配置ID (t_task_config.id) 必填' }],
      stageLevel: [{ required: true, message: '任务阶段：单次任务填1，阶梯任务填 1, 2, 3... 必填' }],
      stageThreshold: [{ required: true, message: '达标阈值快照：如 "100.00" 或 "3" 必填' }],
      prizeGroupId: [{ required: true, message: '触发的奖励包ID (t_prize_group.id) 必填' }],
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
        await taskPrizeMappingApi.update(form);
      } else {
        await taskPrizeMappingApi.add(form);
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
