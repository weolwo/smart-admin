package net.lab1024.sa.marketing.prize.config.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.prize.config.domain.entity.PrizeConfig;
        import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigQueryForm;
        import net.lab1024.sa.marketing.prize.config.domain.vo.PrizeConfigVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 业务级-发奖规则与奖品明细表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */
@Mapper
public interface PrizeConfigDao extends BaseMapper<PrizeConfig> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizeConfigVO> queryPage(Page<?> page, @Param("queryForm") PrizeConfigQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizeConfigVO> queryList(@Param("queryForm") PrizeConfigQueryForm queryForm);

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