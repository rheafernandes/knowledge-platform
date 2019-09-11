package org.sunbird.preprocess

import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.schema.{CoreDomainObject, ISchemaValidator, SchemaValidatorFactory}

abstract class IDefinitionNode(graphId: String, objectType: String, version: String = "1.0") extends CoreDomainObject(graphId, objectType, version) {

    val schemaValidator: ISchemaValidator = SchemaValidatorFactory.getInstance(objectType, version)
    def getNode(input: java.util.Map[String, AnyRef]): ProcessingNode

    @throws[Exception]
    def validate(node: ProcessingNode): ProcessingNode
}
