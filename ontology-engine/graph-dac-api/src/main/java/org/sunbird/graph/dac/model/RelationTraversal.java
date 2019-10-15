package org.sunbird.graph.dac.model;

import java.io.Serializable;

public class RelationTraversal implements Serializable {

    private static final long serialVersionUID = 4380858539752652736L;
    public static final int DIRECTION_IN = 1;
    public static final int DIRECTION_OUT = 2;
    public static final int DIRECTION_BOTH = 0;

    private String relationName;
    private int direction = DIRECTION_BOTH;

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        if (direction == DIRECTION_IN || direction == DIRECTION_OUT || direction == DIRECTION_BOTH)
            this.direction = direction;
        else
            this.direction = DIRECTION_BOTH;
    }
}
