package org.sunbird.graph.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.schema.{DefinitionFactory, DefinitionNode}

import scala.collection.JavaConverters._

object NodeUtils {
  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def getSerializedNode(node: Node, fields: java.util.List[String]): Node = {
    val definitionNode: DefinitionNode = DefinitionFactory.getDefinition(node.getGraphId, node.getObjectType, "1.0")
    val definitionMap: Map[String, AnyRef] = definitionNode.getRelationDefinitionMap()
    val updatedNode: Node = filterOutFields(node, fields)
    val scalaMetadataMap = updatedNode.getMetadata.asScala
    val updatedKeyAndJsonMap = scalaMetadataMap.map(entry => handleKeyNames(entry, fields).getOrElse(entry._1) -> convertJsonString(entry, definitionNode)).toMap
    updatedNode.getMetadata.putAll(updatedKeyAndJsonMap.asJava)
    if(CollectionUtils.isEmpty(fields) || definitionMap.keySet.filter(key => fields.contains(key)).nonEmpty) {
      getRelationMap(definitionMap, node)
    } else
      updatedNode
  }

  private def filterOutFields(node: Node, fields: java.util.List[String]): Node = {
    val metaData = node.getMetadata.asScala.filter(entry => fields.contains(entry._1)).map(entry => entry._1 -> entry._2).toMap
    if (null != metaData && metaData.nonEmpty)
      node.getMetadata.keySet().retainAll(metaData.asJava.keySet())
    node
  }

  private def convertJsonString(entry: (String, AnyRef), definitionNode: DefinitionNode): AnyRef = {
    val jsonProperties = definitionNode.fetchJsonProps()
    if (jsonProperties.contains(entry._1)) {
      try {
        mapper.readTree(entry._2.toString)
      } catch {
        case exception: Exception => entry._2
      }
    } else
      entry._2
  }

  private def handleKeyNames(entry: (String, AnyRef), fields: java.util.List[String]): Option[String] = {
    if (null == fields)
      Some(entry._1.charAt(0).toLower + entry._1.substring(1, entry._1.length))
    else
      None
  }

  private def getRelationMap(definitionMap: Map[String, AnyRef], node: Node): Node = {
    val inRelationList: java.util.List[Relation] = node.getInRelations
    val outRelationList: java.util.List[Relation] = node.getOutRelations
    val inRelationMetadataList: List[(String, Map[String, AnyRef])] = inRelationList.asScala.toList.map(inRelation => definitionMap.get(inRelation.getRelationType + "_in_" + inRelation.getStartNodeObjectType).getOrElse("in").asInstanceOf[String] -> populationRelationMaps(inRelation, "in"))
    val inValue: Map[String, List[Map[String, AnyRef]]] = inRelationMetadataList.groupBy(_._1).map { case (k, v) => k -> v.map(_._2) }
    val outRelationMetadataList: List[(String, Map[String, AnyRef])] = outRelationList.asScala.toList.map(outRelation => definitionMap.get(outRelation.getRelationType + "_out_" + outRelation.getEndNodeObjectType).getOrElse("out").asInstanceOf[String] -> populationRelationMaps(outRelation, "out"))
    val outValue: Map[String, List[Map[String, AnyRef]]] = outRelationMetadataList.groupBy(_._1).map { case (k, v) => k -> v.map(_._2) }
    val mapRelationType = mapper.getTypeFactory.constructMapType(classOf[java.util.Map[_, _]], classOf[String], classOf[java.util.List[java.util.Map[String, AnyRef]]])
    if (inValue.nonEmpty)
      node.getMetadata.putAll(mapper.convertValue(inValue, mapRelationType))
    if (outValue.nonEmpty)
      node.getMetadata.putAll(mapper.convertValue(outValue, mapRelationType))
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
