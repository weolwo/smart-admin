<!--
  * 资产域-优惠配置(预算与风控兜底)表
  *
  * @Author:    weolwo
  * @Date:      2026-04-03 18:44:52
  * @Copyright  weolwo
-->
<template>
    <!---------- 查询表单form begin ----------->
    <a-form class="smart-query-form">
        <a-row class="smart-query-form-row">
            <a-form-item label="【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.prizeType" placeholder="【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)" />
            </a-form-item>
            <a-form-item label="创建时间" class="smart-query-form-item">
                <a-range-picker v-model:value="queryForm.createTime" :presets="defaultTimeRanges" style="width: 200px" @change="onChangeCreateTime" />
            </a-form-item>
            <a-form-item label="【字典】状态：0-停用, 1-启用" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.status" placeholder="【字典】状态：0-停用, 1-启用" />
            </a-form-item>
            <a-form-item label="配置名称" class="smart-query-form-item">
                <a-input style="width: 200px" v-model:value="queryForm.promoName" placeholder="配置名称" />
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

        <PromotionConfigForm  ref="formRef" @reloadList="queryData"/>

    </a-card>
</template>
<script setup lang="ts">
    import { reactive, ref, onMounted } from 'vue';
    import { message, Modal } from 'ant-design-vue';
    import { SmartLoading } from '/@/components/framework/smart-loading';
    import { promotionConfigApi } from '/@/api/business/promotion-config/promotion-config-api';
    import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
    import { smartSentry } from '/@/lib/smart-sentry';
    import TableOperator from '/@/components/support/table-operator/index.vue';
    import PromotionConfigForm from './promotion-config-form.vue';
    import { defaultTimeRanges } from '/@/lib/default-time-ranges';

    // ---------------------------- 表格列 ----------------------------

    const columns = ref([
        {
            title: '配置ID，业务主键如 PRM_1001',
            dataIndex: 'id',
            ellipsis: true,
        },
        {
            title: '租户ID',
            dataIndex: 'tenantId',
            ellipsis: true,
        },
        {
            title: '优惠配置名称，如：双11十元券兜底配置',
            dataIndex: 'promoName',
            ellipsis: true,
        },
        {
            title: '【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)',
            dataIndex: 'prizeType',
            ellipsis: true,
        },
        {
            title: '总库存(个数)：-1为不限制(适用于券/实物)',
            dataIndex: 'totalQuota',
            ellipsis: true,
        },
        {
            title: '已消耗库存(个数)',
            dataIndex: 'usedQuota',
            ellipsis: true,
        },
        {
            title: '总预算(金额)：-1为不限制(适用于积分/现金)',
            dataIndex: 'totalAmount',
            ellipsis: true,
        },
        {
            title: '已消耗预算(金额)',
            dataIndex: 'usedAmount',
            ellipsis: true,
        },
        {
            title: '单次最大数量兜底，超限阻断',
            dataIndex: 'singleMaxQuota',
            ellipsis: true,
        },
        {
            title: '单次最大金额兜底，超限阻断',
            dataIndex: 'singleMaxAmount',
            ellipsis: true,
        },
        {
            title: '【字典】限制周期：LIFETIME(终身), DAILY(每日), WEEKLY(每周), MONTHLY(每月), CUSTOM',
            dataIndex: 'limitPeriod',
            ellipsis: true,
        },
        {
            title: '同周期内，单会员ID最多领取次数 (-1为不限)',
            dataIndex: 'identifyLimit',
            ellipsis: true,
        },
        {
            title: '同周期内，单手机号最多领取次数 (-1为不限)',
            dataIndex: 'phoneLimit',
            ellipsis: true,
        },
        {
            title: '同周期内，单IP地址最多领取次数 (-1为不限)',
            dataIndex: 'ipLimit',
            ellipsis: true,
        },
        {
            title: '同周期内，单设备硬件号(IMEI/IDFA)最多领取次数 (-1为不限)',
            dataIndex: 'deviceLimit',
            ellipsis: true,
        },
        {
            title: '同周期内，单客户端指纹最多领取次数 (-1为不限)',
            dataIndex: 'fingerprintLimit',
            ellipsis: true,
        },
        {
            title: '互斥规则：存互斥的优惠配置ID数组',
            dataIndex: 'mutexRule',
            ellipsis: true,
        },
        {
            title: '【字典】人工审核：0-免审, 1-笔笔审, 2-超阈值审',
            dataIndex: 'reviewType',
            ellipsis: true,
        },
        {
            title: '触发审核的阈值',
            dataIndex: 'reviewThreshold',
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
        prizeType: undefined, //【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)
        createTime: [], //创建时间
        createTimeBegin: undefined, //创建时间 开始
        createTimeEnd: undefined, //创建时间 结束
        status: undefined, //【字典】状态：0-停用, 1-启用
        promoName: undefined, //配置名称
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
            let queryResult = await promotionConfigApi.queryPage(queryForm);
            tableData.value = queryResult.data.list;
            total.value = queryResult.data.total;
        } catch (e) {
            smartSentry.captureError(e);
        } finally {
            tableLoading.value = false;
        }
    }

    function onChangeCreateTime(dates, dateStrings){
        queryForm.createTimeBegin = dateStrings[0];
        queryForm.createTimeEnd = dateStrings[1];
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
            await promotionConfigApi.delete(data.id);
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
            await promotionConfigApi.batchDelete(selectedRowKeyList.value);
            message.success('删除成功');
            queryData();
        } catch (e) {
            smartSentry.captureError(e);
        } finally {
            SmartLoading.hide();
        }
    }
</script>
