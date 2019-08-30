package org.sunbird.graph.model.nodes

import java.util

import akka.dispatch.Futures
import akka.pattern.Patterns
import org.apache.commons.collections4.MapUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.common.exception.ResponseCode
import org.sunbird.graph.dac.model.{Node, Relation}
import org.sunbird.graph.engine.dto.Result
import org.sunbird.graph.engine.BaseDomainObject
import org.sunbird.graph.mgr.BaseGraphManager
import org.sunbird.graph.service.operation.Neo4JBoltNodeOperations

import scala.concurrent.{ExecutionContext, Future}


class DataNode(manager: BaseGraphManager, graphId: String, objectType: String, version: String) extends BaseDomainObject(graphId, objectType, version) {

    implicit val ec: ExecutionContext = manager.getContext().dispatcher
    val definition: org.sunbird.graph.engine.DefinitionNode = new org.sunbird.graph.engine.DefinitionNode(graphId, objectType, "1.0")

    @throws[Exception]
    def create(request: Request): Unit = {
        val data = request.getRequest
        val validationResult = validate(objectType, data)
        if (validationResult.isValid) {
            val response = createNode(validationResult.getNode)
            val extPropsResponse = saveExternalProperties(validationResult.getIdentifier, validationResult.getExternalData, request.getContext, definition.getExternalSchema.getOperation)
            val updateRelResponse = updateRelations(util.Arrays.asList(), request.getContext)
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
        } else {
            val response = new Response
            response.setResponseCode(ResponseCode.CLIENT_ERROR)
            response.put("messages", validationResult.getMessages)
            manager.sender() ! response
        }
    }

    @throws[Exception]
    private def validate(objectType: String, data: util.Map[String, AnyRef]): Result = {
        definition.validate(data)
    }

    private def createNode(node: Node): Response = {
        val addedNode = Neo4JBoltNodeOperations.addNode(graphId, node)
        val response = new Response
        response.put("node_id", addedNode.getIdentifier)
        response.put("versionKey", addedNode.getMetadata.get("versionKey"))
        response
    }

    private def saveExternalProperties(identifier: String, externalProps: util.Map[String, AnyRef], context: util.Map[String, AnyRef], operation: String): Future[AnyRef] = {
        if (MapUtils.isNotEmpty(externalProps)) {
            externalProps.put("identifier", identifier)
            val request = new Request(context, externalProps, operation, objectType)
            manager.getResult(request)
        } else {
            Future(new Response())
        }
    }

    private def updateRelations(relations: util.List[Relation], context: util.Map[String, AnyRef]) : Future[AnyRef] = {
        // TODO: update Relations using Graph Manager Actor.
        Futures.successful(new Response)
    }
}