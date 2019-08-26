package org.sunbird.content;

import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public class ECMLContentNode extends CommonContentNode {

    public ECMLContentNode(Map<String, Object> data) {
        super(data);
    }
    @Override
    public ValidationResult validate(String operation) throws Exception {
        switch (operation) {
            case "create":
                data.put("contentEncoding", "gzip");
                data.put("version", 2);
                break;
            default:
                break;
        }

        return super.validate(operation);
    }
}
