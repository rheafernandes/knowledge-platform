package org.sunbird.actor.core;

import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;

import org.sunbird.actor.router.RequestRouter;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.*;

public abstract class BaseActor extends UntypedAbstractActor {

  public abstract void onReceive(Request request) throws Throwable;

  @Override
  public void onReceive(Object message) throws Throwable {
    if (message instanceof Request) {
      Request request = (Request) message;
      String operation = request.getOperation();
      System.out.println( this.getClass().getCanonicalName() + ": onReceive called for operation: " + operation);
      try {
        onReceive(request);
      } catch (Exception e) {
        ERROR(operation, e);
      }
    } else {
      // Do nothing !
    }
  }


  protected void ERROR(String operation, Exception exception) throws Exception {
    System.out.println("Exception in message processing for: " + operation + " :: message: " + exception.getMessage() + exception);
    sender().tell(getErrorResponse(exception), ActorRef.noSender());
  }

    public void ERROR(String operation) {
        Response response = getErrorResponse(new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid operation provided in request to process: " + operation));
        sender().tell(response, ActorRef.noSender());
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

  protected ActorRef getActorRef(String operation) {
    return RequestRouter.getActor(operation);
  }

  public void OK(Response response, ActorRef actor) {
    sender().tell(response, actor);
  }

}
