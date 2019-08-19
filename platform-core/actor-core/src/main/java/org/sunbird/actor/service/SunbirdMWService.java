package org.sunbird.actor.service;


import akka.pattern.Patterns;
import org.sunbird.common.dto.Request;
import scala.concurrent.Future;

/**
 * @author Mahesh Kumar Gangula
 */
public class SunbirdMWService extends BaseMWService {

    public static void init() {
        getActorSystem();
        initRouters();
    }

    public static Future<Object> execute(Request request) {
        return Patterns.ask(requestRouter, request, 30000);
    }


}
