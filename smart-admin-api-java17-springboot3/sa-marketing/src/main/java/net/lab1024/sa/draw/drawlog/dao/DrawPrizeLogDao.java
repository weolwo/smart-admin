package net.lab1024.sa.draw.drawlog.dao;

        import java.util.List;
        import net.lab1024.sa.draw.drawlog.domain.entity.DrawPrizeLog;
        import net.lab1024.sa.draw.drawlog.domain.form.DrawPrizeLogQueryForm;
        import net.lab1024.sa.draw.drawlog.domain.vo.DrawPrizeLogVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 抽奖记录 Dao
 *
 * @Author weolwo
 * @Date 2026-04-19 09:21:26
 * @Copyright weolwo
 */
@Mapper
public interface DrawPrizeLogDao extends BaseMapper<DrawPrizeLog> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<DrawPrizeLogVO> queryPage(Page<?> page, @Param("queryForm") DrawPrizeLogQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<DrawPrizeLogVO> queryList(@Param("queryForm") DrawPrizeLogQueryForm queryForm);

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