package net.lab1024.sa.marketing.riskmanager.proposal.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.riskmanager.proposal.domain.entity.PromotionProposal;
        import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalQueryForm;
        import net.lab1024.sa.marketing.riskmanager.proposal.domain.vo.PromotionProposalVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 资产域-统一发奖提案控制表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */
@Mapper
public interface PromotionProposalDao extends BaseMapper<PromotionProposal> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PromotionProposalVO> queryPage(Page<?> page, @Param("queryForm") PromotionProposalQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PromotionProposalVO> queryList(@Param("queryForm") PromotionProposalQueryForm queryForm);

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