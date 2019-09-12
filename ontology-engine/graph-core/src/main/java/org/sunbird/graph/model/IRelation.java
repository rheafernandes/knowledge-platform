package org.sunbird.graph.model;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.dac.model.Node;

import java.util.List;
import java.util.Map;


/**
 * @author rayulu
 * 
 */
public interface IRelation extends IPropertyContainer {

    void validate(Request request);

    String getRelationType();

    Node getStartNode();

    Node getEndNode();
    
    Map<String, Object> getMetadata();

    boolean isType(String relationType);

	String createRelation(final Request req);
    
	String deleteRelation(Request req);

	Map<String, List<String>> validateRelation(Request request);

}
