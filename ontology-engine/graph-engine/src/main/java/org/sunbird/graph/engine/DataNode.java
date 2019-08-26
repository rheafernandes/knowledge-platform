package org.sunbird.graph.engine;

import org.sunbird.common.dto.Request;

import java.util.Map;

public class DataNode extends BaseDomainObject {

    DefinitionNode definition;

    public DataNode(String objectType, String version) {
        super(objectType, version);
        definition = new DefinitionNode(objectType, version);
    }



    public void create(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
        super.create(data);
        definition.validate(data);
    }

}
