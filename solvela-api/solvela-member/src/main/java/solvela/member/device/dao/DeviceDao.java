package solvela.member.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import solvela.member.Device;
import solvela.member.device.domain.dto.DeviceMemberDTO;
import solvela.member.device.domain.query.DeviceQuery;

import java.util.List;

@Mapper
public interface DeviceDao extends BaseMapper<Device> {

    /**
     * 刷新最后活跃时间。
     *
     * <h3>为什么是一条手写 UPDATE，而不是查出来改再存回去</h3>
     * 读-改-写要两趟 SQL，而且两个请求同时到会互相覆盖。这里只动一列，
     * 一条语句就够，也不需要事务。
     *
     * <p>🔴 <b>节流由调用方负责</b>（{@code DeviceService.touch}）。
     * 每个请求都执行这条语句会把 {@code t_device} 写成热点表 ——
     * 一台活跃设备一天几千次 UPDATE，而这一列的精度到小时就够用了。
     *
     * @return 影响行数。0 表示这个设备号在库里不存在 —— 令牌验签是通过的，
     *         说明有人拿着一个签名合法但记录已被删的令牌，值得警告
     */
    @Update("""
            UPDATE t_device
               SET last_active_time = NOW()
             WHERE device_id = #{deviceId}
            """)
    int touchActive(@Param("deviceId") String deviceId);

    /**
     * 处置档迁移。<b>带上原值做条件</b>，这一点很要紧。
     *
     * <p>不带 {@code AND status = #{expected}} 的话，「自动降档」会覆盖掉
     * 人工封禁：客服刚把一台设备封了（status=2），下一次命中限流规则就把它
     * 改回观察档（status=1）—— 封禁悄悄失效，而没有任何迹象。
     *
     * @return 影响行数。0 表示当前状态不是 expected，本次迁移<b>没有发生</b>
     */
    @Update("""
            UPDATE t_device
               SET status = #{target}, remark = #{remark}, operator = #{operator}
             WHERE device_id = #{deviceId} AND status = #{expected}
            """)
    int transitStatus(@Param("deviceId") String deviceId,
                      @Param("expected") int expected,
                      @Param("target") int target,
                      @Param("remark") String remark,
                      @Param("operator") String operator);

    /** 设备列表。条件在 DeviceMapper.xml 里，与统计共用一份。 */
    List<Device> queryPage(Page<?> page, @Param("queryForm") DeviceQuery queryForm);

    /**
     * 这台设备碰过哪些账号。
     *
     * <p>数据来自 {@code t_member_login_log}，不是本表 —— 设备表刻意不带 member_id
     * （一台设备本来就可能有多个账号，放一个只能记住最后一个）。
     */
    List<DeviceMemberDTO> listMembers(@Param("deviceId") String deviceId, @Param("limit") int limit);
}
