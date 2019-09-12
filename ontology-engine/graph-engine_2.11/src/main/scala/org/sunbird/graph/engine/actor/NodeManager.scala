package org.sunbird.graph.engine.actor

import org.sunbird.actor.router.ActorConfig
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.mgr.BaseGraphManager
import org.sunbird.graph.nodes.DataNode

@ActorConfig(tasks = Array("createDataNode", "saveExternalProperties"))
class NodeManager extends BaseGraphManager {

    val graphId = "domain"

    @throws[Throwable]
    override def onReceive(request: Request): Unit = {
        val operation = request.getOperation
        operation match {
            case "createDataNode" =>
                createDataNode(request)
            case "saveExternalProperties" =>
                sender() ! new Response()
            case _ =>
                ERROR(operation)
        }
    }

    @throws[Exception]
    private def createDataNode(request: Request): Unit = {
        val node = new DataNode(this, graphId, request.getObjectType, "1.0")
        request.getContext.put("graph_id", graphId)
        node.create(request)
    }
}
