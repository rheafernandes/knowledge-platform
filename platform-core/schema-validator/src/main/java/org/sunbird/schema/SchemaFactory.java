package org.sunbird.schema;

import org.sunbird.schema.impl.LocalSchema;

import java.util.HashMap;
import java.util.Map;

public class SchemaFactory {

    private static Map<String, ISchema> schemaMap = new HashMap<String, ISchema>();

    public static ISchema getInstance(String name, String version) throws Exception {
        String key = getKey(name, version);
        if (schemaMap.containsKey(key)) {
            return schemaMap.get(key);
        } else {
            return initSchema(name, version);
        }
    }

    private static ISchema initSchema(String name, String version) throws Exception {
        ISchema schema = new LocalSchema(name, version);
        schemaMap.put(getKey(name, version), schema);
        return schema;
    }

    private static String getKey(String name, String version) {
        return name +":"+version;
    }
}
