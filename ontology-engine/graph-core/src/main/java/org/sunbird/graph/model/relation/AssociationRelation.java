package org.sunbird.graph.model.relation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.mgr.BaseGraphManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			String startNodeMsg = getNodeTypeFuture(startNode,
                    Arrays.asList(SystemNodeTypes.DATA_NODE.name()));
			if(StringUtils.isNotBlank(startNodeMsg))
                futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(endNode,
                    Arrays.asList(SystemNodeTypes.DATA_NODE.name(), SystemNodeTypes.SET.name()));
            if(StringUtils.isNotBlank(endNodeMsg))
                futures.add(endNodeMsg);

            // check if the relation is valid between object type definitions.
			String objectType = startNode.getObjectType();
			String endNodeObjectType = endNode.getObjectType();
			String objectTypeMessages = validateObjectTypes(objectType, endNodeObjectType);
            if(StringUtils.isNotBlank(objectTypeMessages))
                futures.add(objectTypeMessages);

            Map<String, List<String>> map = new HashMap<>();
            if(CollectionUtils.isNotEmpty(futures)){
                map.put(getStartNode().getIdentifier(), futures);
            }
			return map;
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
