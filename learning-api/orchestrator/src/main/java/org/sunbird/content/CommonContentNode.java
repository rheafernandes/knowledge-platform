package org.sunbird.content;

import org.sunbird.graph.engine.DefinitionNode;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public class CommonContentNode extends DefinitionNode implements IContentNode {

    protected Map<String, Object> data;

    public CommonContentNode(Map<String, Object> data) {
        super("content", "1.0");
        this.data = data;
    }

    public ValidationResult validate(String operation) throws Exception {
        switch (operation) {
            case "create":
                data.put("contentEncoding", "identity");
                data.put("contentDisposition", "inline");
                data.put("osId", "org.ekstep.quiz.app");
                data.put("version", 1);
                break;
            default:
                break;
        }
        return super.validate(data);
    }

}
