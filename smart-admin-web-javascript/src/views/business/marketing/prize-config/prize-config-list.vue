<!--
  * 业务级-发奖规则与奖品明细表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 18:39:36
  * @Copyright  weolwo
-->
<template>
    <!---------- 查询表单form begin ----------->
    <a-form class="smart-query-form">
        <a-row class="smart-query-form-row">
            <a-form-item label="关联的上层奖励包ID (tPrizeGroup.id)" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.prizeGroupId" placeholder="关联的上层奖励包ID (tPrizeGroup.id)" />
            </a-form-item>
            <a-form-item label="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.prizeType" placeholder="【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL" />
            </a-form-item>
            <a-form-item label="奖品展示名称：如“双11特等奖”或“100积分”" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.prizeName" placeholder="奖品展示名称：如“双11特等奖”或“100积分”" />
            </a-form-item>
            <a-form-item label="绑定的底层优惠兜底配置ID (指向 tPromotionConfig)" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.promotionConfigId" placeholder="绑定的底层优惠兜底配置ID (指向 tPromotionConfig)" />
            </a-form-item>
            <a-form-item label="奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.prizeLevel" placeholder="奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)" />
            </a-form-item>
            <a-form-item label="【字典】状态：0-停用, 1-启用" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.status" placeholder="【字典】状态：0-停用, 1-启用" />
            </a-form-item>
            <a-form-item class="smart-query-form-item">
                <a-button type="primary" @click="onSearch">
                    <template #icon>
                        <SearchOutlined />
                    </template>
                    查询
                </a-button>
                <a-button @click="resetQuery" class="smart-margin-left10">
                    <template #icon>
                        <ReloadOutlined />
                    </template>
                    重置
                </a-button>
            </a-form-item>
        </a-row>
    </a-form>
    <!---------- 查询表单form end ----------->

    <a-card size="small" :bordered="false" :hoverable="true">
        <!---------- 表格操作行 begin ----------->
        <a-row class="smart-table-btn-block">
            <div class="smart-table-operate-block">
                <a-button @click="showForm" type="primary" size="small">
                    <template #icon>
                        <PlusOutlined />
                    </template>
                    新建
                </a-button>
                <a-button @click="confirmBatchDelete" type="primary" danger size="small" :disabled="selectedRowKeyList.length == 0">
                    <template #icon>
                        <DeleteOutlined />
                    </template>
                    批量删除
                </a-button>
            </div>
            <div class="smart-table-setting-block">
                <TableOperator v-model="columns" :tableId="null" :refresh="queryData" />
            </div>
        </a-row>
        <!---------- 表格操作行 end ----------->

        <!---------- 表格 begin ----------->
        <a-table
            size="small"
            :scroll="{ y: 800 }"
            :dataSource="tableData"
            :columns="columns"
            rowKey="id"
            bordered
            :loading="tableLoading"
            :pagination="false"
            :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
        >
            <template #bodyCell="{ text, record, column }">


                <template v-if="column.dataIndex === 'action'">
                    <div class="smart-table-operate">
                        <a-button @click="showForm(record)" type="link">编辑</a-button>
                        <a-button @click="onDelete(record)" danger type="link">删除</a-button>
                    </div>
                </template>
            </template>
        </a-table>
        <!---------- 表格 end ----------->

        <div class="smart-query-table-page">
            <a-pagination
                showSizeChanger
                showQuickJumper
                show-less-items
                :pageSizeOptions="PAGE_SIZE_OPTIONS"
                :defaultPageSize="queryForm.pageSize"
                v-model:current="queryForm.pageNum"
                v-model:pageSize="queryForm.pageSize"
                :total="total"
                @change="queryData"
                @showSizeChange="queryData"
                :show-total="(total) => `共${total}条`"
            />
        </div>

        <PrizeConfigForm  ref="formRef" @reloadList="queryData"/>

    </a-card>
