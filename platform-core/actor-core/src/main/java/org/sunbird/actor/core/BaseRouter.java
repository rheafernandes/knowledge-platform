package org.sunbird.actor.core;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.FromConfig;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;

/**
 * @author Mahesh Kumar Gangula
 */
public abstract class BaseRouter extends BaseActor {

    public abstract void route(Request request) throws Throwable;

    protected abstract void cacheActor(String key, ActorRef actor);

    @Override
    public void onReceive(Request request) throws Throwable {
        route(request);
    }

    private Set<Class<? extends BaseActor>> getActors() {
        synchronized (BaseRouter.class) {
            Reflections reflections = new Reflections("org.sunbird");
            Set<Class<? extends BaseActor>> actors = reflections.getSubTypesOf(BaseActor.class);
            return actors;
        }
    }

    protected void initActors(ActorContext context, String name) {
        Set<Class<? extends BaseActor>> actors = getActors();
        for (Class<? extends BaseActor> actor : actors) {
            ActorConfig routerDetails = actor.getAnnotation(ActorConfig.class);
            if (null != routerDetails) {
                String dispatcher = routerDetails.dispatcher();
                switch (name) {
                    case "RequestRouter":
                        String[] operations = routerDetails.tasks();
                        dispatcher = (StringUtils.isNotBlank(dispatcher)) ? dispatcher : "rr-usr-dispatcher";
                        createActor(context, actor, operations, dispatcher);
                        break;
                    default:
                        System.out.println("Router with name '" + name + "' not supported.");
                        break;
                }
            } else {
                //				System.out.println(actor.getSimpleName() + " don't have config.");
            }
        }
    }

    private void createActor(ActorContext context, Class<? extends BaseActor> actor, String[] operations, String dispatcher) {
        if (null != operations && operations.length > 0) {
            Props props = null;
            if (StringUtils.isNotBlank(dispatcher)) {
                props = Props.create(actor).withDispatcher(dispatcher);
            } else {
                props = Props.create(actor);
            }
            ActorRef actorRef = context.actorOf(FromConfig.getInstance().props(props), actor.getSimpleName());
            String parentName = self().path().name();
            for (String operation : operations) {
                cacheActor(getKey(parentName, operation), actorRef);
            }
        }
    }

    protected static String getKey(String name, String operation) {
        return name + ":" + operation;
    }
}
