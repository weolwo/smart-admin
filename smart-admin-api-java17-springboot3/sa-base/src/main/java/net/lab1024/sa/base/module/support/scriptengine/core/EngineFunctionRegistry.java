package net.lab1024.sa.base.module.support.scriptengine.core;

import net.lab1024.sa.base.module.support.scriptengine.domain.EngineFunctionMeta;
import net.lab1024.sa.base.module.support.scriptengine.domain.ScriptFunctionDocDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🚀 规则函数中央注册表 (Single Source of Truth)
 */
@Component
public class EngineFunctionRegistry {

    // 线程安全的底层存储
    private final Map<String, EngineFunctionMeta> functionMap = new ConcurrentHashMap<>();

    /**
     * 注册元数据
     */
    public void register(EngineFunctionMeta meta) {
        functionMap.put(meta.getFunctionName(), meta);
    }

    /**
     * 获取所有的函数元数据（供引擎层绑定使用）
     */
    public List<EngineFunctionMeta> getAllFunctions() {
        return new ArrayList<>(functionMap.values());
    }

    /**
     * 🌟 一键导出纯净的 API 文档（供 Controller 吐给前端 Monaco Editor）
     */
    public Map<String, List<ScriptFunctionDocDTO>> exportDocs() {
        return functionMap.values().stream().map(meta -> {
            ScriptFunctionDocDTO doc = new ScriptFunctionDocDTO();
            doc.setHandlerName(meta.getHandlerName());
            doc.setFunctionName(meta.getFunctionName());
            doc.setDescription(meta.getDescription());
            doc.setReturnType(meta.getReturnType());
            doc.setParams(meta.getParams());
            return doc;
        }).collect(Collectors.groupingBy(e -> e.getHandlerName()));
    }
}