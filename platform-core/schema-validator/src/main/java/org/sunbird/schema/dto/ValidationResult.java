package org.sunbird.schema.dto;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class ValidationResult {

    private boolean valid = false;
    private List<String> messages;
    private String data;

    public ValidationResult(String data, List<String> messages) {
        this.data = data;
        if (CollectionUtils.isEmpty(messages)) {
            this.valid = true;
        } else {
            this.messages = messages;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "valid: " + valid + " | " + "messages: " +  messages + " | " + " data(with defaults): " + data;
    }
}
