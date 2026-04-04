package net.lab1024.sa.marketing.task.config.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.task.config.domain.entity.TaskConfig;
        import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigQueryForm;
        import net.lab1024.sa.marketing.task.config.domain.vo.TaskConfigVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 业务级-任务规则配置表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */
@Mapper
public interface TaskConfigDao extends BaseMapper<TaskConfig> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskConfigVO> queryPage(Page<?> page, @Param("queryForm") TaskConfigQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskConfigVO> queryList(@Param("queryForm") TaskConfigQueryForm queryForm);

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