import akka.actor.Actor
import com.google.inject.AbstractModule
import org.sunbird.actor.service.SunbirdMWService
import org.sunbird.actors.content.ContentActor
import play.libs.akka.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

    override def configure() = {
        super.configure()
        bindActor(classOf[ContentActor], "contentActor")
        println("Initialized learning request router pool...")
    }
}
