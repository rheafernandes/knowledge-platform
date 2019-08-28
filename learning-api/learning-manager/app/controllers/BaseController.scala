package controllers

import org.sunbird.actor.service.SunbirdMWService
import org.sunbird.common.dto.Response
import org.sunbird.common.exception.ResponseCode
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}
import utils.JavaJsonUtils

import scala.concurrent.{ExecutionContext, Future}
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

    def getRequest(input: java.util.Map[String, AnyRef], context: java.util.Map[String, AnyRef], operation: String): org.sunbird.common.dto.Request = {
        new org.sunbird.common.dto.Request(context, input, operation, null);
    }

    def getResult(apiId: String, request: org.sunbird.common.dto.Request) : Future[Result] = {
        val future = SunbirdMWService.execute(request)
        future.map(f => {
            val result = f.asInstanceOf[Response]
            result.setId(apiId)
            val response = JavaJsonUtils.serialize(result);
            result.getResponseCode match {
                case ResponseCode.OK => Ok(response).as("application/json")
                case ResponseCode.CLIENT_ERROR => BadRequest(response).as("application/json")
                case ResponseCode.RESOURCE_NOT_FOUND => NotFound(response).as("application/json")
                case _ => play.api.mvc.Results.InternalServerError(response).as("application/json")
            }
        })
    }
}
