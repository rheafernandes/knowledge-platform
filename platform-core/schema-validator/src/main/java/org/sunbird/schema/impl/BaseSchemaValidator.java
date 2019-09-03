package org.sunbird.schema.impl;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

public abstract class BaseSchemaValidator implements ISchemaValidator {

    public static String name;
    public static String version;
    protected static final JsonValidationService service = JsonValidationService.newInstance();
    public static JsonSchema schema;
    protected static JsonSchemaReaderFactory schemaReaderFactory;
    protected static Config config;

    public BaseSchemaValidator(String name, String version) {
        this.name = name;
        this.version = version;
        this.schemaReaderFactory = service.createSchemaReaderFactoryBuilder()
                .withSchemaResolver(this::resolveSchema)
                .build();

    }

    public abstract JsonSchema resolveSchema(URI id);

    /**
     *
     * @return
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Reads the JSON schema from the specified path.
     *
     * @param path the path to the schema.
     * @return the read schema.
     */
    protected JsonSchema readSchema(Path path) {
        try (JsonSchemaReader reader = schemaReaderFactory.createSchemaReader(path)) {
            this.config = ConfigFactory.parseFile(path.toFile());
            return reader.read();
        }
    }

    public ValidationResult validate(Map<String, Object> data) throws Exception {
        String dataWithDefaults = withDefaultValues(JsonUtils.serialize(data));
        List<String> messages = validate(new StringReader(dataWithDefaults));
        Map<String, Object> dataMap = JsonUtils.deserialize(dataWithDefaults, Map.class);
        Map<String, Object> externalData = new HashMap<>();
        Map<String, Object> relations = getRelations(dataMap);
        if (config == null || !config.hasPath("externalProperties")) {
            return new ValidationResult(messages, dataMap, relations, externalData);
        } else {
            Set<String> extProps = config.getObject("externalProperties").keySet();
            externalData = dataMap.entrySet().stream().filter(f -> extProps.contains(f.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dataMap = dataMap.entrySet().stream().filter(f -> !extProps.contains(f.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new ValidationResult(messages, dataMap, relations, externalData);
        }
    }

    private List<String> validate(StringReader input) {
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

    public Map<String, Object> getRelations(Map<String, Object> data) {
        if (this.getConfig().hasPath("relations")) {
            Set<String> relKeys = this.getConfig().getObject("relations").keySet();
            Map<String, Object> relationData = data.entrySet().stream().filter(e -> relKeys.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (String relKey: relKeys) {
                data.remove(relKey);
            }
            return relationData;
        } else {
            return null;
        }
    }
}
