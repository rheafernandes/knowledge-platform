package org.sunbird.schema;


import org.leadpony.justify.api.*;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Schema {

    public static String name;
    private static final JsonValidationService service = JsonValidationService.newInstance();
    private static JsonSchema schema;
    private static JsonSchemaReaderFactory schemaReaderFactory;
    private static String basePath = "schema/";

    public Schema(String name, String version) throws Exception {
        this.name = name;
        this.schemaReaderFactory = service.createSchemaReaderFactoryBuilder()
                .withSchemaResolver(this::resolveSchema)
                .build();
        String fileName = name + "-" + version + ".json";
        Path schemaPath = Paths.get(ClassLoader.getSystemResource(basePath + fileName).toURI());
        this.schema = readSchema(schemaPath);
    }

    /**
     * Reads the JSON schema from the specified path.
     *
     * @param path the path to the schema.
     * @return the read schema.
     */
    private JsonSchema readSchema(Path path) {
        try (JsonSchemaReader reader = schemaReaderFactory.createSchemaReader(path)) {
            return reader.read();
        }
    }

    /**
     * Resolves the referenced JSON schema.
     *
     * @param id the identifier of the referenced JSON schema.
     * @return referenced JSON schema.
     */
    private JsonSchema resolveSchema(URI id) {
        // The schema is available in the local filesystem.
        try {
            Path path = Paths.get(ClassLoader.getSystemResource(basePath + id.getPath()).toURI());
            return readSchema(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void validate(String data) {
        String dataWithDefaults = withDefaultValues(data);
        List<String> messages = validate(new StringReader(dataWithDefaults));
        System.out.println("Validation messages: " + messages);
        System.out.println("With Defaults: " + dataWithDefaults);
    }

    public List<String> validate(StringReader input) {
        List<String> messages = new ArrayList<>();
        ProblemHandler handler = service.createProblemPrinter(s -> {messages.add(s);});
        try (JsonReader reader = service.createReader(input, schema, handler)) {
            reader.readValue();
        }
        return messages;
    }

    protected String withDefaultValues(String data) {
        ValidationConfig config = service.createValidationConfig();
        config.withSchema(schema).withDefaultValues(true);
        JsonReaderFactory readerFactory = service.createReaderFactory(config.getAsMap());
        JsonReader reader = readerFactory.createReader(new StringReader(data));
        return reader.readValue().toString();
    }

}
