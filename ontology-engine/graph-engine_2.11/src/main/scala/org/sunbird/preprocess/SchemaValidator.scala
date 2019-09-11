package org.sunbird.preprocess

import org.sunbird.graph.engine.dto.ProcessingNode

trait SchemaValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: ProcessingNode): ProcessingNode = {
        println("org.sunbird.preprocess.SchemaValidator: validate called for " + node.getIdentifier)
        schemaValidator.validate(node.getMetadata)
        super.validate(node)
    }
}
