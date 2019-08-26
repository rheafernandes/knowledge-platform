package org.sunbird.schema.impl;


import org.apache.commons.collections4.CollectionUtils;
import org.leadpony.justify.api.*;
import org.sunbird.common.JsonUtils;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.dto.ValidationResult;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SchemaValidator implements ISchemaValidator {

    public static String name;
    public static String version;
    protected static final JsonValidationService service = JsonValidationService.newInstance();
    public static JsonSchema schema;
    protected static List<String> externalKeys = Arrays.asList();
    protected static JsonSchemaReaderFactory schemaReaderFactory;

    public SchemaValidator(String name, String version) {
        this.name = name;
        this.version = version;
        this.schemaReaderFactory = service.createSchemaReaderFactoryBuilder()
                .withSchemaResolver(this::resolveSchema)
                .build();
    }

    public abstract JsonSchema resolveSchema(URI id);


    /**
     * Reads the JSON schema from the specified path.
     *
     * @param path the path to the schema.
     * @return the read schema.
     */
    protected JsonSchema readSchema(Path path) {
        try (JsonSchemaReader reader = schemaReaderFactory.createSchemaReader(path)) {
            return reader.read();
        }
    }

    public ValidationResult validate(Map<String, Object> data) throws Exception {
        String dataWithDefaults = withDefaultValues(JsonUtils.serialize(data));
        List<String> messages = validate(new StringReader(dataWithDefaults));
        Map<String, Object> dataMap = JsonUtils.deserialize(dataWithDefaults, Map.class);
        Map<String, Object> externalData = new HashMap<>();
        if (CollectionUtils.isEmpty(externalKeys)) {
            return new ValidationResult(messages, dataMap, externalData);
        } else {
            externalData = dataMap.entrySet().stream().filter(f -> externalKeys.contains(f.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dataMap = dataMap.entrySet().stream().filter(f -> !externalKeys.contains(f.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new ValidationResult(messages, dataMap, externalData);
        }
    }

    public List<String> validate(StringReader input) {
        List<String> messages = new ArrayList<>();
        ProblemHandler handler = service.createProblemPrinter(s -> {messages.add(s);});
        try (JsonReader reader = service.createReader(input, schema, handler)) {
            reader.readValue();
        }
        return messages;
    }

    public String withDefaultValues(String data) {
        ValidationConfig config = service.createValidationConfig();
        config.withSchema(schema).withDefaultValues(true);
        JsonReaderFactory readerFactory = service.createReaderFactory(config.getAsMap());
        JsonReader reader = readerFactory.createReader(new StringReader(data));
        return reader.readValue().toString();
    }

}
