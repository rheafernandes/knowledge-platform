package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.mgr.BaseGraphManager;

public class AssociationRelation extends AbstractRelation {

    public AssociationRelation(BaseGraphManager manager, String graphId, Node startNode, Node endNode, Map<String, Object> metadata) {
        super(manager, graphId, startNode, endNode, metadata);
    }
    
    public AssociationRelation(BaseGraphManager manager, String graphId, Node startNode, Node endNode) {
        super(manager, graphId, startNode, endNode);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.ASSOCIATED_TO.relationName();
    }

    @Override
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // Check node types: start node type should be data node.
            // end node type should be data node
			String startNodeMsg = getNodeTypeFuture(startNode.getIdentifier(), startNode,
					new String[] { SystemNodeTypes.DATA_NODE.name() });
            futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(endNode.getIdentifier(), endNode,
					new String[] { SystemNodeTypes.DATA_NODE.name(), SystemNodeTypes.SET.name() });
            futures.add(endNodeMsg);

            // check if the relation is valid between object type definitions.
			String objectType = getObjectTypeFuture(startNode);
			String endNodeObjectType = getObjectTypeFuture(endNode);
			String objectTypeMessages = validateObjectTypes(objectType, endNodeObjectType);
            futures.add(objectTypeMessages);

			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
