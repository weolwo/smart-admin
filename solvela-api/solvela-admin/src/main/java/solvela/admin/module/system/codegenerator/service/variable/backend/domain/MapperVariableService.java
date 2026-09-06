package solvela.admin.module.system.codegenerator.service.variable.backend.domain;

import solvela.base.util.SolvelaBeanUtil;
import solvela.admin.module.system.codegenerator.constant.CodeQueryFieldQueryTypeEnum;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.CodeQueryField;
import solvela.admin.module.system.codegenerator.service.variable.CodeGenerateBaseVariableService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 1024创新实验室-主任:卓大
 * @Date 2022/9/29 17:20:41
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */

public class MapperVariableService extends CodeGenerateBaseVariableService {

    @Override
    public boolean isSupport(CodeGeneratorConfigForm form) {
        return true;
    }

    /**
     * Mapper XML 的变量：每个查询字段在 where 里长什么样。
     *
     * <p>三种形态：模糊查询拼 {@code INSTR}（可跨多列 OR）、字典查询同样走 INSTR、
     * 其余就是一个等值列名。
     */
    @Override
    public Map<String, Object> getInjectVariablesMap(CodeGeneratorConfigForm form) {
        List<Map<String, Object>> finalQueryFiledList = new ArrayList<>();
        for (CodeQueryField queryField : form.getQueryFields()) {
            Map<String, Object> fieldMap = SolvelaBeanUtil.beanToMap(queryField);
            applyWhereFragment(form, queryField, fieldMap);
            finalQueryFiledList.add(fieldMap);
        }

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("queryFields", finalQueryFiledList);
        variablesMap.put("daoClassName", form.getBasic().getJavaPackageName() + ".dao." + form.getBasic().getModuleName() + "Dao");
        return variablesMap;
    }

    private void applyWhereFragment(CodeGeneratorConfigForm form, CodeQueryField queryField,
                                    Map<String, Object> fieldMap) {
        if (CodeQueryFieldQueryTypeEnum.LIKE.getValue().equals(queryField.getQueryTypeEnum())) {
            fieldMap.put("likeStr", likeFragment(form, queryField));
            return;
        }
        if (CodeQueryFieldQueryTypeEnum.DICT.equalsValue(queryField.getQueryTypeEnum())) {
            fieldMap.put("likeStr", instr(form, queryField, queryField.getColumnNameList().get(0)));
            return;
        }
        fieldMap.put("columnName", queryField.getColumnNameList().get(0));
    }

    /**
     * 模糊查询片段。多列时用 OR 串起来并整体括住。
     *
     * <p>🔴 那对括号不能省：{@code AND a OR b} 在 SQL 里会和前面的条件重新结合，
     * 表现是「加了别的筛选条件之后，模糊搜索把不该出现的行也带出来了」。
     *
     * <p>用 {@code INSTR} 而不是 {@code LIKE '%x%'}：两者都用不上索引，
     * 但 INSTR 不需要转义用户输入里的 {@code %} 和 {@code _}。
     */
    private String likeFragment(CodeGeneratorConfigForm form, CodeQueryField queryField) {
        List<String> columnNameList = queryField.getColumnNameList();
        if (columnNameList.size() == 1) {
            return "AND " + instr(form, queryField, columnNameList.get(0));
        }
        StringBuilder sb = new StringBuilder("AND (\n                  ");
        for (int i = 0; i < columnNameList.size(); i++) {
            if (i > 0) {
                sb.append("\n                  OR ");
            }
            sb.append(instr(form, queryField, columnNameList.get(i)));
        }
        return sb.append("\n                )").toString();
    }

    /** {@code INSTR(t_notice.title,#{queryForm.keywords})} */
    private String instr(CodeGeneratorConfigForm form, CodeQueryField queryField, String columnName) {
        return "INSTR(" + form.getTableName() + "." + columnName
                + ",#{queryForm." + queryField.getFieldName() + "})";
    }

}
