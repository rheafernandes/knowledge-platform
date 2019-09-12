package org.sunbird.schema.impl;

import com.typesafe.config.ConfigFactory;
import org.leadpony.justify.api.JsonSchema;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

    public class OnlineJsonSchemaValidator extends BaseSchemaValidator {
        private static String basePath = "https://ekstep-public-qa.s3-ap-south-1.amazonaws.com/knowledge-platform/schemas/";

    public OnlineJsonSchemaValidator(String name, String version) throws Exception {
        super(name, version);
        basePath = basePath + name.toLowerCase() + "/" + version + "/";
        loadSchema();
        loadConfig();
    }

    private void loadSchema() throws Exception {
        URLConnection connection;
        connection = new URL(basePath + "schema.json").openConnection();
        InputStream is = connection.getInputStream();
        this.schema = readSchema(is);
        System.out.println(schema.toString());
    }

    private void loadConfig() throws Exception {
        URL url = new URL(basePath + "config.json");
        this.config = ConfigFactory.parseURL(url);

    }

//        private void loadConfig() throws Exception {
//            URI uri = getClass().getClassLoader().getResource( "schemas/content-1.0/" + "config.json").toURI();
//            Path configPath = Paths.get(uri);
//            this.config = ConfigFactory.parseFile(configPath.toFile());
//
//        }

    /**
     * Resolves the referenced JSON schema.
     *
     * @param id the identifier of the referenced JSON schema.
     * @return referenced JSON schema.
     */
    public JsonSchema resolveSchema(URI id) {
        // The schema is available in the local filesystem.
        try {
            URLConnection connection;
            connection = id.toURL().openConnection();
            InputStream is = connection.getInputStream();
            return readSchema(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
