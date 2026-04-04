package net.lab1024.sa.marketing.prize.group.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.prize.group.domain.entity.PrizeGroup;
        import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupQueryForm;
        import net.lab1024.sa.marketing.prize.group.domain.vo.PrizeGroupVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 业务级-奖励包(大礼包)外壳表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */
@Mapper
public interface PrizeGroupDao extends BaseMapper<PrizeGroup> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizeGroupVO> queryPage(Page<?> page, @Param("queryForm") PrizeGroupQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PrizeGroupVO> queryList(@Param("queryForm") PrizeGroupQueryForm queryForm);

            // ----- 物理删除 -----
                /**
                 * 单个物理删除
                 */
                long deleteById(@Param("id") String id);

                /**
                 * 批量物理删除
                 */
                void batchDelete(@Param("idList") List<String> idList);
}