package org.sunbird.graph.model.nodes;

import java.util.Map;

import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.mgr.BaseGraphManager;

public class RootNode extends AbstractNode {

    public RootNode(BaseGraphManager manager, String graphId, String nodeId, Map<String, Object> metadata) {
        super(manager, graphId, nodeId, metadata);
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.ROOT_NODE.name();
    }

    @Override
    public String getFunctionalObjectType() {
        return null;
    }

}
