package solvela.admin.module.system.codegenerator.service;

import solvela.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import solvela.base.domain.PageResult;
import solvela.base.util.SolvelaCollectionUtil;
import solvela.base.dao.SolvelaPageUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.admin.module.system.codegenerator.constant.CodeGeneratorConstant;
import solvela.admin.module.system.codegenerator.dao.CodeGeneratorConfigDao;
import solvela.admin.module.system.codegenerator.dao.CodeGeneratorDao;
import solvela.admin.module.system.codegenerator.domain.entity.CodeGeneratorConfigEntity;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorConfigForm;
import solvela.admin.module.system.codegenerator.domain.form.CodeGeneratorPreviewForm;
import solvela.admin.module.system.codegenerator.domain.form.TableQueryForm;
import solvela.admin.module.system.codegenerator.domain.model.*;
import solvela.admin.module.system.codegenerator.domain.vo.TableColumnVO;
import solvela.admin.module.system.codegenerator.domain.vo.TableConfigVO;
import solvela.admin.module.system.codegenerator.domain.vo.TableVO;
import solvela.base.json.JsonUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

/**
 * 代码生成器 Service
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2022-06-30 22:15:38
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Service
public class CodeGeneratorService {

    private static final String COLUMN_NO_NULLABLE_IDENTIFY = "NO";

    private static final String COLUMN_PRIMARY_KEY = "PRI";

    private static final String COLUMN_AUTO_INCREASE = "auto_increment";

    @Resource
    private CodeGeneratorDao codeGeneratorDao;

    @Resource
    private CodeGeneratorConfigDao codeGeneratorConfigDao;

    @Resource
    private CodeGeneratorTemplateService codeGeneratorTemplateService;


    /**
     * 列信息
     *
     * @param tableName
     * @return
     */
    public List<TableColumnVO> getTableColumns(String tableName) {
        List<TableColumnVO> tableColumns = codeGeneratorDao.selectTableColumn(tableName);
        for (TableColumnVO tableColumn : tableColumns) {
            tableColumn.setNullableFlag(!COLUMN_NO_NULLABLE_IDENTIFY.equalsIgnoreCase(tableColumn.getIsNullable()));
            tableColumn.setPrimaryKeyFlag(COLUMN_PRIMARY_KEY.equalsIgnoreCase(tableColumn.getColumnKey()));
            tableColumn.setAutoIncreaseFlag(SolvelaStringUtil.isNotEmpty(tableColumn.getExtra()) && COLUMN_AUTO_INCREASE.equalsIgnoreCase(tableColumn.getExtra()));
        }
        return tableColumns;
    }


    /**
     * 查询数据库表数据
     *
     * @param tableQueryForm
     * @return
     */
    public PageResult<TableVO> queryTableList(TableQueryForm tableQueryForm) {
        Page<?> page = SolvelaPageUtil.convert2PageQuery(tableQueryForm);
        List<TableVO> tableVOList = codeGeneratorDao.queryTableList(page, tableQueryForm);
        return SolvelaPageUtil.convert2PageResult(page, tableVOList);
    }

    /**
     * 获取 表的 配置信息
     *
     * @param table
     * @return
     */
    public TableConfigVO getTableConfig(String table) {
        TableConfigVO config = new TableConfigVO();
        CodeGeneratorConfigEntity entity = codeGeneratorConfigDao.selectById(table);
        if (entity == null) {
            // 还没配过这张表：返回一个空壳而不是 null，前端拿到就是一张空白配置页
            return config;
        }

        /*
         * 每一段都单独判空再解析。
         *
         * 🔴 不能省掉判空直接 parse：这几列是历史上分批加的，老配置里后加的那几列是 NULL，
         * 而 JsonUtils.parseObject(null) 会抛。表现是「早期建的表一点配置就报错」，
         * 报错信息还指向 JSON 解析，看不出是哪一列缺了。
         */
        if (SolvelaStringUtil.isNotEmpty(entity.getBasic())) {
            config.setBasic(JsonUtils.parseObject(entity.getBasic(), CodeBasic.class));
        }
        if (SolvelaStringUtil.isNotEmpty(entity.getFields())) {
            config.setFields(JsonUtils.parseList(entity.getFields(), CodeField.class));
        }
        if (SolvelaStringUtil.isNotEmpty(entity.getInsertAndUpdate())) {
            config.setInsertAndUpdate(JsonUtils.parseObject(entity.getInsertAndUpdate(), CodeInsertAndUpdate.class));
        }
        if (SolvelaStringUtil.isNotEmpty(entity.getDeleteInfo())) {
            config.setDeleteInfo(JsonUtils.parseObject(entity.getDeleteInfo(), CodeDelete.class));
        }
        if (SolvelaStringUtil.isNotEmpty(entity.getQueryFields())) {
            config.setQueryFields(JsonUtils.parseList(entity.getQueryFields(), CodeQueryField.class));
        }
        if (SolvelaStringUtil.isNotEmpty(entity.getTableFields())) {
            config.setTableFields(JsonUtils.parseList(entity.getTableFields(), CodeTableField.class));
        }
        return config;
    }

    /**
     * 更新配置
     *
     * @param form
     * @return
     */
    public synchronized void updateConfig(CodeGeneratorConfigForm form) {
        if (codeGeneratorDao.countByTableName(form.getTableName()) == 0) {
            throw new BusinessException("表不存在，请联系后端查看下数据库");
        }
        checkTableStructure(form);

        CodeGeneratorConfigEntity entity = codeGeneratorConfigDao.selectById(form.getTableName());
        boolean updateFlag = entity != null;
        if (!updateFlag) {
            entity = new CodeGeneratorConfigEntity();
        }
        fillConfigJson(entity, form);

        if (updateFlag) {
            codeGeneratorConfigDao.updateById(entity);
        } else {
            codeGeneratorConfigDao.insert(entity);
        }
    }

    /**
     * 表结构必须撑得住这份配置，两条：
     *
     * <ul>
     *   <li><b>选了假删就必须真有 deleted_flag 列</b> —— 没有的话生成的代码能编译、能跑，
     *       但每次「删除」都是一条 update 一个不存在的列，运行期才炸；</li>
     *   <li><b>表必须有主键</b> —— 没有主键的表生成不出可用的 CRUD，
     *       这里拦下来好过让人拿着一份坏代码去查为什么编译不过。</li>
     * </ul>
     */
    private void checkTableStructure(CodeGeneratorConfigForm form) {
        List<TableColumnVO> tableColumns = getTableColumns(form.getTableName());

        CodeDelete deleteInfo = form.getDeleteInfo();
        boolean logicalDelete = null != deleteInfo
                && deleteInfo.getIsSupportDelete() && !deleteInfo.getIsPhysicallyDeleted();
        if (logicalDelete && tableColumns.stream()
                .noneMatch(e -> e.getColumnName().equals(CodeGeneratorConstant.DELETED_FLAG))) {
            throw new BusinessException("表结构中没有假删字段：" + CodeGeneratorConstant.DELETED_FLAG + ",请仔细排查");
        }

        if (tableColumns.stream().noneMatch(e -> COLUMN_PRIMARY_KEY.equalsIgnoreCase(e.getColumnKey()))) {
            throw new BusinessException("表必须有主键，请联系后端查看下数据库表结构");
        }
    }

    /** 配置的六段各自序列化成一列 JSON。分列存是为了让每一段能独立演进，见 getTableConfig 的判空 */
    private void fillConfigJson(CodeGeneratorConfigEntity entity, CodeGeneratorConfigForm form) {
        entity.setTableName(form.getTableName());
        entity.setBasic(JsonUtils.toJson(form.getBasic()));
        entity.setFields(JsonUtils.toJson(form.getFields()));
        entity.setInsertAndUpdate(JsonUtils.toJson(form.getInsertAndUpdate()));
        entity.setDeleteInfo(JsonUtils.toJson(form.getDeleteInfo()));
        entity.setQueryFields(JsonUtils.toJson(form.getQueryFields()));
        entity.setTableFields(JsonUtils.toJson(form.getTableFields()));
    }

    /**
     * 预览
     *
     * @param form
     * @return
     */
    public String preview(CodeGeneratorPreviewForm form) {
        long existCount = codeGeneratorDao.countByTableName(form.getTableName());
        if (existCount == 0) {
            throw new BusinessException("表不存在，请联系后端查看下数据库");
        }

        CodeGeneratorConfigEntity codeGeneratorConfigEntity = codeGeneratorConfigDao.selectById(form.getTableName());
        if (codeGeneratorConfigEntity == null) {
            throw new BusinessException("配置信息不存在，请先进行配置");
        }

        List<TableColumnVO> columns = getTableColumns(form.getTableName());
        if (SolvelaCollectionUtil.isEmpty(columns)) {
            throw new BusinessException("表没有列信息无法生成");
        }

        String result = codeGeneratorTemplateService.generate(form.getTableName(), form.getTemplateFile(), codeGeneratorConfigEntity);
        return result;

    }

    /**
     * 下载代码
     *
     * @param tableName
     * @return
     */
    public byte[] download(String tableName) {
        if (SolvelaStringUtil.isBlank(tableName)) {
            throw new BusinessException("表名不能为空");
        }

        long existCount = codeGeneratorDao.countByTableName(tableName);
        if (existCount == 0) {
            throw new BusinessException("表不存在，请联系后端查看下数据库");
        }

        CodeGeneratorConfigEntity codeGeneratorConfigEntity = codeGeneratorConfigDao.selectById(tableName);
        if (codeGeneratorConfigEntity == null) {
            throw new BusinessException("配置信息不存在，请先进行配置");
        }

        List<TableColumnVO> columns = getTableColumns(tableName);
        if (SolvelaCollectionUtil.isEmpty(columns)) {
            throw new BusinessException("表没有列信息无法生成");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codeGeneratorTemplateService.zipGeneratedFiles(out, tableName, codeGeneratorConfigEntity);
        return out.toByteArray();
    }
}