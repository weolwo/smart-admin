package net.lab1024.sa.marketing.task.config.controller;

import net.lab1024.sa.base.common.domain.ValidateList;
import net.lab1024.sa.marketing.task.config.domain.entity.TaskConfig;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigAddForm;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigQueryForm;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigUpdateForm;
import net.lab1024.sa.marketing.task.config.domain.vo.TaskConfigVO;
import net.lab1024.sa.marketing.task.config.service.TaskConfigService;
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
 * 业务级-任务规则配置表 Controller
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "业务级-任务规则配置表")
@RequestMapping("/taskConfig")
public class TaskConfigController {

    private final TaskConfigService Service;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission(":query")
    public ResponseDTO<PageResult<TaskConfigVO>> queryPage(@RequestBody @Valid TaskConfigQueryForm queryForm) {
        return ResponseDTO.ok(Service.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission(":add")
    public ResponseDTO<String> add(@RequestBody @Valid TaskConfigAddForm addForm) {
        return Service.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission(":update")
    public ResponseDTO<String> update(@RequestBody @Valid TaskConfigUpdateForm updateForm) {
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
