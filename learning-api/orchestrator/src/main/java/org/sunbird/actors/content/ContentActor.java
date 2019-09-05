package org.sunbird.actors.content;

import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.Platform;
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
        createRequest.setRequest(request.getRequest());
        createRequest.getContext().put("store_name","content_data_store");
        Patterns.pipe(getResult(createRequest), getContext().getDispatcher()).to(sender());
    }

    private void update(Request request) {
        Request createRequest = new Request(request,"updateDataNode", objectType);
        Patterns.pipe(getResult(createRequest), getContext().getDispatcher()).to(sender());
    }

}
