package org.sunbird.graph.engine;

import akka.actor.ActorRef;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Map;

public abstract class BaseManager extends BaseActor {

    /**
     * This fetches the schema for given objectType and version and validates data against the schema.
     *
     * @param objectType
     * @param version
     * @param request
     * @return ValidationResult
     * @throws Exception
     */

    public ValidationResult validate(String objectType, String version, Map<String, Object> request) throws Exception {
        ISchemaValidator schema = SchemaValidatorFactory.getInstance(objectType, version);
        return schema.validate(JsonUtils.serialize(request));
    }

    public void ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        sender().tell(response, getSelf());
    }

    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(ResponseParams.StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }
}
