package org.sunbird.graph.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.lang3.StringUtils
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.schema.{DefinitionFactory, DefinitionNode}

import scala.collection.JavaConverters._

object NodeUtils {

  def getSerializedNode(node: Node, fields: java.util.List[String]): Node = {
    val definitionNode = DefinitionFactory.getDefinition(node.getGraphId, node.getObjectType, "1.0")
    val updatedNode = filterOutFields(node, fields)
    val scalaMetadataMap = updatedNode.getMetadata.asScala
    val updatedKeyAndJsonMap = scalaMetadataMap.map(entry => handleKeyNames(entry, fields).getOrElse(entry._1) -> convertJsonString(entry, definitionNode)).toMap
    updatedNode.setMetadata(updatedKeyAndJsonMap.asJava)
    val resultNode = getRelationMap(definitionNode, updatedNode)
    resultNode
  }

  private def filterOutFields(node: Node, fields: java.util.List[String]): Node = {
    val metaData = node.getMetadata.asScala.filter(entry => fields.contains(entry._1)).map(entry => entry._1 -> entry._2).toMap
    if (null != metaData && metaData.nonEmpty)
      node.setMetadata(metaData.asJava)
    node
  }

  //Todo: Convert Json String to Json Object
  private def convertJsonString(entry: (String, AnyRef), definitionNode: DefinitionNode): AnyRef = {
    val mapper: ObjectMapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val jsonProperties = definitionNode.fetchJsonProps()
    if (jsonProperties.contains(entry._1)) {
      entry._2
    } else
      entry._2
  }

  private def handleKeyNames(entry: (String, AnyRef), fields: java.util.List[String]): Option[String] = {
    if (null == fields)
      Some(entry._1.charAt(0).toLower + entry._1.substring(1, entry._1.length))
    else
      None
  }

  private def getRelationMap(definitionNode: DefinitionNode, node: Node): Node = {
    val definitionMap = definitionNode.getRelationDefinitionMap()
    val inRelationList: java.util.List[Relation] = node.getInRelations
    val outRelationList: java.util.List[Relation] = node.getOutRelations
    val inRelationMetadataList: List[(String, Map[String, AnyRef])] = inRelationList.asScala.toList.map(inRelation => definitionMap.get(inRelation.getRelationType + "_in_" + inRelation.getStartNodeObjectType).getOrElse("in").asInstanceOf[String]-> populationRelationMaps(inRelation, "in"))
    val value = inRelationMetadataList.groupBy(_._1).map { case (k, v) => (k, v.map(_._2))}
    val outRelationMetadataList: List[(String, Map[String, AnyRef])] = outRelationList.asScala.toList.map(outRelation => definitionMap.get(outRelation.getRelationType + "_out_" + outRelation.getEndNodeObjectType).getOrElse("out").asInstanceOf[String] -> populationRelationMaps(outRelation, "out"))
    val value2 = outRelationMetadataList.groupBy(_._1).map { case (k, v) => (k, v.map(_._2))}
    node.getMetadata.putAll(value.asJava)
    node.getMetadata.putAll(value2.asJava)
    node
  }

  private def populationRelationMaps(relation: Relation, direction: String): Map[String, AnyRef] = {
    if (StringUtils.equalsAnyIgnoreCase("out", direction)) {
      Map[String, AnyRef]("identifier" -> relation.getEndNodeId.replace(".img", ""),
        "name" -> relation.getEndNodeName,
        "objectType" -> relation.getEndNodeObjectType.replace("Image", ""),
        "relation" -> relation.getRelationType,
        "description" -> relation.getEndNodeMetadata.get("description"),
        "status" -> relation.getEndNodeMetadata.get("status"))
    } else {
      Map[String, AnyRef]("identifier" -> relation.getStartNodeId.replace(".img", ""),
        "name" -> relation.getStartNodeName,
        "objectType" -> relation.getStartNodeObjectType.replace("Image", ""),
        "relation" -> relation.getRelationType,
        "description" -> relation.getStartNodeMetadata.get("description"),
        "status" -> relation.getStartNodeMetadata.get("status"))
    }
  }
}
