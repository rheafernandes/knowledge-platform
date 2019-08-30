package org.sunbird.graph.engine.actor;

import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.graph.engine.DataNode;
import org.sunbird.graph.mgr.BaseGraphManager;


@ActorConfig(tasks = {"createDataNode"})
public class NodeManager extends BaseGraphManager {

    // TODO: put it as part of request;
    String graphId = "domain";

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

    private void createDataNode(Request request) throws Exception {
        DataNode node = new DataNode(this, graphId, request.getObjectType(), "1.0");
        node.create(request);
    }

}
