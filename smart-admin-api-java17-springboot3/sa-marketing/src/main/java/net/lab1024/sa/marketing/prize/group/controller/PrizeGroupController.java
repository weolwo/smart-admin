package net.lab1024.sa.marketing.prize.group.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.prize.group.domain.entity.PrizeGroup;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupAddForm;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupQueryForm;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupUpdateForm;
import net.lab1024.sa.marketing.prize.group.domain.vo.PrizeGroupVO;
import net.lab1024.sa.marketing.prize.group.service.PrizeGroupService;
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
 * 业务级-奖励包(大礼包)外壳表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "业务级-奖励包(大礼包)外壳表")
@RequestMapping("/prizeGroup")
public class PrizeGroupController {

    private final PrizeGroupService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<PrizeGroupVO>> queryPage(@RequestBody @Valid PrizeGroupQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid PrizeGroupAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid PrizeGroupUpdateForm updateForm) {
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
