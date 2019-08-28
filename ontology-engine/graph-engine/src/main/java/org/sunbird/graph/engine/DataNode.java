package org.sunbird.graph.engine;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.operation.Neo4JBoltNodeOperations;

import java.util.Map;

public class DataNode extends BaseDomainObject {

    public DataNode(String objectType, String version) {
        super(objectType, version);
    }

    public Response create(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
        Node node = super.create(data);
        Node addedNode = Neo4JBoltNodeOperations.addNode(graphId, node, request);
        Response response = new Response();
        response.put("node_id", addedNode.getIdentifier());
        response.put("versionKey", addedNode.getMetadata().get("versionKey"));
        return response;
    }

}
