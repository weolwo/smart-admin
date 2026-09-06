package solvela.admin.module.system.codegenerator.service.variable.backend.domain;

import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCollectionUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeField;
import solvela.admin.module.system.codegenerator.domain.model.CodeTableField;
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

public class VOVariableService extends CodeGenerateBaseVariableService {

    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        Map<String, Object> variablesMap = new HashMap<>();

        Map<String, CodeField> fieldMap = getFieldMap(form);
        List<CodeTableField> updateFieldList = form.getTableFields().stream().filter(e -> Boolean.TRUE.equals(e.getShowFlag())).collect(Collectors.toList());

        ImmutablePair<List<String>, List<Map<String, Object>>> packageListAndFields = getPackageListAndFields(updateFieldList, form);

        variablesMap.put("packageName", form.getBasic().getJavaPackageName() + ".domain.vo");
        variablesMap.put("importPackageList", packageListAndFields.getLeft());
        variablesMap.put("fields", packageListAndFields.getRight());

        return variablesMap;
    }

    /**
     * VO 的字段与 import 列表。
     *
     * <p>与新增/编辑表单（{@code formFieldsAndPackages}）看着像，但方向<b>相反</b>：
     * 表单是<b>入参</b>，要校验、要把前端传来的东西反序列化成实体值；
     * VO 是<b>出参</b>，不需要任何校验注解，文件字段走的是序列化器而不是反序列化器。
     * 所以这两段不能合并 —— 合并出来的方法会带一个「是入还是出」的布尔开关，
     * 而那种开关迟早会让某一边悄悄带上不该有的注解。
     */
    public ImmutablePair<List<String>, List<Map<String, Object>>> getPackageListAndFields(List<CodeTableField> fields, CodeGeneratorConfigForm form) {
        if (SolvelaCollectionUtil.isEmpty(fields)) {
            return ImmutablePair.of(new ArrayList<>(), new ArrayList<>());
        }

        Map<String, CodeField> fieldMap = getFieldMap(form);
        HashSet<String> packageList = new HashSet<>();
        List<Map<String, Object>> finalFieldList = new ArrayList<>();

        for (CodeTableField field : fields) {
            CodeField codeField = fieldMap.get(field.getColumnName());
            if (codeField == null) {
                // 配置里引用了一个表上不存在的列：跳过而不是报错，改配置就能修
                continue;
            }

            // CodeField 和 CodeTableField 合并
            Map<String, Object> finalFieldMap = SolvelaBeanUtil.beanToMap(field);
            finalFieldMap.putAll(SolvelaBeanUtil.beanToMap(codeField));

            applySchema(codeField, form, finalFieldMap, packageList);
            applyFileSerializer(field, form, finalFieldMap, packageList);

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
     * 文档注解。枚举字段用 {@code @SchemaEnum} —— 它会把枚举的全部取值和含义渲染进接口文档，
     * 前端不用再去问「1 和 2 分别是什么」。
     */
    private void applySchema(CodeField codeField, CodeGeneratorConfigForm form,
                             Map<String, Object> finalFieldMap, HashSet<String> packageList) {
        if (SolvelaStringUtil.isEmpty(codeField.getEnumName())) {
            finalFieldMap.put("apiModelProperty", "@Schema(description = \"" + codeField.getLabel() + "\")");
            packageList.add("import io.swagger.v3.oas.annotations.media.Schema;");
            return;
        }
        packageList.add("import solvela.web.swagger.SchemaEnum;");
        packageList.add("import " + form.getBasic().getJavaPackageName() + ".constant." + codeField.getEnumName() + ";");
        finalFieldMap.put("apiModelProperty", "@SchemaEnum(value = " + codeField.getEnumName()
                + ".class, desc = \"" + codeField.getLabel() + "\")");
        finalFieldMap.put("isEnum", true);
    }

    /** 文件字段：库里存的是 fileId，出参要序列化成前端能直接用的 fileKey 对象 */
    private void applyFileSerializer(CodeTableField field, CodeGeneratorConfigForm form,
                                     Map<String, Object> finalFieldMap, HashSet<String> packageList) {
        if (!isFile(field.getColumnName(), form)) {
            return;
        }
        finalFieldMap.put("file", "\n    @JsonSerialize(using = FileKeyVoSerializer.class)");
        packageList.add("import tools.jackson.databind.annotation.JsonSerialize;");
        packageList.add("import solvela.base.json.serializer.FileKeyVoSerializer;");
    }

}
