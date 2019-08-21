package controllers.v3

import akka.actor.ActorSystem
import akka.dispatch.Futures
import com.google.inject.Singleton
import controllers.BaseController
import javax.inject.Inject
import play.api.mvc.ControllerComponents

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext}

@Singleton
class ContentController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {


    def create() = Action.async { implicit request =>
        val headers = commonHeaders()
        val channel = headers.getOrDefault("channel", "").asInstanceOf[String]
        if (channel.isEmpty) {
            Futures.successful(BadRequest("""{"message": "Header X-Channel-ID required."}""").as("application/json"))
        } else {
            val body = requestBody()
            val content = body.getOrElse("content", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
            content.putAll(commonHeaders())
            getResult(getRequest("createDataNode", content))
        }
    }
}
