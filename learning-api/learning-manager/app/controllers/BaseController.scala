package controllers



import org.sunbird.actor.service.SunbirdMWService
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import utils.JavaJsonUtils

import scala.concurrent.ExecutionContext
import collection.JavaConverters._
import collection.JavaConversions._

abstract class BaseController(protected val cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {

    def requestBody()(implicit request: Request[AnyContent]) = {
        val body = request.body.asJson.getOrElse("{}").toString
        JavaJsonUtils.deserialize[java.util.Map[String, Object]](body).getOrDefault("request", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]]
    }

    def commonHeaders()(implicit request: Request[AnyContent]): java.util.Map[String, Object] = {
        val customHeaders = Map("x-channel-id" -> "channel", "X-Consumer-ID" -> "consumerId", "X-App-Id" -> "appId")
        customHeaders.map(ch => {
            val value = request.headers.get(ch._1)
            if (value.isDefined && !value.isEmpty) {
                collection.mutable.HashMap[String, Object](ch._2 -> value.get).asJava
            } else {
                collection.mutable.HashMap[String, Object]().asJava
            }
        }).flatten.toMap.asJava
    }
}
