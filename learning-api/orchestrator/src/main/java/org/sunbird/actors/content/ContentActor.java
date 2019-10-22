package org.sunbird.actors.content;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseHandler;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.nodes.DataNode;
import scala.concurrent.Future;
import scala.concurrent.Promise;

import java.util.concurrent.CompletionStage;


public class ContentActor extends BaseActor {

    public Future<Response> onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        if ("createContent".equals(operation)) {
            return create(request);
        } else if("updateContent".equals(operation)){
            return update(request);
        }else {
            return ERROR(operation);
        }
    }

    private Future<Response> create(Request request) throws Exception {
        return DataNode.create(request, getContext().dispatcher())
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

    private Future<Response> update(Request request) throws Exception {
        Promise<Response> response = new scala.concurrent.impl.Promise.DefaultPromise<>();
        DataNode.update(request, getContext().dispatcher()).onComplete(new OnComplete<Node>() {
            @Override
            public void onComplete(Throwable failure, Node node) throws Throwable {
                if(null != failure) {
                    response.success(getErrorResponse(failure));
                } else {
                    Response res = ResponseHandler.OK();
                    res.put("node_id", node.getIdentifier());
                    res.put("versionKey", node.getMetadata().get("versionKey"));
                    response.success(res);
                }
            }
        }, getContext().dispatcher());

        return response.future();
    }
}
