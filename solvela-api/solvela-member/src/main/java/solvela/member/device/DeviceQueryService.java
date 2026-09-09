package solvela.member.device;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solvela.base.domain.PageResult;
import solvela.base.dao.SolvelaPageUtil;
import solvela.member.Device;
import solvela.member.device.dao.DeviceDao;
import solvela.member.device.domain.dto.DeviceMemberDTO;
import solvela.member.device.domain.query.DeviceQuery;

import java.util.List;

/**
 * 设备的<b>只读视图</b>：后台查询与关联分析。
 *
 * <h3>为什么与 DeviceDispositionService 分开</h3>
 * 一个是<b>写</b>（处置：降档、回档、封禁），一个是<b>读</b>（后台看）。
 * 混在一起的话，一个只想查列表的调用方会同时拿到 {@code disposeManually}，
 * 而那是能把一台设备封掉的方法 —— 能力应该跟着用途走。
 *
 * @Date 2026-09-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceQueryService {

    /**
     * 「碰过哪些账号」最多列多少个。
     *
     * <p>一台被刷子用的设备可能关联几百个账号，不设上限这个接口会把后台点崩；
     * 而运营看前 50 个就已经能下判断了 —— 真有 50 个账号共用一台机器，
     * 第 51 个是什么已经不影响结论。
     */
    private static final int MEMBER_LIMIT = 50;

    private final DeviceDao deviceDao;

    public PageResult<Device> queryPage(DeviceQuery query) {
        Page<?> page = SolvelaPageUtil.convert2PageQuery(query);
        List<Device> list = deviceDao.queryPage(page, query);
        return SolvelaPageUtil.convert2PageResult(page, list);
    }

    /**
     * 这台设备碰过哪些账号。<b>整套设备方案最终要产出的就是这张表</b>。
     *
     * <p>方案里那句「有了 device_id，『一台设备碰过哪些账号』才查得出来，
     * 而那正是将来判断『要不要花钱买厂商指纹』的唯一依据」—— 说的就是这个查询。
     */
    public List<DeviceMemberDTO> listMembers(String deviceId) {
        return deviceDao.listMembers(deviceId, MEMBER_LIMIT);
    }
}