</template>
<script setup>
    import { reactive, ref, onMounted } from 'vue';
    import { message, Modal } from 'ant-design-vue';
    import { SmartLoading } from '/@/components/framework/smart-loading';
    import { prizeConfigApi } from '/@/api/business/marketing/prize-config/prize-config-api';
    import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
    import { smartSentry } from '/@/lib/smart-sentry';
    import TableOperator from '/@/components/support/table-operator/index.vue';
    import PrizeConfigForm from './prize-config-form.vue';

    // ---------------------------- 表格列 ----------------------------

    const columns = ref([
        {
            title: 'id',
            dataIndex: 'id',
            ellipsis: true,
        },
        {
            title: '关联的上层奖励包ID (t_prize_group.id)',
            dataIndex: 'prizeGroupId',
            ellipsis: true,
        },
        {
            title: '绑定的底层优惠兜底配置ID (指向 t_promotion_config)',
            dataIndex: 'promotionConfigId',
            ellipsis: true,
        },
        {
            title: '【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL',
            dataIndex: 'prizeType',
            ellipsis: true,
        },
        {
            title: '奖品展示名称：如“双11特等奖”或“100积分”',
            dataIndex: 'prizeName',
            ellipsis: true,
        },
        {
            title: '【字典】发奖机制：FIXED, RANDOM, PROBABILITY',
            dataIndex: 'grantMode',
            ellipsis: true,
        },
        {
            title: '奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)',
            dataIndex: 'prizeLevel',
            ellipsis: true,
        },
        {
            title: '发放数量下限(固定发放时与上限一致)',
            dataIndex: 'prizeAmountMin',
            ellipsis: true,
        },
        {
            title: '发放数量上限',
            dataIndex: 'prizeAmountMax',
            ellipsis: true,
        },
        {
            title: '中奖概率(万分位)',
            dataIndex: 'probability',
            ellipsis: true,
        },
        {
            title: '前端展示排序权重',
            dataIndex: 'sortWeight',
            ellipsis: true,
        },
        {
            title: '【字典】状态：0-停用, 1-启用',
            dataIndex: 'status',
            ellipsis: true,
        },
        {
            title: '创建人',
            dataIndex: 'createBy',
            ellipsis: true,
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            ellipsis: true,
        },
        {
            title: '更新人',
            dataIndex: 'updateBy',
            ellipsis: true,
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            ellipsis: true,
        },
        {
            title: '操作',
            dataIndex: 'action',
            fixed: 'right',
            width: 90,
        },
    ]);

    // ---------------------------- 查询数据表单和方法 ----------------------------

    const queryFormState = {
        prizeGroupId: undefined, //关联的上层奖励包ID (tPrizeGroup.id)
        prizeType: undefined, //【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL
        prizeName: undefined, //奖品展示名称：如“双11特等奖”或“100积分”
        promotionConfigId: undefined, //绑定的底层优惠兜底配置ID (指向 tPromotionConfig)
        prizeLevel: undefined, //奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)
        status: undefined, //【字典】状态：0-停用, 1-启用
        pageNum: 1,
        pageSize: 10,
    };
    // 查询表单form
    const queryForm = reactive({ ...queryFormState });
    // 表格加载loading
    const tableLoading = ref(false);
    // 表格数据
    const tableData = ref([]);
    // 总数
    const total = ref(0);

    // 重置查询条件
    function resetQuery() {
        let pageSize = queryForm.pageSize;
        Object.assign(queryForm, queryFormState);
        queryForm.pageSize = pageSize;
        queryData();
    }

    // 搜索
    function onSearch(){
      queryForm.pageNum = 1;
      queryData();
    }

    // 查询数据
    async function queryData() {
        tableLoading.value = true;
        try {
            let queryResult = await prizeConfigApi.queryPage(queryForm);
            tableData.value = queryResult.data.list;
            total.value = queryResult.data.total;
        } catch (e) {
            smartSentry.captureError(e);
        } finally {
            tableLoading.value = false;
        }
    }


    onMounted(queryData);

    // ---------------------------- 添加/修改 ----------------------------
    const formRef = ref();

    function showForm(data) {
        formRef.value.show(data);
    }

    // ---------------------------- 单个删除 ----------------------------
    //确认删除
    function onDelete(data){
        Modal.confirm({
            title: '提示',
            content: '确定要删除选吗?',
            okText: '删除',
            okType: 'danger',
            onOk() {
                requestDelete(data);
            },
            cancelText: '取消',
            onCancel() {},
        });
    }

    //请求删除
    async function requestDelete(data){
        SmartLoading.show();
        try {
            let deleteForm = {
                goodsIdList: selectedRowKeyList.value,
            };
            await prizeConfigApi.delete(data.id);
            message.success('删除成功');
            queryData();
        } catch (e) {
            smartSentry.captureError(e);
        } finally {
            SmartLoading.hide();
        }
    }

    // ---------------------------- 批量删除 ----------------------------

    // 选择表格行
    const selectedRowKeyList = ref([]);

    function onSelectChange(selectedRowKeys) {
        selectedRowKeyList.value = selectedRowKeys;
    }

    // 批量删除
    function confirmBatchDelete() {
        Modal.confirm({
            title: '提示',
            content: '确定要批量删除这些数据吗?',
            okText: '删除',
            okType: 'danger',
            onOk() {
                requestBatchDelete();
            },
            cancelText: '取消',
            onCancel() {},
        });
    }

    //请求批量删除
    async function requestBatchDelete() {
        try {
            SmartLoading.show();
            await prizeConfigApi.batchDelete(selectedRowKeyList.value);
            message.success('删除成功');
            queryData();
        } catch (e) {
            smartSentry.captureError(e);
        } finally {
            SmartLoading.hide();
        }
    }
</script>
