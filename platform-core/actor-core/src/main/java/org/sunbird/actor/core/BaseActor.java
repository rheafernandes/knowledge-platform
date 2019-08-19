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
      System.out.println("BaseActor: onReceive called for operation: " + operation);
      try {
        onReceive(request);
      } catch (Exception e) {
        onReceiveException(operation, e);
      }
    } else {
      // Do nothing !
    }
  }


  protected void onReceiveException(String callerName, Exception exception) throws Exception {
    System.out.println("Exception in message processing for: " + callerName + " :: message: " + exception.getMessage() + exception);
    sender().tell(exception, self());
  }

  protected Response getErrorResponse(Throwable e) {
    Response response = new Response();
    ResponseParams params = new ResponseParams();
    params.setStatus(ResponseParams.StatusType.failed.name());
    if (e instanceof MiddlewareException) {
      MiddlewareException mwException = (MiddlewareException) e;
      params.setErr(mwException.getErrCode());
    } else {
      params.setErr("ERR_SYSTEM_EXCEPTION");
    }
    System.out.println("Exception occured in class :" + e.getClass().getName() + "with message :" + e.getMessage());
    params.setErrmsg(setErrMessage(e));
    response.setParams(params);
    setResponseCode(response, e);
    return response;
  }

  protected String setErrMessage(Throwable e) {
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

  public void ERROR(String operation) throws Exception {
    sender().tell(new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid operation provided in request to process: " + operation), ActorRef.noSender());
  }

  public void unSupportedMessage() {
    sender().tell(new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid operation provided in request to process."), ActorRef.noSender());
  }
}
