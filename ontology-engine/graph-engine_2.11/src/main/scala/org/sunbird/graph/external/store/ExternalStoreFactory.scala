package org.sunbird.graph.external.store

import java.util
import java.util.{Arrays, List}

object ExternalStoreFactory {

    private val PRIMARY_KEY = util.Arrays.asList("identifier")
    var externalStores: Map[String, ExternalStore] = Map()

    def getExternalStore(keySpace: String, table: String): ExternalStore = {
        val key = getKey(keySpace,table)
        println("Size of externalStores: " + externalStores)
        val store = externalStores.getOrElse(key, new ExternalStore(keySpace, table, PRIMARY_KEY))
        if(!externalStores.contains(key))
            externalStores += (key -> store)
        store
    }

    private def getKey(keySpace: String, table: String) = {
        "store-" + keySpace + "-" + table
    }

}
