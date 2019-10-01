package org.sunbird.graph.engine

import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.nodes.DataNode

import scala.concurrent.{ExecutionContext, Future}

object NodeManager {

    val graphId = "domain"

    @throws[Exception]
    def createDataNode(request: Request)(implicit ec: ExecutionContext): Future[Response] = {
        val node = new DataNode(graphId, request.getObjectType, "1.0")
        request.getContext.put("graph_id", graphId)
        node.create(request)
    }
}
