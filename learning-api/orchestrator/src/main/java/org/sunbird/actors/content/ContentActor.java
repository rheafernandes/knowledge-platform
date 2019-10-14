package org.sunbird.actors.content;

import akka.actor.AbstractActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.*;
import org.sunbird.graph.engine.NodeManager;
import scala.concurrent.Future;


public class ContentActor extends BaseActor {

    public static String objectType = "Content";
    public static String version = "1.0";

    private static final String CONTENT_KEYSPACE_NAME = Platform.config.hasPath("content.keyspace.name") ? Platform.config.getString("content.keyspace.name") : "content_store";
    private static final String CONTENT_TABLE_NAME = Platform.config.hasPath("content.keyspace.table") ? Platform.config.getString("content.keyspace.table") : "content_data";


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
        createRequest.getContext().put("keyspace",CONTENT_KEYSPACE_NAME);
        createRequest.getContext().put("table",CONTENT_TABLE_NAME);
        Future<Response> result = NodeManager.createDataNode(createRequest, getContext().dispatcher());
        return result;
//        Patterns.pipe(result, getContext().getDispatcher()).to(sender());
    }

    private void update(Request request) {
        Request updateRequest = new Request(request, objectType);
    }



    private Response getErrorResponse(Throwable e) {
        e.printStackTrace();
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(ResponseParams.StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
            response.put("messages", mwException.getMessages());
        } else {
            params.setErr("ERR_SYSTEM_EXCEPTION");
        }
        System.out.println("Exception occurred in class :" + e.getClass().getName() + " with message :" + e.getMessage());
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        return response;
    }

    private String setErrMessage(Throwable e) {
        if (e instanceof MiddlewareException) {
            return e.getMessage();
        } else {
            return "Something went wrong in server while processing the request";
        }
    }

    private void setResponseCode(Response res, Throwable e) {
        if (e instanceof ClientException) {
            res.setResponseCode(ResponseCode.CLIENT_ERROR);
        } else if (e instanceof ServerException) {
            res.setResponseCode(ResponseCode.SERVER_ERROR);
        } else if (e instanceof ResourceNotFoundException) {
            res.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
        } else {
            res.setResponseCode(ResponseCode.SERVER_ERROR);
        }
    }
}
