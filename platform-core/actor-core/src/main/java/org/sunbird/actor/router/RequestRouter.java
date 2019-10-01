package org.sunbird.actor.router;

import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.sunbird.actor.core.BaseRouter;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** @author Mahesh Kumar Gangula */
public class RequestRouter extends BaseRouter {

  private static String name;
  public static Map<String, ActorRef> routingMap = new HashMap<>();

  @Override
  public void preStart() throws Exception {
    super.preStart();
    name = self().path().name();
    initActors(getContext(), RequestRouter.class.getSimpleName());
  }

  @Override
  protected void cacheActor(String key, ActorRef actor) {
    routingMap.put(key, actor);
  }

  @Override
  public void route(Request request) throws Throwable {
    String operation = request.getOperation();
    ActorRef ref = getActor(operation);
    if (null != ref) {
      route(ref, request, getContext().dispatcher());
    } else {
      ERROR(request.getOperation());
    }
  }

  public static ActorRef getActor(String operation) {
    return routingMap.get(getKey(name, operation));
  }

  /**
   * method will route the message to corresponding router pass into the argument .
   *
   * @param router
   * @param message
   * @return boolean
   */
  private boolean route(ActorRef router, Request message, ExecutionContext ec) {
    long startTime = System.currentTimeMillis();
    Timeout timeout = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    Future<Object> future = Patterns.ask(router, message, timeout);
    ActorRef parent = sender();
    future.onComplete(
        new OnComplete<Object>() {
          @Override
          public void onComplete(Throwable failure, Object result) {
            if (failure != null) {
              if (failure instanceof MiddlewareException) {
                  parent.tell(failure, self());
              } else if (failure instanceof akka.pattern.AskTimeoutException) {
                ServerException exception = new ServerException(ResponseCode.SERVER_ERROR.name(), "Processing request taking more time: " + timeout);
                parent.tell(exception, self());
              } else {
                ServerException exception = new ServerException(ResponseCode.SERVER_ERROR.name(), "Something went wrong while processing the request.", failure);
                parent.tell(exception, self());
              }
            } else {
              parent.tell(result, self());
            }
          }
        },
        ec);
    return true;
  }
}
