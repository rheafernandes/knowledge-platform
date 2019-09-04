package org.sunbird.graph.model.nodes

import java.util

import akka.pattern.Patterns
import org.apache.commons.collections4.{CollectionUtils, MapUtils}
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.common.exception.ResponseCode
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.BaseDomainObject
import org.sunbird.graph.engine.dto.ProcessingNode
import org.sunbird.graph.mgr.BaseGraphManager
import org.sunbird.graph.model.relation.RelationHandler
import org.sunbird.graph.service.operation.Neo4JBoltNodeOperations

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}


class DataNode(manager: BaseGraphManager, graphId: String, objectType: String, version: String) extends BaseDomainObject(graphId, objectType, version) {

    implicit val ec: ExecutionContext = manager.getContext().dispatcher
    val definition: org.sunbird.graph.engine.DefinitionNode = new org.sunbird.graph.engine.DefinitionNode(graphId, objectType, "1.0")

    @throws[Exception]
    def create(request: Request): Unit = {
        val validationResult = validate(request.getRequest)
        val response = createNode(validationResult.getNode)
        val extPropsResponse = saveExternalProperties(validationResult.getIdentifier, validationResult.getExternalData, request.getContext)
        val updateRelResponse = updateRelations(validationResult, request.getContext)
        val futureList = List(extPropsResponse, updateRelResponse)
        val future = Future.sequence(futureList).map(list => {
            val errList = list.map(f => f.asInstanceOf[Response]).filter(res => !StringUtils.equals(res.getResponseCode.name(), ResponseCode.OK.name()));
            if (errList.isEmpty) {
                response
            } else {
                errList(0)
            }
        });
        Patterns.pipe(future, ec).to(manager.sender())
    }

    @throws[Exception]
    private def validate(input: util.Map[String, AnyRef]): ProcessingNode = {
        val node = definition.getNode(input)
        definition.validate(node)
        node
    }

    private def createNode(node: Node): Response = {
        val addedNode = Neo4JBoltNodeOperations.addNode(graphId, node)
        val response = new Response
        response.put("node_id", addedNode.getIdentifier)
        response.put("versionKey", addedNode.getMetadata.get("versionKey"))
        response
    }

    private def saveExternalProperties(identifier: String, externalProps: util.Map[String, AnyRef], context: util.Map[String, AnyRef]): Future[AnyRef] = {
        if (MapUtils.isNotEmpty(externalProps)) {
            externalProps.put("identifier", identifier)
            val request = new Request(context, externalProps, "updateContentBody", objectType)
            manager.getResult(request)
        } else {
            Future(new Response())
        }
    }

    private def updateRelations(node: ProcessingNode, context: util.Map[String, AnyRef]) : Future[AnyRef] = {
        val relations: util.List[Relation] = node.getNewRelations
        if (CollectionUtils.isNotEmpty(relations)) {
            relations.toList.map(relation => RelationHandler.getRelation(manager, graphId, node.getRelationNode(relation.getStartNodeId), relation.getRelationType, node.getRelationNode(relation.getEndNodeId), new util.HashMap()))
                            .map(relation => {
                                val req = new Request()
                                req.setContext(context)
                                relation.validateRelation(req)
                                relation.createRelation(req)
                            })
            Future(new Response())
        } else {
            Future(new Response)
        }
    }
}