package org.sunbird.external.store;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This factory class provides different instance of NodeExternalStore for given store name
 */
public class NodeExternalStoreFactory {
    private static final List<String> PRIMARY_KEY = Arrays.asList("identifier");
    private static Map<String, NodeExternalStore> externalStores = new HashMap<>();

    public static NodeExternalStore getStoreInstance(String keyspace, String table) {
        if (!externalStores.containsKey(getKey(keyspace, table)))
            constructExternalStoresMap(keyspace, table);
        return externalStores.get(getKey(keyspace, table));
    }

    private static Map<String, NodeExternalStore> constructExternalStoresMap(String keyspace, String table) {
        if(!externalStores.containsKey(getKey(keyspace,table)))
            externalStores.put(getKey(keyspace,table), new NodeExternalStore(keyspace,table, PRIMARY_KEY));
        return externalStores;
    }

    private static String getKey(String keyspace, String table) {
        return "store-" + keyspace +"-"+ table;
    }
}
