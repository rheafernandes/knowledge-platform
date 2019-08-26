package org.sunbird.graph.engine;

import org.sunbird.common.JsonUtils;
import org.sunbird.graph.mgr.BaseGraphManager;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public abstract class BaseManager extends BaseGraphManager {

    /**
     * This fetches the schema for given objectType and version and validates data against the schema.
     *
     * @param objectType
     * @param version
     * @param request
     * @return ValidationResult
     * @throws Exception
     */

//    public ValidationResult validate(String objectType, String version, Map<String, Object> request) throws Exception {
//        ISchemaValidator schema = SchemaValidatorFactory.getInstance(objectType, version);
//        return schema.validate(request);
//    }
}
