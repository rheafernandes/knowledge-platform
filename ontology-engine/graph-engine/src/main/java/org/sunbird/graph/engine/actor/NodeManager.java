package org.sunbird.graph.engine.actor;

import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.engine.BaseManager;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

@ActorConfig(tasks = {"createDataNode"})
public class NodeManager extends BaseManager {

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
        ValidationResult result = validate("content", "1.0", request.getRequest());
        Response response = new Response("org.sunbird.content.create");
        if (result.isValid()) {
            Map<String, Object> inputWithDefault = JsonUtils.deserialize(result.getData(), Map.class);
            response.getResult().put("content", inputWithDefault);
            OK(response, self());
        } else {
            ERROR("NODE_VALIDATION_FAILED", "Validation errors.", ResponseCode.CLIENT_ERROR, "messages", result.getMessages());
        }
    }
}
