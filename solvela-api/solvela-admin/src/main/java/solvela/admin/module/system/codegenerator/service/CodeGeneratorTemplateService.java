package solvela.admin.module.system.codegenerator.service;

import solvela.exception.BusinessException;
import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCaseFormat;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import solvela.base.util.SolvelaCollectionUtil;
import solvela.base.util.SolvelaDateFormatterEnum;
import solvela.base.util.SolvelaLocalDateUtil;
import solvela.base.util.SolvelaRandomUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.domain.entity.CodeGeneratorConfigEntity;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.model.*;
import solvela.admin.module.system.codegenerator.service.variable.CodeGenerateBaseVariableService;
import solvela.admin.module.system.codegenerator.service.variable.backend.*;
import solvela.admin.module.system.codegenerator.service.variable.backend.domain.*;
import solvela.admin.module.system.codegenerator.service.variable.front.ApiVariableService;
import solvela.admin.module.system.codegenerator.service.variable.front.ConstVariableService;
import solvela.admin.module.system.codegenerator.service.variable.front.FormVariableService;
import solvela.admin.module.system.codegenerator.service.variable.front.ListVariableService;
import solvela.admin.module.system.codegenerator.util.CodeGeneratorTool;
import solvela.base.json.JsonUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器 模板 Service
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2022-06-30 22:15:38
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */

@Service
@Slf4j
public class CodeGeneratorTemplateService {


    private Map<String, CodeGenerateBaseVariableService> map = new HashMap<>();

    @PostConstruct
    public void init() {
        // 后端
        map.put("java/domain/entity/Entity.java", new EntityVariableService());
        map.put("java/domain/form/AddForm.java", new AddFormVariableService());
        map.put("java/domain/form/UpdateForm.java", new UpdateFormVariableService());
        map.put("java/domain/form/QueryForm.java", new QueryFormVariableService());
        map.put("java/domain/vo/VO.java", new VOVariableService());
        map.put("java/controller/Controller.java", new ControllerVariableService());
        map.put("java/service/Service.java", new ServiceVariableService());
        map.put("java/manager/Manager.java", new ManagerVariableService());
        map.put("java/dao/Dao.java", new DaoVariableService());
        map.put("java/mapper/Mapper.xml", new MapperVariableService());
        // 菜单 SQL
        map.put("java/sql/Menu.sql", new MenuVariableService());
        // 前端
        map.put("js/api.js", new ApiVariableService());
        map.put("js/const.js", new ConstVariableService());
        map.put("js/list.vue", new ListVariableService());
        map.put("js/form.vue", new FormVariableService());
        // ts前端
        map.put("ts/api.ts", new ApiVariableService());
        map.put("ts/const.ts", new ConstVariableService());
        map.put("ts/list.vue", new ListVariableService());
        map.put("ts/form.vue", new FormVariableService());
    }

