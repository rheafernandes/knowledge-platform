package org.sunbird.schema;

import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Schema {

    public static String name;
    private org.everit.json.schema.Schema schema;

    public Schema(String name, String version) {
        this.name = name;
        try (InputStream inputStream = Schema.class.getClassLoader().getResourceAsStream("schema/content-schema.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            SchemaLoader schemaLoader = SchemaLoader.builder().schemaClient(SchemaClient.classPathAwareClient())
                    .schemaJson(rawSchema).resolutionScope("classpath://schema/").draftV7Support().build();
            schema = schemaLoader.load().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> validate(String data) {
        List<String> messages = null;
        if (null != schema) {
            try {
                schema.validate(new JSONObject(data));
            } catch (ValidationException e) {
                messages = e.getAllMessages();
            }
        } else {
            messages = new ArrayList<String>();
            messages.add("schema initialization failed for [" + name + "]");
        }
        return messages;
    }

}
