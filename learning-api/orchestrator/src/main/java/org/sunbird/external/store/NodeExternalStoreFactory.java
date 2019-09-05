package org.sunbird.external.store;

import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;

import java.util.Arrays;
import java.util.List;

/**
 * This factory class provides different instance of NodeExternalStore for given store name
 */
public class NodeExternalStoreFactory {

    private static final String CONTENT_KEYSPACE_NAME = Platform.config.hasPath("content.keyspace.name")? Platform.config.getString("content.keyspace.name"): "content_store";
    private static final String CONTENT_TABLE_NAME = Platform.config.hasPath("content.keyspace.table")? Platform.config.getString("content.keyspace.table"): "content_data";
    private static final String ASSESSMENT_TABLE_NAME = "question_data";
    //TODO: List of primary keys should be fetched from configuration
    private static final List<String> PRIMARY_KEY = Arrays.asList("identifier");

    private static  NodeExternalStore contentStore = new NodeExternalStore(CONTENT_KEYSPACE_NAME, CONTENT_TABLE_NAME, PRIMARY_KEY);
    private static  NodeExternalStore assessmentStore = new NodeExternalStore(CONTENT_KEYSPACE_NAME, ASSESSMENT_TABLE_NAME, PRIMARY_KEY);

    public static NodeExternalStore getStoreInstance(String storeName) {
        switch (storeName) {
            case "content_data_store":
                return contentStore;
            case "assessment_data_store":
                return assessmentStore;
            default:
                throw new ServerException("ERR_UNKNOWN_EXTERNAL_STORE", "Store Configuration Not Found for [" + storeName + "]");
        }
    }
}
