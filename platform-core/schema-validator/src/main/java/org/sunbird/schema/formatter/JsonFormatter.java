package org.sunbird.schema.formatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.spi.FormatAttribute;

import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;

public class JsonFormatter implements FormatAttribute {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public InstanceType valueType() {
       return InstanceType.STRING;
    }

    @Override
    public boolean test(JsonValue value) {
        try {
            String str = ((JsonString) value).getString();
            Map<String, Object> data = mapper.readValue(str, new TypeReference<Map<String, Object>>() {
            });
            return MapUtils.isNotEmpty(data) ? true : false;
        } catch (Exception e) {
            return false;
        }
    }
}
