package org.sunbird.schema;


import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ValidationConfig;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Schema {

    public static String name;
    private static final JsonValidationService service = JsonValidationService.newInstance();
    private static JsonSchema schema;
    private static ProblemHandler handler;
    private static JsonReaderFactory readerFactory;

    public Schema(String name, String version) throws Exception {
        this.name = name;

        JsonSchema objectSchema = service.readSchema(Paths.get(ClassLoader.getSystemResource("schema/content-schema.json").toURI()));


//                service.readSchema(Paths.get(ClassLoader.getSystemResource("schema/content-schema.json").toURI()));
        this.handler = service.createProblemPrinter(System.out::println);
        ValidationConfig config = service.createValidationConfig();
        config.withSchema(objectSchema).withProblemHandler(handler).withDefaultValues(true);
        this.readerFactory = service.createReaderFactory(config.getAsMap());



    }

    public List<String> validate(String data) {
        List<String> messages = null;
        if (null != schema) {
            JsonReader reader = readerFactory.createReader(new StringReader(data));
            JsonValue value = reader.readValue();
            System.out.println("Value: " + value);
        } else {
            messages = new ArrayList<String>();
            messages.add("schema initialization failed for [" + name + "]");
        }
        return messages;
    }

}
