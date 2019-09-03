package org.sunbird.graph.engine;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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

    public DefinitionNode(String graphId, String objectType, String version) throws Exception {
        super(graphId, objectType, version);
        this.schemaValidator = SchemaValidatorFactory.getInstance(objectType, version);
    }


    /**
     *
     * @param data
     * @return
     * @throws Exception
     */
    public Result validate(Map<String, Object> data) throws Exception {
        // TODO: Read JSON Schema of the given object,
        //  execute conditional (if-then-else) statements and update request data.
        ValidationResult jsonResult = schemaValidator.validate(data);

        setSystemProperties(jsonResult.getMetadata());
        // TODO: set audit properties to validated data.
        validateRelations(jsonResult.getRelations());
        return new Result(graphId, objectType, jsonResult);
    }

    private void validateRelations(Map<String, Object> relations) {
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

        } else {

        }
    }

}
