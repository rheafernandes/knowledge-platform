package org.sunbird.graph.model;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.dac.model.Node;

import java.util.List;
import java.util.Map;

public interface INode extends IPropertyContainer {

    String getNodeId();

    String getSystemNodeType();

    String getFunctionalObjectType();

    Node toNode();

    void updateMetadata(Request request);

    Map<String, List<String>> validateNode(Request request);

}
