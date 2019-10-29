package org.sunbird.schema.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.internal.schema.BasicJsonSchema;
import org.sunbird.common.JsonUtils;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.dto.ValidationResult;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * Gets all the properties which are of type json
     * @return
     */
    public List<String> getJsonProps() {
        try {
            return ((Map<String, Object>)(new ObjectMapper().readValue(((BasicJsonSchema) schema).get("properties")
                    .getValueAsJson().asJsonObject().toString(), Map.class))).entrySet().stream().filter(entry -> StringUtils.equalsIgnoreCase("object", (String)((Map<String, Object>)entry.getValue()).get("type"))).map(entry -> entry.getKey()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

//        List<String> value = ((BasicJsonSchema) schema).get("properties")
//                .getValueAsJson().asJsonObject().entrySet().stream()
//                .filter(entry -> StringUtils.equalsIgnoreCase((String) ((Map<String, Object>) entry.getValue()).get("type"), "object"))
//                .map(entry -> entry.getKey())
//                .collect(Collectors.toList());
//        return value;
    }

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

    public ValidationResult getStructuredData(Map<String, Object> input) {
        Map<String, Object> relations = getRelations(input);
        Map<String, Object> externalData = getExternalProps(input);
        return new ValidationResult(input, relations, externalData);
    }

    public ValidationResult validate(Map<String, Object> data) throws Exception {
        String dataWithDefaults = withDefaultValues(JsonUtils.serialize(data));
        List<String> messages = validate(new StringReader(dataWithDefaults));
        Map<String, Object> dataMap = JsonUtils.deserialize(dataWithDefaults, Map.class);
        Map<String, Object> externalData = getExternalProps(dataMap);
        Map<String, Object> relations = getRelations(dataMap);
        return new ValidationResult(messages, dataMap, relations, externalData);
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

    private Map<String, Object> getExternalProps(Map<String, Object> input) {
        Map<String, Object> externalData = new HashMap<>();
        if (config != null && config.hasPath("externalProperties")) {
            Set<String> extProps = config.getObject("externalProperties").keySet();
            externalData = input.entrySet().stream().filter(f -> extProps.contains(f.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            input.keySet().removeAll(extProps);
        }
        return externalData;

    }

    private Map<String, Object> getRelations(Map<String, Object> data) {
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
