package org.sunbird.graph.engine;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.engine.dto.Result;
import org.sunbird.graph.util.NodeValidator;
import org.sunbird.schema.ISchemaValidator;
import org.sunbird.schema.SchemaValidatorFactory;
import org.sunbird.schema.dto.ValidationResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefinitionNode extends BaseDomainObject {

    private ISchemaValidator schemaValidator;
    private Map<String, Object> inRelationSchema;
    private Map<String, Object> outRelationSchema;

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
        }

    }


    /**
     *
     * @param input
     * @return
     * @throws Exception
     */
    public Result validate(Map<String, Object> input) throws Exception {
        // TODO: Read JSON Schema of the given object,
        //  execute conditional (if-then-else) statements and update request data.
        ValidationResult jsonResult = schemaValidator.validate(input);
        setSystemProperties(jsonResult.getMetadata());
        // TODO: set audit properties to validated data.
        validateRelations(jsonResult.getRelations());
        return new Result(objectType, getNode(jsonResult.getMetadata(), jsonResult.getRelations()), jsonResult.getExternalData());
    }

    private Map<String, Object> validateRelations(Map<String, Object> relations) {
        if(MapUtils.isNotEmpty(relations)) {
            List<String> ids = relations.entrySet().stream()
                    .map(e -> (List<Map<String, Object>>) e.getValue())
                    .filter(list -> CollectionUtils.isNotEmpty(list))
                    .map(list -> list.stream().filter(m -> MapUtils.isNotEmpty(m)).map(m -> (String) m.get("identifier")))
                    .flatMap(Stream::distinct).collect(Collectors.toList());

            // TODO: use ids and validate them using NodeValidator.validate(List<String> ids)
            // get all the ids and objectType - List size and ids size
            List<Map<String,Object>> nodesInfo = NodeValidator.validate(ids);
            //TODO: Remove this sysout
            System.out.println("nodesInfo : "+nodesInfo);
        }
        return relations;
    }

    private Node getNode(Map<String, Object> metadata, Map<String, Object> relations) {
        Node node = new Node(graphId, SystemNodeTypes.DATA_NODE.name(), objectType);
        node.setIdentifier(Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp()));
        node.setMetadata(metadata);

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

        return node;
    }



}
