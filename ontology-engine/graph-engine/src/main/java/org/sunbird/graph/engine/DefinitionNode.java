package org.sunbird.graph.engine;

import org.sunbird.common.JsonUtils;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public class DefinitionNode {

    public String objectType;
    public String version;

    public DefinitionNode(String objectType, String version) {
        this.objectType = objectType;
        this.version = version;
    }

    public void setSystemProperties(Map<String, Object> data) {

    }

    public ValidationResult validate(Map<String, Object> data) throws Exception {
        return SchemaValidatorFactory.getInstance(objectType, version).validate(data);
    }
}
