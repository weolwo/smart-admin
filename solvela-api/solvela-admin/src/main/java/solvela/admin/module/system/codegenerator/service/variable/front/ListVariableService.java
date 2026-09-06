package solvela.admin.module.system.codegenerator.service.variable.front;

import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCaseFormat;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.constant.CodeFrontComponentEnum;
import solvela.admin.module.system.codegenerator.constant.CodeQueryFieldQueryTypeEnum;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeField;
import solvela.admin.module.system.codegenerator.domain.model.CodeInsertAndUpdateField;
import solvela.admin.module.system.codegenerator.domain.model.CodeQueryField;
import solvela.admin.module.system.codegenerator.domain.model.CodeTableField;
import solvela.admin.module.system.codegenerator.service.variable.CodeGenerateBaseVariableService;

import java.util.*;

/**
 * @Author 1024创新实验室-主任:卓大
 * @Date 2022/9/29 17:20:41
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */

public class ListVariableService extends CodeGenerateBaseVariableService {

    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    /**
     * 前端列表页的变量：查询条件区、表格列、以及这一页要 import 哪些组件。
     *
     * <p>组件 import 由字段配置反推出来，所以必须和字段一起遍历 —— 分开算就要再走一遍，
     * 而漏掉一个组件的表现是页面白屏（Vue 解析不到未注册的组件），不是报错。
     */
    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        HashSet<String> frontImportSet = new HashSet<>();
        String upperCamel = SolvelaCaseFormat.LOWER_CAMEL.to(SolvelaCaseFormat.UPPER_CAMEL, form.getBasic().getModuleName());
        String lowerHyphen = SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.LOWER_HYPHEN, form.getBasic().getModuleName());
        frontImportSet.add("import " + upperCamel + "Form from './" + lowerHyphen + "-form.vue';");

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("queryFields", buildQueryVariables(form, frontImportSet));
        variablesMap.put("listFields", buildListVariables(form, frontImportSet));
        variablesMap.put("frontImportList", new ArrayList<>(frontImportSet));
        return variablesMap;
    }

    /** 查询条件区：查询方式决定用哪个筛选组件（枚举下拉 / 字典下拉 / 日期区间） */
    private List<Map<String, Object>> buildQueryVariables(CodeGeneratorConfigForm form, HashSet<String> frontImportSet) {
        List<Map<String, Object>> queryVariable = new ArrayList<>();
        for (CodeQueryField queryField : form.getQueryFields()) {
            Map<String, Object> objectMap = SolvelaBeanUtil.beanToMap(queryField);
            CodeField codeField = getCodeFieldByColumnName(queryField.getColumnNameList().get(0), form);

            if (CodeQueryFieldQueryTypeEnum.ENUM.equalsValue(queryField.getQueryTypeEnum())
                    && codeField != null && SolvelaStringUtil.isNotBlank(codeField.getEnumName())) {
                objectMap.put("frontEnumName",
                        SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.UPPER_UNDERSCORE, codeField.getEnumName()));
                frontImportSet.add("import SolvelaEnumSelect from '/@/components/framework/solvela-enum-select/index.vue';");
            }
            if (CodeQueryFieldQueryTypeEnum.DICT.equalsValue(queryField.getQueryTypeEnum()) && codeField != null) {
                objectMap.put("dict", codeField.getDict());
                frontImportSet.add("import DictSelect from '/@/components/support/dict-select/index.vue';");
            }
            if (CodeQueryFieldQueryTypeEnum.DATE_RANGE.equalsValue(queryField.getQueryTypeEnum())) {
                frontImportSet.add("import { defaultTimeRanges } from '/@/lib/default-time-ranges';");
            }
            queryVariable.add(objectMap);
        }
        return queryVariable;
    }

    /**
     * 表格列。只取 {@code showFlag} 为真的列。
     *
     * <p>枚举列渲染成中文描述（{@code $solvelaEnumPlugin}）、字典列渲染成字典标签、
     * 文件列渲染成预览组件 —— 三者都是「库里存的值不是给人看的」这同一件事的不同形态。
     */
    private List<Map<String, Object>> buildListVariables(CodeGeneratorConfigForm form, HashSet<String> frontImportSet) {
        List<Map<String, Object>> listVariable = new ArrayList<>();
        for (CodeTableField tableField : form.getTableFields().stream().filter(CodeTableField::getShowFlag).toList()) {
            CodeField codeField = getCodeFieldByColumnName(tableField.getColumnName(), form);
            if (codeField == null) {
                continue;
            }
            Map<String, Object> objectMap = SolvelaBeanUtil.beanToMap(tableField);
            objectMap.put("fieldName", tableField.getFieldName());

            if (SolvelaStringUtil.isNotBlank(codeField.getEnumName())) {
                String upperUnderscoreEnum =
                        SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.UPPER_UNDERSCORE, codeField.getEnumName());
                objectMap.put("frontEnumPlugin", "$solvelaEnumPlugin.getDescByValue('" + upperUnderscoreEnum + "', text)");
            }
            if (SolvelaStringUtil.isNotBlank(codeField.getDict())) {
                objectMap.put("dict", codeField.getDict());
                frontImportSet.add("import { DICT_CODE_ENUM } from '/@/constants/support/dict-const.js';");
                frontImportSet.add("import DictLabel from '/@/components/support/dict-label/index.vue';");
            }

            /*
             * 🔴 这一列在新增/编辑里配的是什么组件，决定了列表里怎么渲染它。
             * 找不到对应配置就【整列不显示】—— 保持原行为：一个只读不可编辑的列，
             * 模板里没有可用的渲染方式，硬渲染出来会是一个裸的 fileId。
             */
            CodeInsertAndUpdateField insertField = form.getInsertAndUpdate().getFieldList().stream()
                    .filter(e -> SolvelaStringUtil.equals(tableField.getColumnName(), e.getColumnName()))
                    .findFirst().orElse(null);
            if (insertField == null) {
                continue;
            }
            if (CodeFrontComponentEnum.FILE_UPLOAD.equalsValue(insertField.getFrontComponent())) {
                objectMap.put("frontComponent", insertField.getFrontComponent());
                frontImportSet.add("import FilePreview from '/@/components/support/file-preview/index.vue';");
            }
            listVariable.add(objectMap);
        }
        return listVariable;
    }
}
