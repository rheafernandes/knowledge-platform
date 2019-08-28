package org.sunbird.graph.engine.actor;

import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.mgr.BaseGraphManager;

@ActorConfig(tasks = {"updateRelations"})
public class GraphManager extends BaseGraphManager {

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "updateRelations":
                sender().tell(new Response(), self());
                break;
            default:
                ERROR(operation);
        }
    }
}
