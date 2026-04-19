package net.lab1024.sa.lottery.numberpool.dao;

        import java.util.List;
        import net.lab1024.sa.lottery.numberpool.domain.entity.LotteryNumberPool;
        import net.lab1024.sa.lottery.numberpool.domain.form.LotteryNumberPoolQueryForm;
        import net.lab1024.sa.lottery.numberpool.domain.vo.LotteryNumberPoolVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 彩票号码池 Dao
 *
 * @Author weolwo
 * @Date 2026-04-19 11:31:09
 * @Copyright weolwo
 */
@Mapper
public interface LotteryNumberPoolDao extends BaseMapper<LotteryNumberPool> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<LotteryNumberPoolVO> queryPage(Page<?> page, @Param("queryForm") LotteryNumberPoolQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<LotteryNumberPoolVO> queryList(@Param("queryForm") LotteryNumberPoolQueryForm queryForm);

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