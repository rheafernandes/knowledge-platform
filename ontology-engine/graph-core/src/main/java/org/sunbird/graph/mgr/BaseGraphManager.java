package org.sunbird.graph.mgr;

import akka.actor.ActorRef;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.service.SunbirdMWService;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ErrorCodes;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import scala.concurrent.Future;

import java.util.Map;

public abstract class BaseGraphManager {

    public Future<Object> getResult(Request request) {
        return SunbirdMWService.execute(request);
    }

    // TODO: remove errorMessage - message is derived from code.
    public Response ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }

    public Response OK() {
        Response response = new Response();
        response.setParams(getSucessStatus());
        return response;
    }

    public Response OK(String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getSucessStatus());
        return response;
    }

    public Response OK(Map<String, Object> responseObjects, ActorRef parent) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            for (Map.Entry<String, Object> entry : responseObjects.entrySet()) {
                response.put(entry.getKey(), entry.getValue());
            }
        }
        response.setParams(getSucessStatus());
        return response;
    }

    public Response ERROR(Throwable e) {
        return handleException(e);
    }

    public Response ERROR(Throwable e, String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        ResponseParams params = new ResponseParams();
        params.setStatus(ResponseParams.StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(ErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        return response;
    }

    public Response getErrorResponse(String errorCode, String errorMessage, ResponseCode code) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }

    public Response ERROR(String errorCode, String errorMessage, ResponseCode code) {
        return getErrorResponse(errorCode, errorMessage, code);
    }

    public boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(ResponseParams.StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public String getErrorMessage(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            String msg = params.getErrmsg();
            if (StringUtils.isNotBlank(msg))
                return msg;
            return params.getErr();
        }
        return null;
    }


    public Response handleException(Throwable e) {
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(ResponseParams.StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(ErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        return response;
    }


    private ResponseParams getSucessStatus() {
        ResponseParams params = new ResponseParams();
        params.setErr("0");
        params.setStatus(ResponseParams.StatusType.successful.name());
        params.setErrmsg("Operation successful");
        return params;
    }

    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(ResponseParams.StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

    private void setResponseCode(Response res, Throwable e) {
        if (e instanceof ClientException) {
            res.setResponseCode(ResponseCode.CLIENT_ERROR);
        } else if (e instanceof ServerException) {
            res.setResponseCode(ResponseCode.SERVER_ERROR);
        } else if (e instanceof ResourceNotFoundException) {
            res.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
        } else {
            res.setResponseCode(ResponseCode.SERVER_ERROR);
        }
    }

    protected String setErrMessage(Throwable e){
        if (e instanceof MiddlewareException) {
            return e.getMessage();
        } else {
            return "Something went wrong in server while processing the request";
        }
    }
}
