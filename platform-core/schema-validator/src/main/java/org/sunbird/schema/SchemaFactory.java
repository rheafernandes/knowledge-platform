package org.sunbird.schema;

import java.util.HashMap;
import java.util.Map;

public class SchemaFactory {

    private static Map<String, Schema> schemaMap = new HashMap<String, Schema>();

    public static Schema getInstance(String name, String version) throws Exception {
        String key = getKey(name, version);
        if (schemaMap.containsKey(key)) {
            return schemaMap.get(key);
        } else {
            Schema schema = new Schema(name, version);
            schemaMap.put(key, schema);
            return schema;
        }
    }

    private static String getKey(String name, String version) {
        return name +":"+version;
    }
}
