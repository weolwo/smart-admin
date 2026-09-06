package solvela.admin.module.system.codegenerator.service.variable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCaseFormat;
import solvela.base.util.SolvelaCollectionUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.constant.CodeFrontComponentEnum;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeField;
import solvela.admin.module.system.codegenerator.domain.model.CodeInsertAndUpdate;
import solvela.admin.module.system.codegenerator.domain.model.CodeInsertAndUpdateField;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author 1024创新实验室-主任:卓大
 * @Date 2022/9/29 17:20:41
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */
public abstract class CodeGenerateBaseVariableService {

    public abstract Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form);

    /**
     * 是否支持 :
     * 1、增加、修改
     * 2、删除
     *
     * @param form
     * @return
     */
    public abstract boolean isSupport(CodeGeneratorConfigForm form);

    /**
     * 获取所有javabean的 import 包名
     *
     * @param form
     * @return
     */
    public List<String> getJavaBeanImportClass(CodeGeneratorConfigForm form) {
        String upperCamelName = SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.UPPER_CAMEL, form.getBasic().getModuleName());
        ArrayList<String> list = new ArrayList<>();

        list.add("import " + form.getBasic().getJavaPackageName() + ".domain.entity." + upperCamelName + ";");

        list.add("import " + form.getBasic().getJavaPackageName() + ".domain.form." + upperCamelName + "AddForm;");
        list.add("import " + form.getBasic().getJavaPackageName() + ".domain.form." + upperCamelName + "UpdateForm;");
        list.add("import " + form.getBasic().getJavaPackageName() + ".domain.form." + upperCamelName + "QueryForm;");

        list.add("import " + form.getBasic().getJavaPackageName() + ".domain.vo." + upperCamelName + "VO;");
        return list;
    }


    /**
     * 根据列名查找 CodeField
     */
    public CodeField getCodeFieldByColumnName(String columnName, CodeGeneratorConfigForm form) {
        List<CodeField> fields = form.getFields();
        if (SolvelaCollectionUtil.isEmpty(fields)) {
            return null;
        }

        return fields.stream().filter(e -> SolvelaStringUtil.equals(columnName, e.getColumnName())).findFirst().orElse(null);
    }


    /**
     * 是否为文件上传字段
     */
    protected boolean isFile(String columnName, CodeGeneratorConfigForm form) {
        CodeInsertAndUpdate insertAndUpdate = form.getInsertAndUpdate();
        if (insertAndUpdate == null) {
            return false;
        }

        List<CodeInsertAndUpdateField> fieldList = insertAndUpdate.getFieldList();
        if (SolvelaCollectionUtil.isEmpty(fieldList)) {
            return false;
        }

        Optional<CodeInsertAndUpdateField> first = fieldList.stream().filter(e -> columnName.equals(e.getColumnName())).findFirst();
        if (!first.isPresent()) {
            return false;
        }

        CodeInsertAndUpdateField field = first.get();
        return CodeFrontComponentEnum.FILE_UPLOAD.equalsValue(field.getFrontComponent());
    }

    /**
     * 是否为 字典
     */
    protected boolean isDict(String columnName, CodeGeneratorConfigForm form) {
        CodeField codeField = getCodeField(columnName, form);
        return codeField != null && codeField.getDict() != null;
    }

    /**
     * 是否为 枚举
     */
    protected boolean isEnum(String columnName, CodeGeneratorConfigForm form) {
        CodeField codeField = getCodeField(columnName, form);
        return codeField != null && codeField.getEnumName() != null;
    }

    private CodeField getCodeField(String columnName, CodeGeneratorConfigForm form) {
        List<CodeField> fields = form.getFields();
        if (SolvelaCollectionUtil.isEmpty(fields)) {
            return null;
        }

        return fields.stream().filter(e -> columnName.equals(e.getColumnName())).findFirst().orElse(null);
    }

    /**
     * 获取字段集合
     *
     * @param form
     * @return
     */
    protected Map<String, CodeField> getFieldMap(CodeGeneratorConfigForm form) {
        List<CodeField> fields = form.getFields();
        if (fields == null) {
            return new HashMap<>();
        }

        return fields.stream().collect(Collectors.toMap(CodeField::getColumnName, Function.identity()));
    }

    /**
     * 获取java类型
     *
     * @return
     */
    protected String getJavaPackageName(String javaType) {
        if ("BigDecimal".equals(javaType)) {
            return "import java.math.BigDecimal;";
        } else if ("LocalDate".equals(javaType)) {
            return "import java.time.LocalDate;";
        } else if ("LocalDateTime".equals(javaType)) {
            return "import java.time.LocalDateTime;";
        } else {
            return null;
        }
    }

    /*
     * ------------------------------------------------------------------
     * 新增表单与编辑表单的字段生成【完全一样】，所以放在基类。
     *
     * 上提之前是两份逐字相同的 81 行（AddFormVariableService / UpdateFormVariableService）——
     * 改一处漏一处的表现是「新增页的必填校验对，编辑页的不对」，
     * 而这种不一致要等到有人用编辑页保存一条空数据才会被发现。
     * ------------------------------------------------------------------
     */

    /**
     * 新增/编辑表单的字段与 import 列表。
     *
     * <p>一个字段要生成什么，由四件互相独立的事叠加而成：<b>校验注解</b>（枚举 or 普通必填）、
     * <b>字典反序列化</b>、<b>文件上传反序列化</b>、<b>类型 import</b>。
     * 分开写是因为它们可以同时成立 —— 一个字段既可以是字典又可以是必填。
     */
    protected ImmutablePair<List<String>, List<Map<String, Object>>> formFieldsAndPackages(List<CodeInsertAndUpdateField> fields, CodeGeneratorConfigForm form) {
        if (SolvelaCollectionUtil.isEmpty(fields)) {
            return ImmutablePair.of(new ArrayList<>(), new ArrayList<>());
        }

        Map<String, CodeField> fieldMap = getFieldMap(form);
        HashSet<String> packageList = new HashSet<>();
        List<Map<String, Object>> finalFieldList = new ArrayList<>();

        for (CodeInsertAndUpdateField field : fields) {
            CodeField codeField = fieldMap.get(field.getColumnName());
            if (codeField == null) {
                // 表单里配了一个表上不存在的列：跳过而不是报错，让运营改配置就能修
                continue;
            }

            // CodeField 和 InsertAndUpdateField 合并
            Map<String, Object> finalFieldMap = SolvelaBeanUtil.beanToMap(field);
            finalFieldMap.putAll(SolvelaBeanUtil.beanToMap(codeField));

            applyValidation(field, codeField, form, finalFieldMap, packageList);
            applyDict(codeField, finalFieldMap, packageList);
            applyFileUpload(field, finalFieldMap, packageList);

            packageList.add(getJavaPackageName(codeField.getJavaType()));
            finalFieldList.add(finalFieldMap);
        }

        // lombok
        packageList.add("import lombok.Data;");

        List<String> packageNameList = packageList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Collections.sort(packageNameList);
        return ImmutablePair.of(packageNameList, finalFieldList);
    }

    /**
     * 校验与文档注解。枚举字段走 {@code @CheckEnum}，其余走 {@code @NotBlank / @NotNull}。
     *
     * <p>两条路互斥：枚举的「必填」由 CheckEnum 的 {@code required} 表达，
     * 再叠一个 NotNull 只会让同一个错误报两遍。
     */
    private void applyValidation(CodeInsertAndUpdateField field, CodeField codeField,
                                 CodeGeneratorConfigForm form, Map<String, Object> finalFieldMap,
                                 HashSet<String> packageList) {
        if (SolvelaStringUtil.isNotEmpty(codeField.getEnumName())) {
            applyEnum(field, codeField, form, finalFieldMap, packageList);
            return;
        }
        String prefix = "@Schema(description = \"" + codeField.getLabel() + "\"";
        finalFieldMap.put("apiModelProperty",
                prefix + (field.getRequiredFlag() ? ", requiredMode = Schema.RequiredMode.REQUIRED)" : ")"));
        packageList.add("import io.swagger.v3.oas.annotations.media.Schema;");

        if (!Boolean.TRUE.equals(field.getRequiredFlag())) {
            return;
        }
        // 🔴 String 用 @NotBlank 不用 @NotNull：NotNull 放行空串和纯空格，
        // 表现是必填项留空也能保存，而库里存了一个看不见的空值
        boolean isString = "String".equals(codeField.getJavaType());
        finalFieldMap.put("notEmpty",
                "\n    " + (isString ? "@NotBlank" : "@NotNull")
                        + "(message = \"" + codeField.getLabel() + " 不能为空\")");
        packageList.add(isString ? "import jakarta.validation.constraints.NotBlank;"
                : "import jakarta.validation.constraints.NotNull;");
    }

    private void applyEnum(CodeInsertAndUpdateField field, CodeField codeField,
                           CodeGeneratorConfigForm form, Map<String, Object> finalFieldMap,
                           HashSet<String> packageList) {
        packageList.add("import solvela.web.swagger.SchemaEnum;");
        packageList.add("import solvela.base.validation.enumeration.CheckEnum;");
        packageList.add("import " + form.getBasic().getJavaPackageName() + ".constant." + codeField.getEnumName() + ";");

        String checkEnumPrefix = "@CheckEnum(value = " + codeField.getEnumName()
                + ".class, message = \"" + codeField.getLabel() + " 错误\"";
        finalFieldMap.put("apiModelProperty", "@SchemaEnum(value = " + codeField.getEnumName()
                + ".class, desc = \"" + codeField.getLabel() + "\")");
        finalFieldMap.put("checkEnum", checkEnumPrefix + (field.getRequiredFlag() ? ", required = true)" : ")"));
        finalFieldMap.put("isEnum", true);
    }

    /** 字典字段：前端提交的是字典标签，靠反序列化器翻回码值 */
    private void applyDict(CodeField codeField, Map<String, Object> finalFieldMap, HashSet<String> packageList) {
        if (SolvelaStringUtil.isEmpty(codeField.getDict())) {
            return;
        }
        finalFieldMap.put("dict", "\n    @JsonDeserialize(using = DictDataDeserializer.class)");
        packageList.add("import tools.jackson.databind.annotation.JsonDeserialize;");
        packageList.add("import solvela.base.json.deserializer.DictDataDeserializer;");
    }

    /** 文件上传字段：前端提交的是 fileKey 对象，反序列化成实体上的 fileId */
    private void applyFileUpload(CodeInsertAndUpdateField field, Map<String, Object> finalFieldMap,
                                 HashSet<String> packageList) {
        if (!CodeFrontComponentEnum.FILE_UPLOAD.equalsValue(field.getFrontComponent())) {
            return;
        }
        finalFieldMap.put("file", "\n    @JsonDeserialize(using = FileKeyVoDeserializer.class)");
        packageList.add("import tools.jackson.databind.annotation.JsonDeserialize;");
        packageList.add("import solvela.base.json.deserializer.FileKeyVoDeserializer;");
    }
}
