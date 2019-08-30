package org.sunbird.content.actors;

import akka.dispatch.Futures;
import akka.pattern.Patterns;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.content.external.ContentExternalStore;
import org.sunbird.graph.mgr.BaseGraphManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ActorConfig(tasks = {"updateContentBody"})
public class ContentExternalStoreActor extends BaseGraphManager {
    private String keySpace = Platform.config.hasPath("content.keyspace.name")? Platform.config.getString("content.keyspace.name"): "content_store";
    private List<String> primaryKey = Arrays.asList("content_id");
    private ContentExternalStore externalStore = new ContentExternalStore(keySpace, primaryKey);

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "updateContentBody":
                updateContentBody(request);
                break;
            default:
                break;
        }
    }

    public void updateContentBody(Request request) {
        Map<String, Object> data = request.getRequest();
        data.put("content_id", data.get("identifier"));
        data.remove("identifier");
        externalStore.insert(data);
        Patterns.pipe(Futures.successful(new Response()), getContext().dispatcher()).to(sender());
    }
}
