package org.sunbird.graph.engine.dto;


import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.schema.dto.ValidationResult;

import java.util.List;
import java.util.Map;

public class Result {

    private Map<String, Object> externalData;
    private String identifier;
    private Node node;

    public Result(String graphId, String objectType, ValidationResult result) {
        this.externalData = result.getExternalData();
        this.identifier = (String) result.getMetadata().get("identifier");
        if (StringUtils.isBlank(identifier)) {
            this.identifier = Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp());
        }
        this.node = new Node(this.identifier, SystemNodeTypes.DATA_NODE.name(), objectType);
        node.setMetadata(result.getMetadata());
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, Object> getExternalData() {
        return externalData;
    }

    public Node getNode() {
        return node;
    }


}
