package org.sunbird.external.store;

import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This factory class provides different instance of NodeExternalStore for given store name
 */
public class NodeExternalStoreFactory {

    private static final String CONTENT_KEYSPACE_NAME = Platform.config.hasPath("content.keyspace.name") ? Platform.config.getString("content.keyspace.name") : "content_store";
    private static final String CONTENT_TABLE_NAME = Platform.config.hasPath("content.keyspace.table") ? Platform.config.getString("content.keyspace.table") : "content_data";
    private static final String ASSESSMENT_TABLE_NAME = "question_data";
    private static final List<String> PRIMARY_KEY = Arrays.asList("identifier");
    private static Map<String, Object> INSTANCES;

    static {
        INSTANCES = new HashMap<String, Object>() {{
            put(getKey(CONTENT_KEYSPACE_NAME, CONTENT_TABLE_NAME), new NodeExternalStore(CONTENT_KEYSPACE_NAME, CONTENT_TABLE_NAME, PRIMARY_KEY));
            put(getKey(CONTENT_KEYSPACE_NAME, ASSESSMENT_TABLE_NAME), new NodeExternalStore(CONTENT_KEYSPACE_NAME, ASSESSMENT_TABLE_NAME, PRIMARY_KEY));
        }};
    }

    public static NodeExternalStore getStoreInstance(String keyspace, String table) {
        if (INSTANCES.containsKey(getKey(keyspace, table))) {
            return (NodeExternalStore) INSTANCES.get(getKey(keyspace, table));
        } else {
            throw new ServerException("ERR_UNKNOWN_EXTERNAL_STORE", "Store Configuration Not Found for [" + keyspace + " , " + table + "]");
        }
    }

    private static String getKey(String keyspace, String table) {
        return "store-" + keyspace + table;
    }
}