    /**
     * 把整套代码生成到一个临时目录，打成 zip 写出去，然后<b>无论成败都删掉临时目录</b>。
     *
     * <p>临时目录用 UUID 命名：两个人同时点生成，落在同一个目录里就会互相覆盖，
     * 而下载下来的 zip 是两套代码混在一起 —— 没有报错，只有一份跑不起来的产物。
     */
    public void zipGeneratedFiles(OutputStream outputStream, String tableName, CodeGeneratorConfigEntity codeGeneratorConfigEntity) {
        String uuid = SolvelaRandomUtil.simpleUuid();
        File dir = new File(uuid);
        CodeBasic basic = JsonUtils.parseObject(codeGeneratorConfigEntity.getBasic(), CodeBasic.class);

        try {
            generateTemplateFiles(uuid, tableName, basic, codeGeneratorConfigEntity);
            generateEnumFiles(uuid, basic, codeGeneratorConfigEntity);
            zipDirectoryContent(outputStream, dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            deleteRecursively(dir);
        }
    }

    /**
     * 按注册的模板逐个生成文件。
     *
     * <p>输出路径是从模板路径推出来的：{@code java/xxx.java.vm} -> {@code java/模块名/模块名Xxx.java}，
     * {@code js/xxx.vue.vm} -> {@code js/模块名/模块名-xxx.vue}。
     * 单个模板出错只记日志不中断 —— 一个模板坏掉不该让整包都下载不下来。
     */
    private void generateTemplateFiles(String uuid, String tableName, CodeBasic basic,
                                       CodeGeneratorConfigEntity codeGeneratorConfigEntity) {
        String moduleName = basic.getModuleName();
        String upperCamel = new CodeGeneratorTool().lowerCamel2UpperCamel(moduleName);
        String lowerHyphen = new CodeGeneratorTool().lowerCamel2LowerHyphen(moduleName);

        for (String templateFile : map.keySet()) {
            try {
                String fullPathFileName = resolveOutputPath(templateFile, basic, upperCamel, lowerHyphen);
                String fileContent = generate(tableName, templateFile, codeGeneratorConfigEntity);
                writeTo(uuid + "/" + fullPathFileName, fileContent);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /** 模板路径 -> 产物路径。Entity 模板要特殊处理：它的文件名就叫 {@code 模块名.java} */
    private String resolveOutputPath(String templateFile, CodeBasic basic, String upperCamel, String lowerHyphen) {
        String[] templateSplit = templateFile.split("/");
        String last = templateSplit[templateSplit.length - 1];
        String fileName = templateFile.startsWith("java")
                ? upperCamel + (last.contains("Entity") ? ".java" : last)
                : lowerHyphen + "-" + last;
        return templateFile.replaceAll(last, fileName)
                .replaceAll("java/", "java/" + basic.getModuleName().toLowerCase() + "/")
                .replaceAll("js/", "js/" + lowerHyphen + "/");
    }

    /**
     * 配了 enumName 的列各生成一个枚举类。
     *
     * <p>类名统一补 {@code Enum} 后缀：生成出来的 VO/Form 里引用的就是这个名字
     *（见各 VariableService 拼的那句 import），少了后缀会 import 到一个不存在的类。
     */
    private void generateEnumFiles(String uuid, CodeBasic basic, CodeGeneratorConfigEntity codeGeneratorConfigEntity) {
        List<CodeField> fields = JsonUtils.parseList(codeGeneratorConfigEntity.getFields(), CodeField.class);
        if (SolvelaCollectionUtil.isEmpty(fields)) {
            return;
        }
        for (CodeField codeField : fields) {
            if (SolvelaStringUtil.isBlank(codeField.getEnumName())) {
                continue;
            }
            String enumName = SolvelaCaseFormat.LOWER_CAMEL.to(SolvelaCaseFormat.UPPER_CAMEL, codeField.getEnumName());
            if (!enumName.endsWith("Enum")) {
                enumName = enumName + "Enum";
            }
            Map<String, Object> variablesMap = new HashMap<>();
            variablesMap.put("enumName", enumName);
            variablesMap.put("enumDesc", codeField.getColumnComment());
            variablesMap.put("enumJavaType", codeField.getJavaType());
            variablesMap.put("basic", basic);
            variablesMap.put("packageName", basic.getJavaPackageName() + ".constant");

            String fileContent = render("code-generator-template/java/constant/enum.java.vm", variablesMap);
            String path = uuid + "/java/" + basic.getModuleName().toLowerCase() + "/constant/" + enumName + ".java";
            try {
                writeTo(path, fileContent);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /** 建好父目录再写。父目录不存在时 FileWriter 抛的是 FileNotFoundException，指不到真正的原因 */
    private void writeTo(String path, String content) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        appendUtf8(file, content);
    }

    /**
     * 生成时间戳，null 时返回 null —— 模板里 basic.frontDate 允许没配
     */
    private static String formatDateTime(java.time.LocalDateTime time) {
        return time == null ? null : SolvelaLocalDateUtil.format(time, SolvelaDateFormatterEnum.YMD_HMS);
    }

    /**
     * 以 UTF-8 追加写入，文件不存在则创建（原 FileUtil.appendUtf8String）
     */
    static void appendUtf8(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * 把目录里的内容打成 zip 写进 outputStream —— 是「内容」，不含 dir 这一层目录本身
     * （对应原 ZipUtil.zip 的 withSrcDir=false），所以解压出来直接就是 java/、js/ 这些目录。
     *
     * 这里必须关掉 ZipOutputStream：中央目录是在 close 时才写的，不关就是一个打不开的空包。
     * 调用方传进来的是 ByteArrayOutputStream，关它没有副作用，toByteArray 照常可用。
     */
    static void zipDirectoryContent(OutputStream outputStream, File dir) throws IOException {
        Path root = dir.toPath();
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);
             Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                // zip 里一律用正斜杠，Windows 下 Path 的分隔符是反斜杠，直接拼会生成一个畸形包
                String entryName = root.relativize(path).toString().replace(File.separatorChar, '/');
                zipOut.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zipOut);
                zipOut.closeEntry();
            }
        }
    }

    /**
     * 递归删除临时目录（原 FileUtil.del）。删不掉只记日志，不能让它盖掉正常返回。
     *
     * 包级可见而非 private：这三个方法是从 hutool FileUtil/ZipUtil 手抄回来的，
     * 必须能被同包测试直接打，见 CodeGeneratorTemplateZipTest。
     */
    static void deleteRecursively(File dir) {
        if (!dir.exists()) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            // 倒序：先文件后目录，否则非空目录删不掉
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("删除代码生成临时文件失败：{}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("清理代码生成临时目录失败：{}", dir, e);
        }
    }


    /**
     * 渲染一个模板。
     *
     * <p>三种「不生成」的情况都<b>返回一句人话而不是抛异常</b>：调用方是打包循环，
     * 一个模板不适用不该让整包失败，而这句话会直接出现在预览框里告诉运营为什么没有内容。
     */
    public String generate(String tableName, String file, CodeGeneratorConfigEntity codeGeneratorConfigEntity) {
        String finalFile = file;
        Optional<String> optional = map.keySet().stream().filter(e -> e.contains(finalFile)).findFirst();
        if (optional.isEmpty()) {
            return "不存在此模板！";
        }
        file = optional.get();
        CodeGenerateBaseVariableService variableService = map.get(file);
        if (variableService == null) {
            return "代码生成Service不存在，请检查相关代码！";
        }

        CodeGeneratorConfigForm form = toConfigForm(tableName, codeGeneratorConfigEntity);
        if (!variableService.isSupport(form)) {
            return "业务不需要此功能，故没有生成代码；";
        }

        Map<String, Object> variablesMap = commonVariables(form, tableName);
        // 各模板自己的特殊变量最后放，允许覆盖通用变量
        variablesMap.putAll(variableService.getInjectVariablesMap(form));
        return render("code-generator-template/" + file + ".vm", variablesMap);
    }

    /** 把库里那几段 JSON 还原成一份配置对象。宽度缺省补 0 —— 模板里直接输出，null 会渲染成字面量 "null" */
    private CodeGeneratorConfigForm toConfigForm(String tableName, CodeGeneratorConfigEntity entity) {
        CodeBasic basic = JsonUtils.parseObject(entity.getBasic(), CodeBasic.class);
        List<CodeField> fields = JsonUtils.parseList(entity.getFields(), CodeField.class);
        CodeInsertAndUpdate insertAndUpdate = JsonUtils.parseObject(entity.getInsertAndUpdate(), CodeInsertAndUpdate.class);
        CodeDelete deleteInfo = JsonUtils.parseObject(entity.getDeleteInfo(), CodeDelete.class);
        List<CodeQueryField> queryFields = JsonUtils.parseList(entity.getQueryFields(), CodeQueryField.class);
        List<CodeTableField> tableFields = JsonUtils.parseList(entity.getTableFields(), CodeTableField.class);
        tableFields.forEach(e -> e.setWidth(e.getWidth() == null ? 0 : e.getWidth()));

        CodeGeneratorConfigForm form = CodeGeneratorConfigForm.builder()
                .basic(basic).fields(fields).insertAndUpdate(insertAndUpdate)
                .deleteInfo(deleteInfo).queryFields(queryFields).tableFields(tableFields).build();
        form.setTableName(tableName);
        return form;
    }

    /** 所有模板都能用到的变量：配置各段、三种命名形态、主键信息 */
    private Map<String, Object> commonVariables(CodeGeneratorConfigForm form, String tableName) {
        CodeBasic basic = form.getBasic();
        Map<String, Object> basicMap = SolvelaBeanUtil.beanToMap(basic);
        basicMap.put("frontDate", formatDateTime(basic.getFrontDate()));
        basicMap.put("backendDate", formatDateTime(basic.getBackendDate()));

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("basic", basicMap);
        variablesMap.put("fields", form.getFields());
        variablesMap.put("insertAndUpdate", form.getInsertAndUpdate());
        variablesMap.put("deleteInfo", form.getDeleteInfo());
        variablesMap.put("queryFields", form.getQueryFields());
        variablesMap.put("tableFields", form.getTableFields());
        variablesMap.put("tableName", tableName);

        // 同一个模块名的三种写法，模板里按位置各取所需
        HashMap<String, String> names = new HashMap<>();
        names.put("lowerCamel", SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.LOWER_CAMEL, basic.getModuleName()));
        names.put("upperCamel", SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.UPPER_CAMEL, basic.getModuleName()));
        names.put("lowerHyphenCamel", SolvelaCaseFormat.UPPER_CAMEL.to(SolvelaCaseFormat.LOWER_HYPHEN, basic.getModuleName()));
        variablesMap.put("name", names);

        /*
         * 主键必须有，没有就当场抛。
         *
         * 🔴 不能「找不到就不放这几个变量」—— Velocity 遇到未定义变量会把 $primaryKeyFieldName
         * 原样渲染进产物，于是生成出一份看起来正常、实际编译不过的代码，
         * 而报错位置指向生成代码，根本看不出根因是这张表没配主键。
         */
        CodeField primaryKey = form.getFields().stream()
                .filter(CodeField::getPrimaryKeyFlag).findFirst()
                .orElseThrow(() -> new BusinessException("表 " + tableName + " 没有配置主键，无法生成代码"));
        variablesMap.put("primaryKeyJavaType", primaryKey.getJavaType());
        variablesMap.put("primaryKeyFieldName", primaryKey.getFieldName());
        variablesMap.put("primaryKeyColumnName", primaryKey.getColumnName());
        return variablesMap;
    }

    /**
     * 渲染
     *
     * @param templateFile
     * @param variablesMap
     * @return
     */
    private String render(String templateFile, Map<String, Object> variablesMap) {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, true);
        engine.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        engine.setProperty("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init();
        Template template = engine.getTemplate(templateFile);

        //加载tools.xml配置文件
        ToolManager toolManager = new ToolManager();
        toolManager.configure("code-generator-template/tools.xml");

        //注入变量
        ToolContext context = toolManager.createContext();
        context.putAll(variablesMap);

        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

}
