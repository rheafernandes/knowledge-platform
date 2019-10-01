package org.sunbird.graph.external

import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.external.store.ExternalStoreFactory

import scala.concurrent.{ExecutionContext, Future}

object ExternalPropsManager {

    def saveProps(request: Request)(implicit ec: ExecutionContext): Future[Response] = {
        val keySpace = request.getContext.get("keyspace").asInstanceOf[String]
        val table = request.getContext.get("table").asInstanceOf[String]
        val store = ExternalStoreFactory.getExternalStore(keySpace, table)
        store.insert(request.getRequest)
        Future(new Response())
    }

}
