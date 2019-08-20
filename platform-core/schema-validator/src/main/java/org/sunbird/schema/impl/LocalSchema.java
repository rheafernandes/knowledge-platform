package org.sunbird.schema.impl;

import org.leadpony.justify.api.JsonSchema;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;



public class LocalSchema extends Schema {

    private static String basePath = "schema/";

    public LocalSchema(String name, String version) throws Exception {
        super(name, version);
        String fileName = name + "-" + version + ".json";
        Path schemaPath = Paths.get(ClassLoader.getSystemResource(basePath + fileName).toURI());
        this.schema = readSchema(schemaPath);
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
            Path path = Paths.get(ClassLoader.getSystemResource(basePath + id.getPath()).toURI());
            return readSchema(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
