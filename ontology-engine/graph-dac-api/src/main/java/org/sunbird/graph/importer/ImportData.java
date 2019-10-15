package org.sunbird.graph.importer;

import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ImportData implements Serializable {

    private static final long serialVersionUID = 4500122507402085192L;

    private List<Node> definitionNodes;
    private List<Node> dataNodes;
    private Map<String, List<String>> tagMembersMap;
    private List<Relation> relations;

    public ImportData(List<Node> definitionNodes, List<Node> dataNodes, List<Relation> relations,
            Map<String, List<String>> tagMembersMap) {
        this.definitionNodes = definitionNodes;
        this.dataNodes = dataNodes;
        this.tagMembersMap = tagMembersMap;
        this.relations = relations;
    }

    /**
     * @return the definitionNodes
     */
    public List<Node> getDefinitionNodes() {
        return definitionNodes;
    }

    /**
     * @param definitionNodes
     *            the definitionNodes to set
     */
    public void setDefinitionNodes(List<Node> definitionNodes) {
        this.definitionNodes = definitionNodes;
    }

    /**
     * @return the dataNodes
     */
    public List<Node> getDataNodes() {
        return dataNodes;
    }

    /**
     * @return the relations
     */
    public List<Relation> getRelations() {
        return relations;
    }

    /**
     * @param relations
     *            the relations to set
     */
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
