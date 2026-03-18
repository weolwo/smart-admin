package net.lab1024.sa.base.common.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import net.lab1024.sa.base.module.support.datamasking.DataMasking;
import net.lab1024.sa.base.module.support.datamasking.DataMaskingTypeEnum;
import net.lab1024.sa.base.module.support.datamasking.DataMaskingUtil;

import java.io.IOException;

public class DataMaskingSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final DataMaskingTypeEnum type;

    // 默认构造函数（Jackson 必须）
    public DataMaskingSerializer() {
        this.type = DataMaskingTypeEnum.COMMON;
    }

    public DataMaskingSerializer(DataMaskingTypeEnum dataMaskingSerializer) {
        this.type = dataMaskingSerializer;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // 直接调用脱敏逻辑，不涉及任何反射 set 操作
        gen.writeString(DataMaskingUtil.apply(value, type));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            DataMasking annotation = property.getAnnotation(DataMasking.class);
            if (annotation != null) {
                // 重点：每次返回一个新实例，确保线程安全，且不同字段互不干扰
                return new DataMaskingSerializer(annotation.value());
            }
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}