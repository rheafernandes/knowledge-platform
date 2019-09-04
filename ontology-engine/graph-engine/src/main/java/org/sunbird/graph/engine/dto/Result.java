package org.sunbird.graph.engine.dto;



import org.sunbird.graph.dac.model.Node;

public class Result {

    private String identifier;
    private ProcessingNode node;

    public Result(String objectType, ProcessingNode node) {
        this.node = node;
        this.identifier = node.getIdentifier();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Node getNode() {
        return node;
    }


}
