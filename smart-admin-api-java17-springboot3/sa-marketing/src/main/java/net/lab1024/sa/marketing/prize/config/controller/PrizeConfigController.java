package net.lab1024.sa.marketing.prize.config.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.prize.config.domain.entity.PrizeConfig;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigAddForm;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigQueryForm;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigUpdateForm;
import net.lab1024.sa.marketing.prize.config.domain.vo.PrizeConfigVO;
import net.lab1024.sa.marketing.prize.config.service.PrizeConfigService;
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
 * 业务级-发奖规则与奖品明细表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "业务级-发奖规则与奖品明细表")
@RequestMapping("/prizeConfig")
public class PrizeConfigController {

    private final PrizeConfigService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<PrizeConfigVO>> queryPage(@RequestBody @Valid PrizeConfigQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid PrizeConfigAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid PrizeConfigUpdateForm updateForm) {
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
