package org.sunbird.graph.schema

import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.schema.validator.{BaseDefinitionNode, RelationValidator, SchemaValidator, VersioningNode}

class DefinitionNode(graphId: String, objectType: String, version: String = "1.0") extends BaseDefinitionNode(graphId , objectType, version) with VersioningNode with RelationValidator with SchemaValidator {

    def getOutRelationObjectTypes: List[String] = outRelationObjectTypes

    def getNode(identifier: String, input: java.util.Map[String, AnyRef]): ProcessingNode = { // TODO: used for update operations.
        null
    }
}
