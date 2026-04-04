package net.lab1024.sa.marketing.riskmanager.proposal.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.entity.PromotionProposal;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalAddForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalQueryForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalUpdateForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.vo.PromotionProposalVO;
import net.lab1024.sa.marketing.riskmanager.proposal.service.PromotionProposalService;
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
 * 资产域-统一发奖提案控制表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "资产域-统一发奖提案控制表")
@RequestMapping("/promotionProposal")
public class PromotionProposalController {

    private final PromotionProposalService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<PromotionProposalVO>> queryPage(@RequestBody @Valid PromotionProposalQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid PromotionProposalAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid PromotionProposalUpdateForm updateForm) {
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
