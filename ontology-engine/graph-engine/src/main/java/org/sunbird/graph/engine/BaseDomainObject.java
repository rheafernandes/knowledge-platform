package org.sunbird.graph.engine;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.DateUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.common.enums.SystemProperties;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;

import java.util.Map;

public abstract class BaseDomainObject {

    public String graphId = "domain";
    public String objectType;
    public String version;

    public BaseDomainObject(String objectType, String version) {
        this.objectType = objectType;
        this.version = version;
    }



    public Node create(Map<String, Object> data) {
        data.put("createdOn", DateUtils.formatCurrentDate());
        data.put(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name());
        data.put(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType);
        String identifier = (String) data.get("identifier");
        data.remove("identifier");
        if (StringUtils.isBlank(identifier)) {
            identifier = Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp());
        }
        Node node = new Node(identifier, SystemNodeTypes.DATA_NODE.name(), objectType);
        node.setGraphId(graphId);
        node.setMetadata(data);
        return node;
    }

    public Node update(Map<String, Object> data) {
        data.put("lastUpdatedOn", DateUtils.formatCurrentDate());
        return null;
    }
}
