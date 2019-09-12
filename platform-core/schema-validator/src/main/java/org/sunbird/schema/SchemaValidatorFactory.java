package org.sunbird.schema;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.schema.impl.JsonSchemaValidator;
import org.sunbird.schema.impl.OnlineJsonSchemaValidator;

import java.util.HashMap;
import java.util.Map;

public class SchemaValidatorFactory {

    private static String schemaType = Platform.config.getString("content.schema.location");
    private static Map<String, ISchemaValidator> schemaMap = new HashMap<String, ISchemaValidator>();

    public static ISchemaValidator getInstance(String name, String version) throws Exception {
        String key = getKey(name, version);
        if (schemaMap.containsKey(key)) {
            return schemaMap.get(key);
        } else {
            return initSchema(name, version);
        }
    }

    private static ISchemaValidator initSchema(String name, String version) throws Exception {
        ISchemaValidator schema;
        if (StringUtils.equalsIgnoreCase(schemaType, "remote"))
            schema = new OnlineJsonSchemaValidator(name, version);
        else
            schema = new JsonSchemaValidator(name, version);
        schemaMap.put(getKey(name, version), schema);
        return schema;
    }

    private static String getKey(String name, String version) {
        return name +":"+version;
    }
}
