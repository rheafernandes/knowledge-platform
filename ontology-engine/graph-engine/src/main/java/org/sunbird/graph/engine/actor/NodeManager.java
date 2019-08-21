package org.sunbird.graph.engine.actor;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.schema.ISchema;
import org.sunbird.schema.SchemaFactory;
import org.sunbird.schema.dto.Result;

import java.util.Map;

@ActorConfig(tasks = {"createDataNode"})
public class NodeManager extends BaseActor {

    @Override
    public void onReceive(Request request) throws Throwable {
        String action = request.getOperation();
        switch (action) {
            case "createDataNode":
                Result result = validate("content", "1.0", request.getRequest());
                Response response = new Response("org.sunbird.content.create");
                if (result.isValid()) {
                    Map<String, Object> inputWithDefault = JsonUtils.deserialize(result.getData(), Map.class);

                    response.getResult().put("content", inputWithDefault);
                } else {
                    response.setParams(new ResponseParams());
                    response.setResponseCode(ResponseCode.CLIENT_ERROR);
                    response.getResult().put("messages", result.getMessages());
                }
                OK(response, self());
                break;
            default:
                ERROR(action);
        }
    }

    public Result validate(String objectType, String version, Map<String, Object> request) throws Exception {
        ISchema schema = SchemaFactory.getInstance(objectType, version);
        return schema.validate(JsonUtils.serialize(request));
    }
}
