package org.sunbird.content.actors;

import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.actor.service.SunbirdMWService;
import org.sunbird.common.dto.Request;

import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.content.IContentNode;
import org.sunbird.content.mgr.ContentNodeManager;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;
import org.sunbird.graph.mgr.BaseGraphManager;
import org.sunbird.schema.dto.ValidationResult;
import scala.concurrent.Future;


import java.util.Map;


@ActorConfig(tasks = {"createContent"})
public class ContentActor extends BaseGraphManager {

    public static String objectType = "Content";
    public static String version = "1.0";
    public static String externalPropsDB = "";

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "createContent":
                create(request);
                break;
            default:
                ERROR(operation);
                break;
        }

    }

    private void create(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
        IContentNode node = ContentNodeManager.getContentNode(data);
        ValidationResult result = node.validate("create");
        if (result.isValid()) {
            // TODO: save data
            // createDataNode
            Request createNodeRequest = new Request(request.getContext(), result.getData(), objectType, "createDataNode");
            Future<Object> createFuture = SunbirdMWService.execute(createNodeRequest);
            // TODO: this (parent) should become a parameter to all "operation" methods.
            ActorRef parent = sender();
            createFuture.andThen(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable failure, Object success) throws Throwable {
                    if(null == failure) {
                        // TODO:
                        // updateRelations
                        // saveExternalProperties
                        Future<Object> updateRelFuture = SunbirdMWService.execute(new Request(request.getContext(), null, objectType, "updateRelations"));
                        Future<Object> extPropFuture = SunbirdMWService.execute(new Request(request.getContext(), result.getExternalData(), objectType, "saveExternalProperties"));
                        Response response = (Response)success;
                        parent.tell(response, self());
                    } else {
                        ERROR(failure, sender());
                    }
                }
            }, getContext().getDispatcher());


        } else {
            ERROR(GraphEngineErrorCodes.ERR_INVALID_NODE.name(),"Validation errors.", ResponseCode.CLIENT_ERROR, "messages", result.getMessages());
        }
    }

    private void update(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
    }

}
