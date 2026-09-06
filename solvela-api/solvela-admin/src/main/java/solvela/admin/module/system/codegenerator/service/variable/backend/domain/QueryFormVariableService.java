package solvela.admin.module.system.codegenerator.service.variable.backend.domain;

import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaEnumUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.constant.CodeQueryFieldQueryTypeEnum;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeField;
import solvela.admin.module.system.codegenerator.domain.model.CodeQueryField;
import solvela.admin.module.system.codegenerator.service.variable.CodeGenerateBaseVariableService;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author 1024创新实验室-主任:卓大
 * @Date 2022/9/29 17:20:41
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */

public class QueryFormVariableService extends CodeGenerateBaseVariableService {


    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        Map<String, Object> variablesMap = new HashMap<>();
        ImmutablePair<List<String>, List<Map<String, Object>>> packageListAndFields = getPackageListAndFields(form);
        variablesMap.put("packageName", form.getBasic().getJavaPackageName() + ".domain.form");
        variablesMap.put("importPackageList", packageListAndFields.getLeft());
        variablesMap.put("fields", packageListAndFields.getRight());
        return variablesMap;
    }


    /**
     * 查询表单的字段与 import 列表。
     *
     * <p>每种查询方式（等值 / 日期 / 枚举 / 字典）决定了字段的 Java 类型、要加哪些注解、
     * 以及要补哪些 import —— 三件事绑在一起，所以按查询方式分而不是按「类型/注解/import」分。
     */
    public ImmutablePair<List<String>, List<Map<String, Object>>> getPackageListAndFields(CodeGeneratorConfigForm form) {
        HashSet<String> packageList = new HashSet<>();
        List<Map<String, Object>> finalFieldList = new ArrayList<>();

        for (CodeQueryField field : form.getQueryFields()) {
            CodeQueryFieldQueryTypeEnum queryTypeEnum =
                    SolvelaEnumUtil.getEnumByValue(field.getQueryTypeEnum(), CodeQueryFieldQueryTypeEnum.class);
            if (queryTypeEnum == null) {
                continue;
            }

            Map<String, Object> finalFieldMap = SolvelaBeanUtil.beanToMap(field);
            finalFieldMap.put("apiModelProperty", "@Schema(description = \"" + field.getLabel() + "\")");
            packageList.add("import io.swagger.v3.oas.annotations.media.Schema;");

            if (applyQueryType(queryTypeEnum, field, form, finalFieldMap, packageList)) {
                finalFieldList.add(finalFieldMap);
            }
        }

        // lombok
        packageList.add("import lombok.Data;");
        packageList.add("import lombok.EqualsAndHashCode;");

        List<String> packageNameList = packageList.stream().filter(Objects::nonNull).sorted().collect(Collectors.toList());
        return ImmutablePair.of(packageNameList, finalFieldList);
    }

    /**
     * 按查询方式填好这一个字段。
     *
     * @return false 表示这个字段该整个跳过（枚举查询却找不到对应的列）
     */
    private boolean applyQueryType(CodeQueryFieldQueryTypeEnum queryTypeEnum, CodeQueryField field,
                                   CodeGeneratorConfigForm form, Map<String, Object> finalFieldMap,
                                   HashSet<String> packageList) {
        switch (queryTypeEnum) {
            case EQUAL -> {
                CodeField codeField = getCodeFieldByColumnName(field.getColumnNameList().get(0), form);
                // 找不到列就退化成 String：生成的代码仍然编译得过，运营改一下配置即可
                finalFieldMap.put("javaType", codeField == null ? "String" : codeField.getJavaType());
            }
            case DATE_RANGE, DATE -> {
                packageList.add("import java.time.LocalDate;");
                finalFieldMap.put("javaType", "LocalDate");
            }
            case ENUM -> {
                return applyEnum(field, form, finalFieldMap, packageList);
            }
            case DICT -> applyDict(field, form, finalFieldMap, packageList);
            default -> finalFieldMap.put("javaType", "String");
        }
        return true;
    }

    /**
     * 枚举查询：带上 {@code @SchemaEnum} 与 {@code @CheckEnum}。
     *
     * <p>🔴 枚举类名没配时<b>降级成普通字段</b>而不是照拼 —— 照拼会生成
     * {@code import xxx.constant.null} 和 {@code null.class}，整个生成出来的模块编译不过，
     * 而报错位置指向生成代码，看不出根因在配置上。
     */
    private boolean applyEnum(CodeQueryField field, CodeGeneratorConfigForm form,
                              Map<String, Object> finalFieldMap, HashSet<String> packageList) {
        CodeField codeField = getCodeFieldByColumnName(field.getColumnNameList().get(0), form);
        if (codeField == null) {
            return false;
        }
        finalFieldMap.put("javaType", codeField.getJavaType());
        if (SolvelaStringUtil.isEmpty(codeField.getEnumName())) {
            return true;
        }

        packageList.add("import solvela.web.swagger.SchemaEnum;");
        packageList.add("import solvela.base.validation.enumeration.CheckEnum;");
        packageList.add("import " + form.getBasic().getJavaPackageName() + ".constant." + codeField.getEnumName() + ";");

        finalFieldMap.put("apiModelProperty",
                "@SchemaEnum(value = " + codeField.getEnumName() + ".class, desc = \"" + codeField.getLabel() + "\")");
        finalFieldMap.put("checkEnum",
                "@CheckEnum(value = " + codeField.getEnumName() + ".class, message = \"" + codeField.getLabel() + " 错误\")");
        finalFieldMap.put("isEnum", true);
        return true;
    }

    /** 字典查询：字段类型是 String，值由 {@code DictDataDeserializer} 在反序列化时翻译 */
    private void applyDict(CodeQueryField field, CodeGeneratorConfigForm form,
                           Map<String, Object> finalFieldMap, HashSet<String> packageList) {
        CodeField codeField = getCodeFieldByColumnName(field.getColumnNameList().get(0), form);
        if (codeField != null && SolvelaStringUtil.isNotEmpty(codeField.getDict())) {
            finalFieldMap.put("dict", "\n    @JsonDeserialize(using = DictDataDeserializer.class)");
            packageList.add("import tools.jackson.databind.annotation.JsonDeserialize;");
            packageList.add("import solvela.base.json.deserializer.DictDataDeserializer;");
        }
        finalFieldMap.put("javaType", "String");
    }

}
