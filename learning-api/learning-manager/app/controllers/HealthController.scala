package controllers

import akka.actor.ActorSystem
import javax.inject._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

class HealthController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

    def health() = Action.async { implicit request =>
        getFutureMessage(1.second).map { msg => Ok(msg) }
    }

    private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
        val promise: Promise[String] = Promise[String]()
        actorSystem.scheduler.scheduleOnce(delayTime) {
            promise.success("Health API Testing!...")
        }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
        promise.future
    }
}
