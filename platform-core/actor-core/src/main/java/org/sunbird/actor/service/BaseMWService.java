package org.sunbird.actor.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.sunbird.actor.router.RequestRouter;

/** @author Mahesh Kumar Gangula */
public class BaseMWService {

  public static Config config =
      ConfigFactory.systemEnvironment().withFallback(ConfigFactory.load());
  protected static ActorSystem system;
  protected static String name = "SunbirdMWSystem";
  protected static ActorRef requestRouter;

  protected static ActorSystem getActorSystem() {
    if (null == system) {
      Config conf = config.getConfig(name);
      system = ActorSystem.create(name, conf);
    }
    return system;
  }

  protected static void initRouters() {
    requestRouter = system.actorOf(FromConfig.getInstance().props(Props.create(RequestRouter.class).withDispatcher("rr-dispatcher")), RequestRouter.class.getSimpleName());
  }
}
