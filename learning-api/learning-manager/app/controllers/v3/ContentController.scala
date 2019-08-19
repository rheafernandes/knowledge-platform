package controllers.v3

import akka.actor.ActorSystem
import akka.dispatch.Futures
import controllers.BaseController
import javax.inject.Inject
import org.sunbird.common.dto.Response
import play.api.mvc.ControllerComponents
import utils.JavaJsonUtils

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class ContentController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {


    def create() = Action.async { implicit request =>
        val headers = commonHeaders()
        val channel = headers.getOrDefault("channel", "").asInstanceOf[String]
        if (channel.isEmpty) {
            Futures.successful(BadRequest("""{"message": "Header X-Channel-ID required."}""").as("application/json"))
        } else {
            val body = requestBody()
            var content = body.getOrElse("content", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
            content.putAll(commonHeaders())

            val response = new Response()
            Futures.successful(Ok(JavaJsonUtils.serialize(response)).as("application/json"))
        }
    }
}
