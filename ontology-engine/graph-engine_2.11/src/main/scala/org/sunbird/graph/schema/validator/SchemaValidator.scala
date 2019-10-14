package org.sunbird.graph.schema.validator

import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.dac.model.Node

trait SchemaValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: Node): Node = {
        schemaValidator.validate(node.getMetadata)
        super.validate(node)
    }
}
