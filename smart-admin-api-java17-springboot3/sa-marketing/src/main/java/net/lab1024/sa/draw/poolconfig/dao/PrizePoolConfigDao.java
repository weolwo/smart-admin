package net.lab1024.sa.draw.poolconfig.dao;

        import java.util.List;
        import net.lab1024.sa.draw.poolconfig.domain.entity.PrizePoolConfig;
        import net.lab1024.sa.draw.poolconfig.domain.form.PrizePoolConfigQueryForm;
        import net.lab1024.sa.draw.poolconfig.domain.vo.PrizePoolConfigVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 奖池配置 Dao
 *
 * @Author weolwo
 * @Date 2026-04-19 09:42:12
 * @Copyright weolwo
 */
@Mapper
public interface PrizePoolConfigDao extends BaseMapper<PrizePoolConfig> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizePoolConfigVO> queryPage(Page<?> page, @Param("queryForm") PrizePoolConfigQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizePoolConfigVO> queryList(@Param("queryForm") PrizePoolConfigQueryForm queryForm);

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