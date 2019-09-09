package org.sunbird.graph.engine;


import org.sunbird.common.DateUtils;

import java.util.Map;

public abstract class BaseDomainObject {

    public String graphId;
    public String objectType;
    public String version;

    public BaseDomainObject(String graphId, String objectType, String version) {
        this.graphId = graphId;
        this.objectType = objectType;
        this.version = version;
    }

    public void setSystemProperties(Map<String, Object> data) {
        data.put("createdOn", DateUtils.formatCurrentDate());
    }
}
