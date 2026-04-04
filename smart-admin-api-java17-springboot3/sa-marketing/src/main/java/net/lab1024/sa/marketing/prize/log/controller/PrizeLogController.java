package net.lab1024.sa.marketing.prize.log.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.prize.log.domain.entity.PrizeLog;
import net.lab1024.sa.marketing.prize.log.domain.form.PrizeLogAddForm;
import net.lab1024.sa.marketing.prize.log.domain.form.PrizeLogQueryForm;
import net.lab1024.sa.marketing.prize.log.domain.form.PrizeLogUpdateForm;
import net.lab1024.sa.marketing.prize.log.domain.vo.PrizeLogVO;
import net.lab1024.sa.marketing.prize.log.service.PrizeLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/**
 * 资产域-奖励发放执行明细与快照表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "资产域-奖励发放执行明细与快照表")
@RequestMapping("/prizeLog")
public class PrizeLogController {

    private final PrizeLogService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<PrizeLogVO>> queryPage(@RequestBody @Valid PrizeLogQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid PrizeLogAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid PrizeLogUpdateForm updateForm) {
        return Service.update(updateForm);
    }

    @Operation(summary = "批量删除")
    @PostMapping("/batchDelete")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@RequestBody ValidateList<Long> idList) {
        return Service.batchDelete(idList);
    }

    @Operation(summary = "单个删除")
    @GetMapping("/delete/{id}")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@PathVariable Long id) {
        return Service.delete(id);
    }
}
