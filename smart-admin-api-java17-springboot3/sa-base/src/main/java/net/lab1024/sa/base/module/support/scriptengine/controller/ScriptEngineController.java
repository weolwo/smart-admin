package net.lab1024.sa.base.module.support.scriptengine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.constant.SwaggerTagConst;
import net.lab1024.sa.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import net.lab1024.sa.base.module.support.scriptengine.core.EngineFunctionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = SwaggerTagConst.Support.SCRIPT_DOC)
@RequiredArgsConstructor
public class ScriptEngineController {

    private final EngineFunctionRegistry engineFunctionRegistry;

    @Operation(summary = "【用户】脚本引擎-文档查询")
    @GetMapping("/script/engine/view")
    @RepeatSubmit
    public ResponseDTO view() {
        return ResponseDTO.ok(engineFunctionRegistry.exportDocs());
    }
}
