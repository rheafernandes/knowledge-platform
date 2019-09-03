package org.sunbird.graph.util;

import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeValidator {
    //TODO: GRAPH_ID can be fetched from configuration
    private static final String GRAPH_ID = "domain";

    public static List<Map<String, Object>> validate(List<String> identifiers) {
        List<Map<String, Object>> result;
        List<Node> nodes = getDataNodes(identifiers);
        if (nodes.size() != identifiers.size()) {
            List<String> invalidIds = identifiers.stream().filter(id -> nodes.stream().noneMatch(node -> node.getIdentifier().equals(id)))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Node Not Found with Identifier " + invalidIds);
        } else {
            result = nodes.stream().map(node -> new HashMap<String, Object>() {{
                put(GraphDACParams.identifier.name(), node.getIdentifier());
                put(GraphDACParams.objectType.name(), node.getObjectType());
                put(GraphDACParams.nodeType.name(), node.getNodeType());
            }}).collect(Collectors.toList());
        }
        return result;
    }

    private static List<Node> getDataNodes(List<String> identifiers) {
        List<Node> nodes = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        MetadataCriterion mc = null;
        if (identifiers.size() == 1) {
            mc = MetadataCriterion
                    .create(Arrays.asList(new Filter("identifier", SearchConditions.OP_EQUAL, identifiers.get(0))));
        } else {
            mc = MetadataCriterion.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_IN, identifiers)));
        }
        searchCriteria.addMetadata(mc);
        searchCriteria.setCountQuery(false);
        try {
            nodes = Neo4JBoltSearchOperations.getNodeByUniqueIds(GRAPH_ID, searchCriteria);
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_PROCESSING_ERROR.name(), "Unable to fetch nodes from graph. Exception is: " + e.getMessage());

        }
        return nodes;
    }
}
