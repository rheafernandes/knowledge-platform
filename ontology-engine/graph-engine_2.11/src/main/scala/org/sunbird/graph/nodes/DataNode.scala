package org.sunbird.graph.nodes

import java.util

import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, Response, ResponseHandler}
import org.sunbird.common.exception.ResponseCode
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.RelationManager
import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.external.ExternalPropsManager
import org.sunbird.graph.model.IRelation
import org.sunbird.graph.model.relation.RelationHandler
import org.sunbird.graph.schema.{CoreDomainObject, DefinitionFactory}
import org.sunbird.graph.service.operation.NodeAsyncOperations

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


class DataNode(graphId: String, objectType: String, version: String)(implicit ec: ExecutionContext) extends CoreDomainObject(graphId, objectType, version) {

    val definition = DefinitionFactory.getDefinition(graphId, objectType, version)

    @throws[Exception]
    def create(request: Request): Future[Response] = {
        val validationResult = validate(request.getRequest)
        val response = createNode(validationResult.getNode)
        val future = response.map(result => {
            if (StringUtils.equals(ResponseCode.OK.name(), result.getResponseCode.name())) {
                val extPropsResponse = saveExternalProperties(validationResult.getIdentifier, validationResult.getExternalData, request.getContext)
                val updateRelResponse = updateRelations(validationResult, request.getContext)
                val futureList = List(extPropsResponse)
                Future.sequence(futureList).map(list => {
                    val errList = list.filter(res => !StringUtils.equals(res.getResponseCode.name(), ResponseCode.OK.name()))
                    if (errList.isEmpty) {
                        result
                    } else {
                        ResponseHandler.handleResponses(errList)
                    }
                })
            } else {
                Future { result }
            }
        }).flatMap(f => f)
        future
    }

    @throws[Exception]
    private def validate(input: util.Map[String, AnyRef]): ProcessingNode = {
        val node = definition.getNode(input)
        definition.validate(node)
        node
    }

    private def createNode(node: Node): Future[Response] = {
        val addedNode =  NodeAsyncOperations.addNode(graphId, node);
        addedNode.map(updatedNode => {
            val response = new Response
            response.put("node_id", updatedNode.getIdentifier)
            response.put("versionKey", updatedNode.getMetadata.get("versionKey"))
            response
        })
    }

    private def saveExternalProperties(identifier: String, externalProps: util.Map[String, AnyRef], context: util.Map[String, AnyRef]): Future[Response] = {
        if (MapUtils.isNotEmpty(externalProps)) {
            externalProps.put("identifier", identifier)
            val request = new Request(context, externalProps, "", objectType)
            ExternalPropsManager.saveProps(request)
        } else {
            Future(new Response())
        }
    }
    
    private def updateRelations(node: ProcessingNode, context: util.Map[String, AnyRef]) : Future[Response] = {
        val relations: util.List[Relation] = node.getNewRelations
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
}