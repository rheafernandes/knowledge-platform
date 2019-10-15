package org.sunbird.graph.schema

import org.sunbird.graph.dac.model.Node
import org.sunbird.schema.{ISchemaValidator, SchemaValidatorFactory}

abstract class IDefinitionNode(graphId: String, objectType: String, version: String = "1.0") extends CoreDomainObject(graphId, objectType, version) {

    val schemaValidator: ISchemaValidator = SchemaValidatorFactory.getInstance(objectType, version)
    def getNode(input: java.util.Map[String, AnyRef]): Node

    @throws[Exception]
    def validate(node: Node): Node

    def getNode(identifier: String, operation: String = "read", mode: String): Node
}
