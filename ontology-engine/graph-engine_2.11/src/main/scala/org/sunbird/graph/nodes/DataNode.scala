package org.sunbird.graph.nodes

import java.util

import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.RelationManager
import org.sunbird.graph.external.ExternalPropsManager
import org.sunbird.graph.relations.{IRelation, RelationHandler}
import org.sunbird.graph.schema.DefinitionFactory
import org.sunbird.graph.service.operation.NodeAsyncOperations
import org.sunbird.parseq.Task

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


object DataNode {

  @throws[Exception]
  def read(request: Request)(implicit ec: ExecutionContext): Future[Node] = {
    val graphId = request.getContext.get("graph_id").asInstanceOf[String]
    val definitionNode = DefinitionFactory.getDefinition(graphId, request.getObjectType, request.getContext.get("version").asInstanceOf[String])
    val mode: String = request.get("mode").asInstanceOf[String]
    val resultNode: Future[Node] = definitionNode.getNode(request.get("identifier").asInstanceOf[String], "read", mode)
    resultNode.map(node => {
      val fields: List[String] = request.get("fields").asInstanceOf[util.ArrayList[String]].toList
        val extPropNameList = definitionNode.getExternalProps()
        if (CollectionUtils.isNotEmpty(extPropNameList) && null != fields && fields.filter(field => extPropNameList.contains(field)).nonEmpty)
          populateExternalProperties(fields, node, request, extPropNameList)
        else
          Future {node}
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
        val contentTaggedKeys = List("subject", "medium")
        Future{node}
    }

    private def populateExternalProperties(fields: List[String], node: Node, request: Request, externalProps: List[String])(implicit ec: ExecutionContext): Future[Node] = {
        val externalPropsResponse = ExternalPropsManager.fetchProps(request, externalProps.filter(prop => fields.contains(prop)))
        externalPropsResponse.map(response => {
          node.getMetadata.putAll(response.getResult)
          Future{node}
        }).flatMap(f => f)
    }
}

