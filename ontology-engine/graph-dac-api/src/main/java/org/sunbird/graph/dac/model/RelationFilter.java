package org.sunbird.graph.dac.model;

import org.sunbird.graph.dac.model.RelationCriterion.DIRECTION;

import java.io.Serializable;

public class RelationFilter implements Serializable {

    private static final long serialVersionUID = 6067704868050557358L;
    
    private String name;
    private int fromDepth = 1;
    private int toDepth = 1;
    private String direction = DIRECTION.OUT.name();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getFromDepth() {
        return fromDepth;
    }
    public int getToDepth() {
        return toDepth;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
    

}
