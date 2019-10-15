package org.sunbird.graph.dac.enums;

import org.apache.commons.lang3.StringUtils;

public enum RelationTypes {

	HIERARCHY("isParentOf"), CONSTITUENCY("hasConstituent"), 
    SET_MEMBERSHIP("hasMember"), 
    SEQUENCE_MEMBERSHIP("hasSequenceMember"),
    ASSOCIATED_TO("associatedTo"),
    SUB_SET("hasSubSet"),
	CO_OCCURRENCE("coOccurrence"), PRE_REQUISITE("preRequisite"),
	SUPERSEDED("superseded"),
    SYNONYM("synonym"),
    ANTONYM("hasAntonym"),
    HYPERNYM("hasHypernym"),
    HOLONYM("hasHolonym"),
    HYPONYM("hasHyponym"),
    MERONYM("hasMeronym"),
    FOLLOWS("follows"),
    TOOL("usesTool"),
    WORKER("hasWorker"),
    ACTION("hasAction"),
    OBJECT("actionOn"),
	CONVERSE("hasConverse");

    private String relationName;

    RelationTypes(String relationName) {
        this.relationName = relationName;
    }

    public String relationName() {
        return this.relationName;
    }
    
    public static boolean isValidRelationType(String str) {
        if (StringUtils.isBlank(str))
            return false;
        return true;
    }
}
