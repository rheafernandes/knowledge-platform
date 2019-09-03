package org.sunbird.schema.dto;

import org.apache.commons.collections4.CollectionUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationResult {

    private Map<String, Object> metadata;
    private Map<String, Object> externalData;
    private Map<String, Object> relations;

    public ValidationResult(List<String> messages, Map<String, Object> metadata, Map<String, Object> relations, Map<String, Object> externalData) throws Exception {
        if (CollectionUtils.isEmpty(messages)) {
            this.metadata = metadata;
            this.relations = relations;
            this.externalData = externalData;
        } else {
            throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Validation errors.", new HashMap<String, Object>(){{
               put("messages", messages);
            }});
        }

    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getRelations() {
        return relations;
    }

    public Map<String, Object> getExternalData() {
        return externalData;
    }

}
