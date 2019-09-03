package org.sunbird.schema.dto;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

public class ValidationResult {

    private boolean valid = false;
    private List<String> messages;
    private Map<String, Object> metadata;
    private Map<String, Object> externalData;
    private Map<String, Object> relations;

    public ValidationResult(List<String> messages, Map<String, Object> metadata, Map<String, Object> relations, Map<String, Object> externalData) {
        if (CollectionUtils.isEmpty(messages)) {
            this.valid = true;
        } else {
            this.messages = messages;
        }
        this.metadata = metadata;
        this.relations = relations;
        this.externalData = externalData;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return messages;
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

    @Override
    public String toString() {
        return "valid: " + valid + " | " + "messages: " +  messages + " | " + " metadata(with defaults): " + metadata;
    }
}
