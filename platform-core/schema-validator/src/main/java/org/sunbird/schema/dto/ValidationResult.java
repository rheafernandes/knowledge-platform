package org.sunbird.schema.dto;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

public class ValidationResult {

    private boolean valid = false;
    private List<String> messages;
    private Map<String, Object> data;
    private Map<String, Object> externalData;

    public ValidationResult(List<String> messages, Map<String, Object> data, Map<String, Object> externalData) {
        if (CollectionUtils.isEmpty(messages)) {
            this.valid = true;
        } else {
            this.messages = messages;
        }
        this.data = data;
        this.externalData = externalData;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getExternalData() {
        return externalData;
    }

    @Override
    public String toString() {
        return "valid: " + valid + " | " + "messages: " +  messages + " | " + " data(with defaults): " + data;
    }
}
