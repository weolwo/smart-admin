package net.lab1024.sa.marketing.ledger.physical.dao;

        import java.util.List;
        import net.lab1024.sa.marketing.ledger.physical.domain.entity.PhysicalDelivery;
        import net.lab1024.sa.marketing.ledger.physical.domain.form.PhysicalDeliveryQueryForm;
        import net.lab1024.sa.marketing.ledger.physical.domain.vo.PhysicalDeliveryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 资产域-实物发货物流表 Dao
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */
@Mapper
public interface PhysicalDeliveryDao extends BaseMapper<PhysicalDelivery> {

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PhysicalDeliveryVO> queryPage(Page<?> page, @Param("queryForm") PhysicalDeliveryQueryForm queryForm);

    /**
     * 列表查询 (无分页)
     *
     * @param queryForm 查询表单
     * @return 列表数据
     */
    List<PhysicalDeliveryVO> queryList(@Param("queryForm") PhysicalDeliveryQueryForm queryForm);

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