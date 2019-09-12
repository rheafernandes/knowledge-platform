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
        String key = getKey(keyspace, table);
        if (!externalStores.containsKey(key))
            constructExternalStoresMap(keyspace, table, key);
        return externalStores.get(getKey(keyspace, table));
    }

    private static Map<String, NodeExternalStore> constructExternalStoresMap(String keyspace, String table, String key) {
        if (!externalStores.containsKey(key))
            externalStores.put(key, new NodeExternalStore(keyspace, table, PRIMARY_KEY));
        return externalStores;
    }

    private static String getKey(String keyspace, String table) {
        return "store-" + keyspace + "-" + table;
    }
}
