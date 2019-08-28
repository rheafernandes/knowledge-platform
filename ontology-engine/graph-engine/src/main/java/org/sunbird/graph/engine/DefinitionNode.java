package org.sunbird.graph.engine;

import org.sunbird.graph.engine.dto.Result;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public class DefinitionNode extends BaseDomainObject {

    public DefinitionNode(String graphId, String objectType, String version) {
        super(graphId, objectType, version);
    }

    /**
     *
     * @param data
     * @return
     * @throws Exception
     */
    public Result validate(Map<String, Object> data) throws Exception {
        // TODO: Read JSON Schema of the given object,
        //  execute conditional (if-then-else) statements and update request data.
        ValidationResult jsonResult = SchemaValidatorFactory.getInstance(objectType, version).validate(data);
        // TODO: set audit properties to validated data.
        setSystemProperties(jsonResult.getData());
        return new Result(graphId, objectType, jsonResult);
    }
}
