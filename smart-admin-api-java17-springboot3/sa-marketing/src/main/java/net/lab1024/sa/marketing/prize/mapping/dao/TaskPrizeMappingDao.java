package net.lab1024.sa.marketing.prize.mapping.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.prize.mapping.domain.entity.TaskPrizeMapping;
        import net.lab1024.sa.marketing.prize.mapping.domain.form.TaskPrizeMappingQueryForm;
        import net.lab1024.sa.marketing.prize.mapping.domain.vo.TaskPrizeMappingVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 业务级-任务阶段与奖励包映射表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */
@Mapper
public interface TaskPrizeMappingDao extends BaseMapper<TaskPrizeMapping> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskPrizeMappingVO> queryPage(Page<?> page, @Param("queryForm") TaskPrizeMappingQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<TaskPrizeMappingVO> queryList(@Param("queryForm") TaskPrizeMappingQueryForm queryForm);

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