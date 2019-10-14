package org.sunbird.actors.content;

import akka.actor.AbstractActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.*;
import org.sunbird.graph.engine.NodeManager;
import scala.concurrent.Future;


@ActorConfig(tasks = {"createContent"})
public class ContentActor extends AbstractActor {

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
        Future<Response> result = NodeManager.createDataNode(createRequest, getContext().dispatcher());
        return result;
//        Patterns.pipe(result, getContext().getDispatcher()).to(sender());
    }

    private void update(Request request) {
        Request updateRequest = new Request(request, objectType);
    }

    public Future<Response> ERROR(String operation) {
        Response response = getErrorResponse(new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid operation provided in request to process: " + operation));
        return Futures.successful(response);
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

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, message -> {
            Patterns.pipe(onReceive(message), getContext().dispatcher()).to(sender());
        }).build();
    }
}
