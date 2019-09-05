package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.mgr.BaseGraphManager;

public class SequenceMembershipRelation extends AbstractRelation {

    public SequenceMembershipRelation(BaseGraphManager manager, String graphId, Node startNode, Node endNode, Map<String, Object> metadata) {
        super(manager, graphId, startNode, endNode, metadata);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SEQUENCE_MEMBERSHIP.relationName();
    }

    @Override
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // check for cycle
			String cyclicCheck = checkCycle(request);
            futures.add(cyclicCheck);
			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
