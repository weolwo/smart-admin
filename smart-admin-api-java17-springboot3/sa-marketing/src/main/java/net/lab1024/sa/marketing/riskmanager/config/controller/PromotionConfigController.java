package net.lab1024.sa.marketing.riskmanager.config.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.riskmanager.config.domain.entity.PromotionConfig;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigAddForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigQueryForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigUpdateForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.vo.PromotionConfigVO;
import net.lab1024.sa.marketing.riskmanager.config.service.PromotionConfigService;
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
 * 资产域-优惠配置(预算与风控兜底)表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "资产域-优惠配置(预算与风控兜底)表")
@RequestMapping("/promotionConfig")
public class PromotionConfigController {

    private final PromotionConfigService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<PromotionConfigVO>> queryPage(@RequestBody @Valid PromotionConfigQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid PromotionConfigAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid PromotionConfigUpdateForm updateForm) {
        return Service.update(updateForm);
    }

    @Operation(summary = "批量删除")
    @PostMapping("/batchDelete")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@RequestBody ValidateList<String> idList) {
        return Service.batchDelete(idList);
    }

    @Operation(summary = "单个删除")
    @GetMapping("/delete/{id}")
    @SaCheckPermission(":delete")
    public ResponseDTO<String> batchDelete(@PathVariable String id) {
        return Service.delete(id);
    }
}
