package org.sunbird.graph.model;

import org.sunbird.graph.engine.DefinitionNode;

import java.util.HashMap;
import java.util.Map;

public class DefinitionFactory {

    private Map<String, DefinitionNode> definitions = new HashMap<>();

    public DefinitionNode getDefinition(String graphId, String objectType, String version) throws Exception {
        String key = getKey(graphId, objectType, version);
        if (definitions.containsKey(key)) {
            return definitions.get(key);
        } else {
            DefinitionNode definition = new DefinitionNode(graphId, objectType, version);
            definitions.put(key, definition);
            return definition;
        }
    }

    private String getKey(String graphId, String objectType, String version) {
        return graphId + ":" + objectType + ":" + version;
    }
}
