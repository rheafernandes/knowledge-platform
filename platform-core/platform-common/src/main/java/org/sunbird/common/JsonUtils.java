package org.sunbird.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String serialize(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String value, Class<T> clazz) throws Exception {
        return mapper.readValue(value, clazz);
    }

}
