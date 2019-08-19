package org.sunbird.graph.engine.actor;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

@ActorConfig(tasks = {"createDataNode"})
public class NodeManager extends BaseActor {

    @Override
    public void onReceive(Request request) throws Throwable {
        String action = request.getOperation();
        switch (action) {
            case "createDataNode":
                Response response = new Response("org.sunbird.content.create");
                OK(response, self());
                break;
            default:
                ERROR(action);
        }
    }
}
