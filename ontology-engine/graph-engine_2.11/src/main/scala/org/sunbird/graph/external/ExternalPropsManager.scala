package org.sunbird.graph.external

import com.typesafe.config.ConfigFactory
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.external.store.ExternalStoreFactory

import scala.concurrent.{ExecutionContext, Future}

object ExternalPropsManager {
    val keySpace = ConfigFactory.load().getString("content.keyspace.name")
    val table = ConfigFactory.load().getString("content.keyspace.table")

    def saveProps(request: Request)(implicit ec: ExecutionContext): Future[Response] = {
        val store = ExternalStoreFactory.getExternalStore(keySpace, table)
        store.insert(request.getRequest)
        Future(new Response)
    }

}
