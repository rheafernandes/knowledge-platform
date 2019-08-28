package org.sunbird.graph.engine.dto;


import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.schema.dto.ValidationResult;

import java.util.List;
import java.util.Map;

public class Result {

    private boolean valid = false;
    private List<String> messages;
    private Map<String, Object> externalData;
    private Node node;

    public Result(String graphId, String objectType, ValidationResult result) {
        this.valid = result.isValid();
        this.externalData = result.getExternalData();
        String identifier = (String) result.getData().get("identifier");
        if (StringUtils.isBlank(identifier)) {
            identifier = Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp());
        }
        this.node = new Node(identifier, SystemNodeTypes.DATA_NODE.name(), objectType);
        node.setMetadata(result.getData());
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public Map<String, Object> getExternalData() {
        return externalData;
    }

    public Node getNode() {
        return node;
    }


}
