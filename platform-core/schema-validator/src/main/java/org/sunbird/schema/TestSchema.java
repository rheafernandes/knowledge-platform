package org.sunbird.schema;

import java.util.List;

public class TestSchema {

    public static void main(String args[]) {
        Schema schema = SchemaFactory.getInstance("Object", "1.0");
        List<String> messages = schema.validate("{\"name\": \"Mahesh\", \"mimeType\": \"application/pdf\"}");
        System.out.println("Validation messages: " + " : " + messages);
    }
}
