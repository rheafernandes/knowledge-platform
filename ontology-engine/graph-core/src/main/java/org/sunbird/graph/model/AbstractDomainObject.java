package org.sunbird.graph.model;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.mgr.IGraphDACGraphMgr;
import org.sunbird.graph.dac.mgr.IGraphDACNodeMgr;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltGraphMgrImpl;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltNodeMgrImpl;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltSearchMgrImpl;
import org.sunbird.graph.exception.GraphEngineErrorCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDomainObject {

    protected String graphId;


    protected IGraphDACGraphMgr graphMgr = new Neo4JBoltGraphMgrImpl();
    protected IGraphDACSearchMgr searchMgr = new Neo4JBoltSearchMgrImpl();
    protected IGraphDACNodeMgr nodeMgr = new Neo4JBoltNodeMgrImpl();

    public AbstractDomainObject(String graphId) {
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_GRAPH_ID.name(), "GraphId is blank");
        }
        if (checkForWhiteSpace(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_GRAPH_ID.name(), "GraphId should not have white spaces");
        }
        this.graphId = graphId;

    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    protected boolean checkForWhiteSpace(String name) {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(name);
        boolean found = matcher.find();
        return found;
    }

    protected boolean checkForCharacter(String name, String character) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(character)) {
            if (name.contains(character))
                return true;
        }
        return false;
    }

    protected Request getRequestObject(Request req, String manager, String operation, String key, Object value) {
        Request request = new Request(req);
        request.setOperation(operation);
        if (StringUtils.isNotBlank(key) && null != value) {
            request.put(key, value);
        }
        return request;
    }

    protected Request getRequestObject(Request req, String manager, String operation, Map<String, Object> params) {
        Request request = new Request(req);
        request.setOperation(operation);
        if (null != params && !params.isEmpty()) {
            for (Entry<String, Object> param : params.entrySet()) {
                request.put(param.getKey(), param.getValue());
            }
        }
        return request;
    }

    protected List<String> getErrorMessages(Map<String, List<String>> messageMap) {
        List<String> errMessages = new ArrayList<String>();
        if (null != messageMap) {
            for (List<String> list : messageMap.values()) {
                if (null != list && !list.isEmpty()) {
                    for (String msg : list) {
                        if (StringUtils.isNotBlank(msg)) {
                            errMessages.add(msg);
                        }
                    }
                }
            }
        }
        return errMessages;
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

    public boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(ResponseParams.StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
