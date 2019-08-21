package org.sunbird.schema;

import org.sunbird.schema.dto.ValidationResult;

import java.io.StringReader;
import java.util.List;

public interface ISchemaValidator {

    ValidationResult validate(String data);

    List<String> validate(StringReader input);

    String withDefaultValues(String data);
}
