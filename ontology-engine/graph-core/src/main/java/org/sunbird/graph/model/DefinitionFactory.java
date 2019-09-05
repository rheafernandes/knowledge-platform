package org.sunbird.graph.model;

import org.sunbird.graph.engine.DefinitionNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinitionFactory {

    private static Map<String, DefinitionNode> definitions = new HashMap<>();

    public static DefinitionNode getDefinition(String graphId, String objectType, String version) throws Exception {
        String key = getKey(graphId, objectType, version);
        if (definitions.containsKey(key)) {
            return definitions.get(key);
        } else {
            DefinitionNode definition = new DefinitionNode(graphId, objectType, version);
            definitions.put(key, definition);
            return definition;
        }
    }

    public static List<String> getOutRelationObjectTypes(String graphId, String objectType, String version) {
        try {
            DefinitionNode definition = getDefinition(graphId, objectType, version);
            return definition.getOutRelationObjectTypes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList();
    }

    private static String getKey(String graphId, String objectType, String version) {
        return graphId + ":" + objectType + ":" + version;
    }
}
