package org.sunbird.schema.impl;

import org.apache.commons.collections4.MapUtils;
import org.leadpony.justify.api.JsonSchema;
import org.sunbird.common.JsonUtils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


public class LocalSchemaValidator extends SchemaValidator {

    private static String basePath = "schema/";

    public LocalSchemaValidator(String name, String version) throws Exception {
        super(name, version);
        String fileName = name.toLowerCase() + "-" + version + ".json";
        URI uri = getClass().getClassLoader().getResource( basePath + fileName).toURI();
        Path schemaPath = Paths.get(uri);
        this.schema = readSchema(schemaPath);
        Map<String, Object> schemaMap = JsonUtils.deserialize(schema.toString(), Map.class);
        if (MapUtils.isNotEmpty(schemaMap) && null != schemaMap.get("externalProperties")) {
            this.externalKeys = (List<String>) schemaMap.get("externalProperties");
        }
    }

    public List<String> externalProperties() {
        return externalKeys;
    }


    /**
     * Resolves the referenced JSON schema.
     *
     * @param id the identifier of the referenced JSON schema.
     * @return referenced JSON schema.
     */
    public JsonSchema resolveSchema(URI id) {
        // The schema is available in the local filesystem.
        try {
            Path path = Paths.get( getClass().getClassLoader().getResource(basePath + id.getPath()).toURI());
            return readSchema(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
