package org.sunbird.content.actors;

import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;

import org.sunbird.graph.mgr.BaseGraphManager;


@ActorConfig(tasks = {"createContent"})
public class ContentActor extends BaseGraphManager {

    public static String objectType = "Content";
    public static String version = "1.0";

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

    private void create(Request request) {
        Request createRequest = new Request(request,"createDataNode", objectType);
        Patterns.pipe(getResult(createRequest), getContext().getDispatcher()).to(sender());
    }

    private void update(Request request) {
        Request createRequest = new Request(request,"updateDataNode", objectType);
        Patterns.pipe(getResult(createRequest), getContext().getDispatcher()).to(sender());
    }

}
