package org.sunbird.graph.engine;


public abstract class BaseDomainObject {

    public String graphId = "domain";
    public String objectType;
    public String version;

    public BaseDomainObject(String graphId, String objectType, String version) {
        this.objectType = objectType;
        this.version = version;
    }
}
