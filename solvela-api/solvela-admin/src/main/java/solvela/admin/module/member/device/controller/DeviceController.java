package solvela.admin.module.member.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solvela.admin.auth.CurrentEmployee;
import solvela.admin.module.member.device.domain.form.DeviceDisposeForm;
import solvela.base.domain.PageResult;
import solvela.exception.BusinessException;
import solvela.member.Device;
import solvela.member.device.DeviceDispositionService;
import solvela.member.device.DeviceQueryService;
import solvela.member.device.domain.dto.DeviceMemberDTO;
import solvela.member.device.domain.query.DeviceQuery;
import solvela.web.RequiresPermission;

import java.util.List;

/**
 * 设备。
 *
 * <h3>这个模块补的是「数据看得见」那一半</h3>
 * {@code t_device} 从 2026-09-08 起就在写了，但后台没有任何入口 ——
 * 也就是说这张表<b>只有开发能用 SQL 看</b>。而它要回答的那几个问题
 * （每天签发多少台？哪些 IP 在批量领？这台设备碰过哪些账号？）
 * 恰恰是运营和风控要看的。
 *
 * @Date 2026-09-10
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "设备（服务端签发，防刷用）")
@RequestMapping("/member/device")
public class DeviceController {

    private final DeviceQueryService deviceQueryService;

    private final DeviceDispositionService dispositionService;

    @Operation(summary = "分页查询设备")
    @PostMapping("/queryPage")
    @RequiresPermission("member:query")
    public PageResult<Device> queryPage(@RequestBody @Valid DeviceQuery queryForm) {
        return deviceQueryService.queryPage(queryForm);
    }

    /**
     * 这台设备碰过哪些账号。
     *
     * <p>🔴 <b>整套设备方案最终要产出的就是这张表</b>：一台设备下挂着二十个账号，
     * 和二十台设备各挂一个账号，是完全不同的两件事 —— 而在 device_id 存在之前，
     * 这个问题根本问不出口。
     */
    @Operation(summary = "这台设备碰过哪些账号（最近 50 个）")
    @GetMapping("/members/{deviceId}")
    @RequiresPermission("member:query")
    public List<DeviceMemberDTO> members(@PathVariable String deviceId) {
        return deviceQueryService.listMembers(deviceId);
    }

    /**
     * 人工处置。
     *
     * <p>操作人取自当前登录员工，<b>不收前端传的</b> —— {@code t_device.operator}
     * 这一列存在的唯一理由就是事后追得到人，可被伪造就没有价值。
     */
    @Operation(summary = "人工处置：封禁 / 解封 / 推进观察档 @author 客服")
    @PostMapping("/dispose")
    @RequiresPermission("member:update")
    public String dispose(@RequestBody @Valid DeviceDisposeForm form) {
        String operator = CurrentEmployee.nameOrNull();
        if (operator == null || operator.isBlank()) {
            // 取不到当前员工时【拒绝】，而不是记一个匿名处置 ——
            // 一条没有操作人的封禁记录，事后没有任何办法追溯
            throw new BusinessException("取不到当前操作人，无法处置");
        }
        boolean changed = dispositionService.disposeManually(
                form.getDeviceId(), form.getStatus(), form.getRemark(), operator);
        return changed ? "已处置" : "没有这个设备号";
    }
}
