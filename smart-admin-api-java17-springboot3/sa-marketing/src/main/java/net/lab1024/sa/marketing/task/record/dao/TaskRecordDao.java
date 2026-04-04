package net.lab1024.sa.marketing.task.record.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.task.record.domain.entity.TaskRecord;
        import net.lab1024.sa.marketing.task.record.domain.form.TaskRecordQueryForm;
        import net.lab1024.sa.marketing.task.record.domain.vo.TaskRecordVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 运行时-会员任务进度实例表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */
@Mapper
public interface TaskRecordDao extends BaseMapper<TaskRecord> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskRecordVO> queryPage(Page<?> page, @Param("queryForm") TaskRecordQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskRecordVO> queryList(@Param("queryForm") TaskRecordQueryForm queryForm);

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