package org.sunbird.preprocess
import java.util

import org.apache.commons.collections4.MapUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.graph.common.Identifier
import org.sunbird.graph.dac.enums.SystemNodeTypes
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.dto.ProcessingNode

import scala.collection.JavaConverters._

class BaseDefinitionNode(graphId: String, objectType: String, version: String = "1.0") extends IDefinitionNode(graphId, objectType, version) {

    val inRelationsSchema: Map[String, AnyRef] = relationsSchema("in")
    val outRelationsSchema: Map[String, AnyRef] = relationsSchema("out")
    val outRelationObjectTypes: List[String] = {
        outRelationsSchema.values.map((e: AnyRef) => e.asInstanceOf[java.util.Map[String, AnyRef]].asScala)
                .flatten(e => {
                    val relType = e.getOrElse("type", "associatedTo")
                    val objects = e.getOrElse("objects", new util.ArrayList).asInstanceOf[java.util.List[String]].asScala
                    objects.map(obj => relType + ":" + obj)
                }).toList.distinct
    }
    private def relationsSchema(direction: String): Map[String, AnyRef] = {
        if (schemaValidator.getConfig.hasPath("relations")) {
            schemaValidator.getConfig.getObject("relations").unwrapped().asScala.filter(entry => {
                val relation = entry._2.asInstanceOf[java.util.Map[String, AnyRef]].asScala
                direction.equalsIgnoreCase(relation.getOrElse("direction", "out").asInstanceOf[String])
            }).map(entry => (entry._1 ,entry._2.asInstanceOf[AnyRef])).toMap
        } else {
            Map()
        }
    }


    override def getNode(input: java.util.Map[String, Object]): ProcessingNode = {
        val result = schemaValidator.getStructuredData(input)
        val node = new Node(graphId, result.getMetadata)
        // TODO: set SYS_NODE_TYPE, FUNC_OBJECT_TYPE
        node.setNodeType(SystemNodeTypes.DATA_NODE.name)
        node.setObjectType(objectType)
        if (StringUtils.isBlank(node.getIdentifier)) node.setIdentifier(Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp))
        setRelations(node, result.getRelations)
        new ProcessingNode(node, result.getExternalData)
    }

    @throws[Exception]
    override def validate(node: ProcessingNode): ProcessingNode = {
        println("org.sunbird.preprocess.BasePreProcessor : validate called for " + node.getIdentifier)
        node
    }


    private def setRelations(node: Node, relations: java.util.Map[String, AnyRef]): Unit = {
        if (MapUtils.isNotEmpty(relations)) {
            def getRelations(schema: Map[String, AnyRef]): List[Relation] = {
                relations.asScala.filterKeys(key => schema.keySet.contains(key))
                        .flatten(entry => {
                            val relSchema = schema.get(entry._1).get.asInstanceOf[java.util.Map[String, AnyRef]].asScala
                            val relData = entry._2.asInstanceOf[java.util.List[java.util.Map[String, AnyRef]]]
                            relData.asScala.map(r => {
                                new Relation(node.getIdentifier, relSchema.get("type").get.asInstanceOf[String], r.get("identifier").asInstanceOf[String])
                            })
                        }).toList
            }
            val inRelations = getRelations(inRelationsSchema).asJava
            node.setInRelations(inRelations)
            val outRelations = getRelations(outRelationsSchema).asJava
            node.setOutRelations(outRelations)
        }
    }
}
