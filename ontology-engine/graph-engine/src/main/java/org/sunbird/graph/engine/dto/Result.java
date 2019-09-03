package org.sunbird.graph.engine.dto;



import org.sunbird.graph.dac.model.Node;

import java.util.Map;

public class Result {

    private Map<String, Object> externalData;
    private String identifier;
    private Node node;

    public Result(String objectType, Node node, Map<String, Object> externalData) {
        this.externalData = externalData;
        this.node = node;
        this.identifier = node.getIdentifier();
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
