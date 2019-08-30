package org.sunbird.schema;

import org.sunbird.schema.dto.ValidationResult;

import java.util.List;
import java.util.Map;

public interface ISchemaValidator {

    ValidationResult validate(Map<String, Object> data) throws Exception;

    ExternalSchema getExternalSchema();
}
