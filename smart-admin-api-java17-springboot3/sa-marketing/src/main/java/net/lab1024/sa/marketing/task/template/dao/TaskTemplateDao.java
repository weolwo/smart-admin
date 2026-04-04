package net.lab1024.sa.marketing.task.template.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.task.template.domain.entity.TaskTemplate;
        import net.lab1024.sa.marketing.task.template.domain.form.TaskTemplateQueryForm;
        import net.lab1024.sa.marketing.task.template.domain.vo.TaskTemplateVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 系统级-任务模板表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */
@Mapper
public interface TaskTemplateDao extends BaseMapper<TaskTemplate> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskTemplateVO> queryPage(Page<?> page, @Param("queryForm") TaskTemplateQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskTemplateVO> queryList(@Param("queryForm") TaskTemplateQueryForm queryForm);

            // ----- 物理删除 -----
                /**
                 * 单个物理删除
                 */
                long deleteById(@Param("id") Long id);

                /**
                 * 批量物理删除
                 */
                void batchDelete(@Param("idList") List<Long> idList);
}