package org.sunbird.content;

import org.sunbird.schema.dto.ValidationResult;

public interface IContentNode {

    public ValidationResult validate(String operation) throws Exception;
}
