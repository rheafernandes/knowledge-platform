package org.sunbird.graph.engine.actor;

import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.engine.DataNode;
import org.sunbird.graph.mgr.BaseGraphManager;


@ActorConfig(tasks = {"createDataNode", "saveExternalProperties"})
public class NodeManager extends BaseGraphManager {

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "createDataNode":
                createDataNode(request);
                break;
            case "saveExternalProperties":
                saveExternalProperties(request);
                break;
            default:
                ERROR(operation);
        }
    }

    private void createDataNode(Request request) throws Exception {
        // TODO: create Graph Node with metadata
        DataNode node = new DataNode(request.getObjectType(), "1.0");
        Response response = node.create(request);
        OK(response, self());
    }

    private void saveExternalProperties(Request request) {
        Response response = new Response();
        OK(response, self());
    }




}
