package org.sunbird.graph.model.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.JsonUtils;
import org.sunbird.graph.dac.model.Node;


public class DefinitionDTO implements Serializable {

    private static final long serialVersionUID = -228044920246692592L;
    private String identifier;
    private String objectType;
    private List<MetadataDefinition> properties;
    private List<RelationDefinition> inRelations;
    private List<RelationDefinition> outRelations;
    private Map<String, Object> metadata;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public List<MetadataDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<MetadataDefinition> properties) {
        this.properties = properties;
    }

    public List<RelationDefinition> getInRelations() {
        return inRelations;
    }

    public void setInRelations(List<RelationDefinition> inRelations) {
        this.inRelations = inRelations;
    }

    public List<RelationDefinition> getOutRelations() {
        return outRelations;
    }

    public void setOutRelations(List<RelationDefinition> outRelations) {
        this.outRelations = outRelations;
    }


    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    
    @SuppressWarnings("unchecked")
	public void fromNode(Node node) {
    	setIdentifier(node.getIdentifier());
        setObjectType(node.getObjectType());
        this.properties = new ArrayList<>();
        Map<String, Object> nodeMetadata = node.getMetadata();
        if (null != nodeMetadata && !nodeMetadata.isEmpty()) {
            Map<String, Object> otherMetadata = new HashMap<String, Object>();
            otherMetadata.putAll(nodeMetadata);
            String indexableMetadata = (String) nodeMetadata.get(DefinitionNode.INDEXABLE_METADATA_KEY);
            if (StringUtils.isNotBlank(indexableMetadata)) {
                try {
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) JsonUtils.deserialize(indexableMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.properties.add((MetadataDefinition) JsonUtils.convert(metaMap, MetadataDefinition.class));
                    }
                    otherMetadata.remove(DefinitionNode.INDEXABLE_METADATA_KEY);
                } catch (Exception e) {
                }
            }
            String nonIndexableMetadata = (String) nodeMetadata.get(DefinitionNode.NON_INDEXABLE_METADATA_KEY);
            if (StringUtils.isNotBlank(nonIndexableMetadata)) {
                try {
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) JsonUtils.deserialize(nonIndexableMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.properties.add((MetadataDefinition) JsonUtils.convert(metaMap, MetadataDefinition.class));
                    }
                    otherMetadata.remove(DefinitionNode.NON_INDEXABLE_METADATA_KEY);
                } catch (Exception e) {
                }
            }
            String inRelationsMetadata = (String) nodeMetadata.get(DefinitionNode.IN_RELATIONS_KEY);
            if (StringUtils.isNotBlank(inRelationsMetadata)) {
                try {
                    this.inRelations = new ArrayList<RelationDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) JsonUtils.deserialize(inRelationsMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.inRelations.add((RelationDefinition) JsonUtils.convert(metaMap, RelationDefinition.class));
                    }
                    otherMetadata.remove(DefinitionNode.IN_RELATIONS_KEY);
                } catch (Exception e) {
                }
            }
            String outRelationsMetadata = (String) nodeMetadata.get(DefinitionNode.OUT_RELATIONS_KEY);
            if (StringUtils.isNotBlank(outRelationsMetadata)) {
                try {
                    this.outRelations = new ArrayList<RelationDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) JsonUtils.deserialize(outRelationsMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.outRelations.add((RelationDefinition) JsonUtils.convert(metaMap, RelationDefinition.class));
                    }
                    otherMetadata.remove(DefinitionNode.OUT_RELATIONS_KEY);
                } catch (Exception e) {
                }
            }

            try {
                otherMetadata.remove(DefinitionNode.REQUIRED_PROPERTIES);
            } catch (Exception e) {
            }
            setMetadata(otherMetadata);
        }
    }
}
