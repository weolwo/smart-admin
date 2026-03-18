package net.lab1024.sa.base.module.support.datamasking;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 脱敏计算工具类 (重构后的工业级版本)
 * 职责：只做字符串截取，不做反射，不改对象
 */
public class DataMaskingUtil {

    public static String apply(Object value, DataMaskingTypeEnum type) {
        if (value == null) {
            return null;
        }
        String origin = String.valueOf(value);
        if (origin.isEmpty()) {
            return origin;
        }

        // 根据类型直接匹配，跳过所有复杂的反射逻辑
        switch (type) {
            case PHONE:
                return DesensitizedUtil.mobilePhone(origin);
            case CHINESE_NAME:
                return DesensitizedUtil.chineseName(origin);
            case ID_CARD:
                return DesensitizedUtil.idCardNum(origin, 6, 2);
            case EMAIL:
                return DesensitizedUtil.email(origin);
            case PASSWORD:
                return "******";
            case BANK_CARD:
                return DesensitizedUtil.bankCard(origin);
            default:
                // 自定义规则：保留前1/3和后1/3，中间打码
                return defaultMask(origin);
        }
    }

    private static String defaultMask(String value) {
        int len = value.length();
        if (len <= 1) return "*";
        if (len <= 4) return StrUtil.hide(value, 1, len);
        // 动态计算遮罩区间，性能远高于原版的 if-else 嵌套
        return StrUtil.hide(value, len / 4, len - len / 4);
    }
}