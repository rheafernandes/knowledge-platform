package org.sunbird.graph.nodes

import java.util

import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.RelationManager
import org.sunbird.graph.external.ExternalPropsManager
import org.sunbird.graph.relations.{IRelation, RelationHandler}
import org.sunbird.graph.schema.{DefinitionFactory}
import org.sunbird.graph.service.operation.NodeAsyncOperations
import org.sunbird.parseq.Task

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


object DataNode {

    def read(request: Request)(implicit ec: ExecutionContext): Future[Node] = {
      val graphId = request.getContext.get("graph_id").asInstanceOf[String]
      val definitionNode = DefinitionFactory.getDefinition(graphId, request.getObjectType, request.getContext.get("version").asInstanceOf[String])
      val mode: String = request.get("mode").asInstanceOf[String]
      val resultNode: Future[Node] = definitionNode.getNode(request.get("identifier").asInstanceOf[String], "read", mode)
      resultNode.map(node => {
        val fields:List[String] = request.get("fields").asInstanceOf[String].split(",").toList
        val extPropNameList = definitionNode.getExternalProps()
        if (CollectionUtils.isNotEmpty(extPropNameList)) {
          val externalResult = populateExternalProperties(fields, node, request, extPropNameList)
          externalResult.map(node => filterOutFields(node, fields)).flatMap(f => f)
        } else
          filterOutFields(node, fields)
      }).flatMap(f => f)
    }

    @throws[Exception]
    def create(request: Request)(implicit ec: ExecutionContext): Future[Node] = {
        val graphId: String = request.getContext.get("graph_id").asInstanceOf[String]
        val version: String = request.getContext.get("version").asInstanceOf[String]
        val definition = DefinitionFactory.getDefinition(graphId, request.getObjectType, version)
        val inputNode = definition.getNode(request.getRequest)
        val validatedNode = definition.validate(inputNode, "create")
        validatedNode.map(node => {
            val response = NodeAsyncOperations.addNode(graphId, node)
            response.map(result => {
                val futureList = Task.parallel[Response](
                    saveExternalProperties(node.getIdentifier, node.getExternalData, request.getContext, request.getObjectType),
                    updateRelations(graphId, node, request.getContext))
                futureList.map(list => result)
            }).flatMap(f => f)
        }).flatMap(f => f)
    }

    private def saveExternalProperties(identifier: String, externalProps: util.Map[String, AnyRef], context: util.Map[String, AnyRef], objectType: String)(implicit ec: ExecutionContext): Future[Response] = {
        if (MapUtils.isNotEmpty(externalProps)) {
            externalProps.put("identifier", identifier)
            val request = new Request(context, externalProps, "", objectType)
            ExternalPropsManager.saveProps(request)
        } else {
            Future(new Response)
        }
    }
    
    private def updateRelations(graphId: String, node: Node, context: util.Map[String, AnyRef])(implicit ec: ExecutionContext) : Future[Response] = {
        val relations: util.List[Relation] = node.getAddedRelations
        if (CollectionUtils.isNotEmpty(relations)) {
            val relationList: List[IRelation] = relations.toList.map(relation =>
                RelationHandler.getRelation(graphId, node.getRelationNode(relation.getStartNodeId),
                    relation.getRelationType, node.getRelationNode(relation.getEndNodeId), new util.HashMap()))
            val request: Request = new Request
            request.setContext(context)
            request.put("relations", relationList)
            RelationManager.createNewRelations(request)
        } else {
            Future(new Response)
        }
    }

    //TODO: Get values from configuration
    private def updateContentTaggedProperty(node: Node)(implicit ec:ExecutionContext): Future[Node] = {
        val contentTaggedKeys = List("subject", "medium");
        Future{node}
    }

    private def filterOutFields(node: Node, fields: List[String])(implicit ec: ExecutionContext): Future[Node] = {
        val metaData = node.getMetadata.entrySet().filter(entry => fields.contains(entry.getKey)).map(entry => entry.getKey -> entry.getValue).toMap
        if (MapUtils.isNotEmpty(metaData))
            node.setMetadata(metaData.asInstanceOf[Map[String, Object]])
        Future {node}
    }

    private def populateExternalProperties(fields: List[String], node: Node, request: Request, externalProps: Set[String])(implicit ec: ExecutionContext): Future[Node] = {
        val externalPropList = (for (prop <- externalProps) yield prop) (collection.breakOut)
        val externalPropsResponse = ExternalPropsManager.fetchProps(request, externalPropList.filter(prop => fields.contains(prop)).toList)
        externalPropsResponse.map(response => {
          node.getMetadata.putAll(response.getResult)
          Future{node}
        }).flatMap(f => f)
    }
}

