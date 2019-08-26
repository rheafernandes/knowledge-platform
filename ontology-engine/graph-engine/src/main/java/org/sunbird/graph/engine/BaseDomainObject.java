package org.sunbird.graph.engine;

import org.sunbird.common.DateUtils;
import org.sunbird.common.dto.Request;

import java.util.Map;

public abstract class BaseDomainObject {

    public String graphId = "domain";
    public String objectType;
    public String version;

    public BaseDomainObject(String objectType, String version) {
        this.objectType = objectType;
        this.version = version;
    }

    public void setSystemProperties() {

    }

    public void create(Map<String, Object> data) {
        data.put("createdOn", DateUtils.formatCurrentDate());
    }

    public void update(Map<String, Object> data) {
        data.put("lastUpdatedOn", DateUtils.formatCurrentDate());
    }
}
