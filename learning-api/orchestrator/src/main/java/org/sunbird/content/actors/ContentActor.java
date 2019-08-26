package org.sunbird.content.actors;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.content.IContentNode;
import org.sunbird.content.mgr.ContentNodeManager;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;
import org.sunbird.graph.engine.DataNode;
import org.sunbird.graph.engine.DefinitionNode;
import org.sunbird.graph.mgr.BaseGraphManager;
import org.sunbird.schema.dto.ValidationResult;

import java.util.HashMap;
import java.util.Map;


@ActorConfig(tasks = {"createContent"})
public class ContentActor extends BaseGraphManager {

    public static String objectType = "content";
    public static String version = "1.0";
    public static String externalPropsDB = "";

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

    private void create(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
        IContentNode node = ContentNodeManager.getContentNode(data);
        ValidationResult result = node.validate("create");
        if (result.isValid()) {
            // validateContentSpecific
            // createDataNode
            // updateRelations
            // saveExternalProperties
            Response response = new Response();
            response.put("content", new HashMap<String, Object>() {{
                put("properties", result.getData());
                put("externalProperties", result.getExternalData());
            }});
            OK(response, self());
        } else {
            ERROR(GraphEngineErrorCodes.ERR_INVALID_NODE.name(),"Validation errors.", ResponseCode.CLIENT_ERROR, "messages", result.getMessages());
        }
    }

    private void update(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
    }

    public DefinitionNode getDefinitionNode() {
        return new DefinitionNode(objectType, version);
    }

    public DataNode getDataNode() {
        return new DataNode(objectType, version);
    }
}
