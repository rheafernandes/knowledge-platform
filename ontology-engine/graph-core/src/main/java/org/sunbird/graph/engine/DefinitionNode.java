package org.sunbird.graph.engine;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.engine.dto.ProcessingNode;
import org.sunbird.graph.validator.NodeValidator;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefinitionNode extends BaseDomainObject {

    private ISchemaValidator schemaValidator;
    private Map<String, Object> inRelationSchema;
    private Map<String, Object> outRelationSchema;
    private List<String> outRelationObjectTypes;

    public DefinitionNode(String graphId, String objectType, String version) throws Exception {
        super(graphId, objectType, version);
        this.schemaValidator = SchemaValidatorFactory.getInstance(objectType, version);
        if (this.schemaValidator.getConfig().hasPath("relations")) {
            this.inRelationSchema = this.schemaValidator.getConfig().getObject("relations").unwrapped().entrySet().stream().filter(e ->  {
                Map<String, String> relation = (Map<String, String>) e.getValue();
                return StringUtils.equalsIgnoreCase(relation.get("direction"), "in");
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            this.outRelationSchema = this.schemaValidator.getConfig().getObject("relations").unwrapped().entrySet().stream().filter(e ->  {
                Map<String, String> relation = (Map<String, String>) e.getValue();
                return StringUtils.equalsIgnoreCase(relation.get("direction"), "out");
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            this.outRelationObjectTypes = this.outRelationSchema.values().stream().map(s -> (Map<String, Object>) s)
                    .map(s -> {
                        String type = (String) s.get("type");
                        List<String> objects = (List<String>)s.get("objects");
                        return objects.stream().map(obj -> type + ":" + obj);
                    }).flatMap(Stream::distinct).collect(Collectors.toList());
        }
    }

    public List<String> getOutRelationObjectTypes() {
        return outRelationObjectTypes;
    }

    /**
     *
     * @param node
     * @return
     * @throws Exception
     */
    public void validate(ProcessingNode node) throws Exception {
        // TODO: Read JSON Schema of the given object,
        //  execute conditional (if-then-else) statements and update request data.
        schemaValidator.validate(node.getMetadata());
        setSystemProperties(node.getMetadata());
        // TODO: set audit properties to validated data.
        validateRelations(node);
    }

    private void validateRelations(ProcessingNode node) {
        List<Relation> relations = node.getNewRelations();
        if(CollectionUtils.isNotEmpty(relations)) {
            List<String> ids = relations.stream()
                    .map(r -> Arrays.asList(r.getStartNodeId(), r.getEndNodeId()))
                    .flatMap(List::stream)
                    .filter(id -> StringUtils.isNotBlank(id)).distinct().collect(Collectors.toList());
            ids.remove(node.getIdentifier());
            Map<String, Node> relationNodes = NodeValidator.validate(graphId, ids);
            node.setNodeType(SystemNodeTypes.DATA_NODE.name());
            relationNodes.put(node.getIdentifier(), node);
            node.setRelationNodes(relationNodes);
            // TODO: behavior validation should be here.
        }
    }

    public ProcessingNode getNode(Map<String, Object> input) {
        ValidationResult result = schemaValidator.getStructuredData(input);
        Node node = new Node(graphId, result.getMetadata());
        // TODO: set SYS_NODE_TYPE, FUNC_OBJECT_TYPE
        node.setNodeType(SystemNodeTypes.DATA_NODE.name());
        node.setObjectType(objectType);
        if(StringUtils.isBlank(node.getIdentifier())) {
            node.setIdentifier(Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp()));
        }
        setRelations(node, result.getRelations());
        return new ProcessingNode(node, result.getExternalData());
    }

    public ProcessingNode getNode(String identifier, Map<String, Object> input) {
        // TODO: used for update operations.
        return null;
    }


    private void setRelations(Node node, Map<String, Object> relations) {
        if (MapUtils.isNotEmpty(relations)) {
            List<Relation> inRelations = relations.entrySet().stream()
                    .filter(e -> this.inRelationSchema.keySet().contains(e.getKey()))
                    .map(e -> {
                        Map<String, String> relSchema = (Map<String, String>) this.inRelationSchema.get(e.getKey());
                        List<Map<String, Object>> relData = (List<Map<String, Object>>) e.getValue();

                        return relData.stream().map(r -> {
                            return new Relation(node.getIdentifier(), relSchema.get("type"), (String) r.get("identifier"));
                        }).collect(Collectors.toList());
                    }).flatMap(List::stream).collect(Collectors.toList());

            node.setInRelations(inRelations);

            List<Relation> outRelations = relations.entrySet().stream()
                    .filter(e -> this.outRelationSchema.keySet().contains(e.getKey()))
                    .map(e -> {
                        Map<String, String> relSchema = (Map<String, String>) this.outRelationSchema.get(e.getKey());
                        List<Map<String, Object>> relData = (List<Map<String, Object>>) e.getValue();

                        return relData.stream().map(r -> {
                            return new Relation(node.getIdentifier(), relSchema.get("type"), (String) r.get("identifier"));
                        }).collect(Collectors.toList());
                    }).flatMap(List::stream).collect(Collectors.toList());

            node.setOutRelations(outRelations);
        }
    }

}