package org.sunbird.graph.schema.validator

import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.schema.IDefinitionNode

trait SchemaValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: ProcessingNode): ProcessingNode = {
        println("org.sunbird.preprocess.SchemaValidator: validate called for " + node.getIdentifier)
        schemaValidator.validate(node.getMetadata)
        super.validate(node)
    }
}
