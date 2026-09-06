package solvela.admin.module.system.codegenerator.service.variable.front;

import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCaseFormat;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.constant.CodeFrontComponentEnum;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeField;
import solvela.admin.module.system.codegenerator.domain.model.CodeInsertAndUpdateField;
import solvela.admin.module.system.codegenerator.service.variable.CodeGenerateBaseVariableService;

import java.util.*;

/**
 * @Author 1024创新实验室-主任:卓大
 * @Date 2022/9/29 17:20:41
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */

public class FormVariableService extends CodeGenerateBaseVariableService {

    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    /**
     * 前端表单页的变量：每个字段的展示配置，以及这一页要 import 哪些组件。
     *
     * <p>两件事必须一起算：组件 import 是由字段的 frontComponent 决定的，
     * 分开算就得把字段再遍历一遍，而漏掉一个组件的表现是页面白屏
     *（Vue 解析不到未注册的组件）。
     */
    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        List<Map<String, Object>> fieldsVariableList = new ArrayList<>();
        HashSet<String> frontImportSet = new HashSet<>();

        for (CodeInsertAndUpdateField field : form.getInsertAndUpdate().getFieldList()) {
            // 既不参与新增也不参与编辑的列，表单上根本不出现
            if (!(field.getInsertFlag() || field.getUpdateFlag())) {
                continue;
            }
            CodeField codeField = getCodeFieldByColumnName(field.getColumnName(), form);
            if (codeField == null) {
                continue;
            }
            fieldsVariableList.add(toFieldVariables(field, codeField));
            collectComponentImports(field, frontImportSet);
        }

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("formFields", fieldsVariableList);
        variablesMap.put("frontImportList", new ArrayList<>(frontImportSet));
        return variablesMap;
    }

    /** 一个字段在模板里能用到的全部变量。枚举字段额外给一个大写下划线名，前端常量就叫那个 */
    private Map<String, Object> toFieldVariables(CodeInsertAndUpdateField field, CodeField codeField) {
        Map<String, Object> objectMap = SolvelaBeanUtil.beanToMap(field);
        objectMap.put("label", codeField.getLabel());
        objectMap.put("fieldName", codeField.getFieldName());
        objectMap.put("dict", codeField.getDict());
        if (SolvelaStringUtil.isNotBlank(codeField.getEnumName())) {
            objectMap.put("upperUnderscoreEnum",
                    SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.UPPER_UNDERSCORE, codeField.getEnumName()));
        }
        return objectMap;
    }

    /**
     * 这个字段的前端组件需要哪些 import。
     *
     * <p>用 Set 收集：同一种组件被多个字段用到时只 import 一次 ——
     * 重复的 import 在 Vue SFC 里是编译错误，不是警告。
     */
    private void collectComponentImports(CodeInsertAndUpdateField field, HashSet<String> frontImportSet) {
        String component = field.getFrontComponent();
        if (CodeFrontComponentEnum.ENUM_SELECT.equalsValue(component)) {
            frontImportSet.add("import SolvelaEnumSelect from '/@/components/framework/solvela-enum-select/index.vue';");
        }
        if (CodeFrontComponentEnum.BOOLEAN_SELECT.equalsValue(component)) {
            frontImportSet.add("import BooleanSelect from '/@/components/framework/boolean-select/index.vue';");
        }
        if (CodeFrontComponentEnum.DICT_SELECT.equalsValue(component)) {
            frontImportSet.add("import DictSelect from '/@/components/support/dict-select/index.vue';");
            frontImportSet.add("import { DICT_CODE_ENUM } from '/@/constants/support/dict-const.js';");
        }
        if (CodeFrontComponentEnum.FILE_UPLOAD.equalsValue(component)) {
            frontImportSet.add("import { FILE_FOLDER_TYPE_ENUM } from '/@/constants/support/file-const';");
            frontImportSet.add("import FileUpload from '/@/components/support/file-upload/index.vue';");
        }
    }
}
