package org.sunbird.graph.engine;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.pattern.Patterns;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.engine.dto.Result;
import org.sunbird.graph.mgr.BaseGraphManager;
import org.sunbird.graph.service.operation.Neo4JBoltNodeOperations;
import scala.concurrent.Future;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class DataNode extends BaseDomainObject {

    private BaseGraphManager manager;

    public DataNode(BaseGraphManager manager, String graphId, String objectType, String version) {
        super(graphId, objectType, version);
        this.manager = manager;
    }

    public void create(Request request) throws Exception {
        Map<String, Object> data = request.getRequest();
        Result validationResult = validate(objectType, data);

        if(validationResult.isValid()) {
            Response response = createNode(validationResult.getNode());
            Future<Object> extPropsResponse = saveExternalProperties(validationResult.getExternalData(), request.getContext());
            Future<Object> updateRelResponse = updateRelations(Arrays.asList(), request.getContext());
            List<Future<Object>> futureList = Arrays.asList(extPropsResponse, updateRelResponse);
            // TODO: onComplete of futureList, if all successful - send the response.
            Future<Iterable<Object>> future = Futures.sequence(futureList, manager.getContext().getDispatcher());
            Future<Object> result = future.map(new Mapper<Iterable<Object>, Object>() {
                @Override
                public Object apply(Iterable<Object> f) {
                    List<Response> resList = StreamSupport.stream(f.spliterator(), false)
                            .filter(res -> {
                        String code = ((Response) res).getResponseCode().name();
                        return !StringUtils.equals(code, ResponseCode.OK.name());
                    }).map(res -> (Response) res).collect(Collectors.toList());
                    if (resList.size() == 0) {
                        return response;
                    } else {
                        return resList.get(0);
                    }
                }
            }, manager.getContext().dispatcher());
            Patterns.pipe(result, manager.getContext().dispatcher()).to(manager.sender());
        } else {
            Response response = new Response();
            response.setResponseCode(ResponseCode.CLIENT_ERROR);
            response.put("messages", validationResult.getMessages());
            Patterns.pipe(Futures.successful(response), manager.getContext().dispatcher()).to(manager.sender());
        }


    }

    private Result validate(String objectType, Map<String, Object> data) throws Exception {
        DefinitionNode definition = new DefinitionNode(graphId, objectType, "1.0");
        return definition.validate(data);
    }

    private Response createNode(Node node) {
        Node addedNode = Neo4JBoltNodeOperations.addNode(graphId, node);
        Response response = new Response();
        response.put("node_id", addedNode.getIdentifier());
        response.put("versionKey", addedNode.getMetadata().get("versionKey"));
        return response;
    }

    private Future<Object> saveExternalProperties(Map<String, Object> externalProps, Map<String, Object> context) {
        // TODO: Update externalProps using ExternalStoreActor (Generic CassandraStorage).
        Request request = new Request(context, externalProps, "saveExternalProperties", objectType);
        return manager.getResult(request);
    }

    private Future updateRelations(List<Relation> relations, Map<String, Object> context) {
        // TODO: update Relations using GraphManager Actor.
        return Futures.successful(new Response());
    }
}
