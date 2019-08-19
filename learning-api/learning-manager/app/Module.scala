import com.google.inject.AbstractModule
import org.sunbird.actor.service.SunbirdMWService

class Module extends AbstractModule {

    override def configure() = {
        super.configure()
        SunbirdMWService.init()
        println("Initialized learning request router pool...")
    }
}
