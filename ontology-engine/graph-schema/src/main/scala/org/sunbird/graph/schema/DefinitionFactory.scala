package org.sunbird.graph.schema

object DefinitionFactory {

    private var definitions: Map[String, DefinitionNode] = Map()

    def getDefinition(graphId: String, objectType: String, version: String): DefinitionNode = {
        val key = getKey(graphId, objectType, version)
        val definition = definitions.getOrElse(key, new DefinitionNode(graphId, objectType, version))
        if (!definitions.contains(key))
            definitions + (key -> definition)
        definition
    }

    def getKey(graphId: String, objectType: String, version: String): String = {
        graphId + ":" + objectType + ":" + version
    }





}
