package org.sunbird.graph.schema.validator

import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.graph.dac.enums.SystemNodeTypes
import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.validator.NodeValidator

import scala.collection.JavaConverters._

trait RelationValidator extends IDefinitionNode {

    @throws[Exception]
    abstract override def validate(node: ProcessingNode): ProcessingNode = {
        println("org.sunbird.preprocess.RelationValidator : validate called for " + node.getIdentifier)
        val relations = node.getNewRelations
        if (CollectionUtils.isNotEmpty(relations)) {
            val ids = relations.asScala.map(r => List(r.getStartNodeId, r.getEndNodeId)).flatten
                    .filter(id => StringUtils.isNotBlank(id) && !StringUtils.equals(id, node.getIdentifier)).toList.distinct
            val relationNodes = NodeValidator.validate(node.getGraphId, ids.asJava)
            node.setNodeType(SystemNodeTypes.DATA_NODE.name)
            relationNodes.put(node.getIdentifier, node)
            node.setRelationNodes(relationNodes)
            // TODO: behavior validation should be here.
        }
        super.validate(node)
    }

}
