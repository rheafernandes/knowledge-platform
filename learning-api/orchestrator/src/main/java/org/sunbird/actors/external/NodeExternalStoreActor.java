package org.sunbird.actors.external;

import akka.dispatch.Futures;
import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.external.store.NodeExternalStore;
import org.sunbird.external.store.NodeExternalStoreFactory;
import org.sunbird.graph.mgr.BaseGraphManager;

import java.util.Map;

@ActorConfig(tasks = {"updateExternalProps"})
public class NodeExternalStoreActor extends BaseGraphManager {

    private NodeExternalStore externalStore;

    @Override
    public void onReceive(Request request) throws Throwable {
        externalStore = NodeExternalStoreFactory.getStoreInstance((String)request.getContext().get("store_name"));
        String operation = request.getOperation();
        switch (operation) {
            case "updateExternalProps":
                updateExternalProps(request);
                break;
            default:
                break;
        }
    }

    public void updateExternalProps(Request request) {
        externalStore.insert(request.getRequest());
        Patterns.pipe(Futures.successful(new Response()), getContext().dispatcher()).to(sender());
    }
}
