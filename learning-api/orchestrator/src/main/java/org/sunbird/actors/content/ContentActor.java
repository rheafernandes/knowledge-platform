package org.sunbird.actors.content;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseHandler;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.nodes.DataNode;
import scala.concurrent.Future;


public class ContentActor extends BaseActor {
    public static String objectType = "Content";
    public static String version = "1.0";

    public Future<Response> onReceive(Request request) {
        String operation = request.getOperation();
        try {
            if ("createContent".equals(operation)) {
                return create(request);
            } else {
                return ERROR(operation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Futures.successful(new Response());
        }

    }

    private Future<Response> create(Request request) throws Exception {
        Request createRequest = new Request(request, objectType);
        createRequest.setRequest(request.getRequest());
        createRequest.getContext().put("graph_id", "domain");
        createRequest.getContext().put("version", "1.0");
        return DataNode.create(createRequest, getContext().dispatcher())
                .map(new Mapper<Node, Response>() {
                    @Override
                    public Response apply(Node node) {
                        Response response = ResponseHandler.OK();
                        response.put("node_id", node.getIdentifier());
                        response.put("versionKey", node.getMetadata().get("versionKey"));
                        return response;
                    }
                }, getContext().dispatcher());
    }

    private void update(Request request) {
        Request updateRequest = new Request(request, objectType);
    }

}
