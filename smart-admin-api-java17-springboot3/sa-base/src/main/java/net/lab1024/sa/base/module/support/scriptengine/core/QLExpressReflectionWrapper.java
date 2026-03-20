package net.lab1024.sa.base.module.support.scriptengine.core;

import com.alibaba.qlexpress4.runtime.Parameters;
import com.alibaba.qlexpress4.runtime.QContext;
import com.alibaba.qlexpress4.runtime.function.CustomFunction;
import net.lab1024.sa.base.module.support.scriptengine.domain.EngineFunctionMeta;

import java.lang.reflect.Method;

/**
 * 专为 QLExpress 4.1.0 打造的优雅反射桥接器
 */
public class QLExpressReflectionWrapper implements CustomFunction {

    private final Object targetBean;
    private final Method method;
    private final int paramCount;

    public QLExpressReflectionWrapper(EngineFunctionMeta meta) {
        this.targetBean = meta.getTargetBean();
        this.method = meta.getMethod();
        this.paramCount = meta.getMethod().getParameterCount();
        // 压榨性能：取消 Java 语言访问检查，提升反射调用速度
        this.method.setAccessible(true);
    }

    @Override
    public Object call(QContext qContext, Parameters parameters) throws Throwable {
        // 校验脚本传参个数是否匹配
        if (parameters.size() != paramCount) {
            throw new IllegalArgumentException(
                    String.format("函数执行报错: 参数个数不匹配! 期望 %d 个, 实际传入 %d 个", paramCount, parameters.size())
            );
        }

        // 提取 4.x 的动态参数
        Object[] args = new Object[paramCount];
        for (int i = 0; i < paramCount; i++) {
            args[i] = parameters.get(i);
        }

        // 执行真实 Java 方法！
        return method.invoke(targetBean, args);
    }
}