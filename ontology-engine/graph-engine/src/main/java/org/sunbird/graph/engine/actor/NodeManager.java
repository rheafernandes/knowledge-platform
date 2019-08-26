package org.sunbird.graph.engine.actor;

import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.mgr.BaseGraphManager;


@ActorConfig(tasks = {"createDataNode"})
public class NodeManager extends BaseGraphManager {

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "createDataNode":
                createDataNode(request);
                break;
            default:
                ERROR(operation);
        }
    }

    private void createDataNode(Request request) {
        // TODO: create Graph Node with metadata
        Response response = new Response();
        OK(response, self());
    }




}
