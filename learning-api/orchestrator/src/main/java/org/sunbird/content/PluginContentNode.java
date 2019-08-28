package org.sunbird.content;

import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public class PluginContentNode extends CommonContentNode {

    public PluginContentNode(Map<String, Object> data) {
        super(data);
    }

    @Override
    public ValidationResult validate(String operation) throws Exception {
        switch (operation) {
            case "create":
                data.put("identifier", data.get("code"));
                break;
            default:
                break;
        }
        return super.validate(operation);
    }
}
