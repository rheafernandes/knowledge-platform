package org.sunbird.graph.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.JsonUtils;
import org.sunbird.graph.common.NodeDTO;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.common.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.RelationDefinition;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NodeUtil {

    public static Map<String, Object> serialize(Node node, String domainId, DefinitionDTO definition, List<String> fieldList) {
        Map<String, Object> map = new HashMap<String, Object>();
        Object sysLastUpdatedOn = null;
        if (null != node) {
            Map<String, Object> metadata = node.getMetadata();
            if (MapUtils.isNotEmpty(metadata)) {
                sysLastUpdatedOn = metadata.remove(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name());
                List<String> jsonProps = getJSONProperties(definition);
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    if (CollectionUtils.isNotEmpty(fieldList)) {
                        if (fieldList.contains(entry.getKey()))
                            if (jsonProps.contains(entry.getKey().toLowerCase())) {
                                Object val = JsonUtils.convertJSONString((String) entry.getValue());
                                TelemetryManager.log("JSON Property " + entry.getKey() + " converted value is " + val);
                                if (null != val)
                                    map.put(entry.getKey(), val);
                            } else
                                map.put(entry.getKey(), entry.getValue());
                    } else {
                        String key = entry.getKey();
                        if (StringUtils.isNotBlank(key)) {
                            char c[] = key.toCharArray();
                            c[0] = Character.toLowerCase(c[0]);
                            key = new String(c);
                            if (jsonProps.contains(key.toLowerCase())) {
                                Object val = entry.getValue();
                                if (val instanceof String) {
                                    val = JsonUtils.convertJSONString((String) entry.getValue());
                                }
                                TelemetryManager.log("JSON Property " + key + " converted value is " + val);
                                if (null != val)
                                    map.put(key, val);
                            } else
                                map.put(key, entry.getValue());
                        }
                    }
                }
            }
            //TODO: Remove the property after inspection.
            if (sysLastUpdatedOn != null)
                map.put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name(), sysLastUpdatedOn);

            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
            if (CollectionUtils.isNotEmpty(node.getInRelations())) {
                Map<String, List<NodeDTO>> inRelMap = new HashMap<String, List<NodeDTO>>();
                for (Relation inRel : node.getInRelations()) {
                    String key = inRel.getRelationType() + inRel.getStartNodeObjectType();
                    if (inRelDefMap.containsKey(key)) {
                        List<NodeDTO> list = inRelMap.get(key);
                        if (null == list) {
                            list = new ArrayList<NodeDTO>();
                            inRelMap.put(inRel.getRelationType() + inRel.getStartNodeObjectType(), list);
                        }
                        String objectType = inRel.getStartNodeObjectType();
                        String id = inRel.getStartNodeId();
                        if (StringUtils.endsWith(objectType, "Image")) {
                            objectType = objectType.replace("Image", "");
                            id = id.replace(".img", "");
                        }
                        list.add(new NodeDTO(id, inRel.getStartNodeName(), getDescription(inRel.getStartNodeMetadata()),
                                objectType, inRel.getRelationType(), getStatus(inRel.getStartNodeMetadata())));
                    }
                }
                updateReturnMap(map, inRelMap, inRelDefMap);
            }
            if (CollectionUtils.isNotEmpty(node.getOutRelations())) {
                Map<String, List<NodeDTO>> outRelMap = new HashMap<String, List<NodeDTO>>();
                for (Relation outRel : node.getOutRelations()) {
                    String key = outRel.getRelationType() + outRel.getEndNodeObjectType();
                    if (outRelDefMap.containsKey(key)) {
                        List<NodeDTO> list = outRelMap.get(key);
                        if (null == list) {
                            list = new ArrayList<NodeDTO>();
                            outRelMap.put(key, list);
                        }
                        String objectType = outRel.getEndNodeObjectType();
                        String id = outRel.getEndNodeId();
                        if (StringUtils.endsWith(objectType, "Image")) {
                            if (isVisibilityDefault(outRel.getEndNodeMetadata())) {
                                objectType = objectType.replace("Image", "");
                                id = id.replace(".img", "");
                                NodeDTO child = new NodeDTO(id, outRel.getEndNodeName(),
                                        getDescription(outRel.getEndNodeMetadata()), outRel.getEndNodeObjectType(),
                                        outRel.getRelationType(), outRel.getMetadata(), getStatus(outRel.getEndNodeMetadata()));
                                list.add(child);
                            }

                        } else {
                            NodeDTO child = new NodeDTO(id, outRel.getEndNodeName(),
                                    getDescription(outRel.getEndNodeMetadata()), outRel.getEndNodeObjectType(),
                                    outRel.getRelationType(), outRel.getMetadata(), getStatus(outRel.getEndNodeMetadata()));
                            list.add(child);
                        }
                    }
                }
                updateReturnMap(map, outRelMap, outRelDefMap);
            }
            map.put("identifier", node.getIdentifier());
        }
        return map;
    }

    public static Node deserialize(Map<String, Object> map, DefinitionDTO definition, Node graphNode) throws Exception {
        Node node = new Node();
        if (MapUtils.isNotEmpty(map)) {
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelDefMaps(definition, inRelDefMap, outRelDefMap);
            Map<String, List<Relation>> dbRelations = getDBRelations(definition, graphNode, map);
            List<Relation> inRelations = dbRelations.get("in");
            List<Relation> outRelations = dbRelations.get("out");

            Map<String, Object> metadata = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (StringUtils.equalsIgnoreCase("identifier", entry.getKey())) {
                    node.setIdentifier((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
                    node.setObjectType((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("tags", entry.getKey())) {
                    try {
                        // TODO: Check if JsonUtils.convert can be directly used.
                        String objectStr = JsonUtils.serialize(entry.getValue());
                        List<String> tags = JsonUtils.deserialize(objectStr, List.class);
                        if (CollectionUtils.isNotEmpty(tags))
                            metadata.put("keywords", tags);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                } else if (inRelDefMap.containsKey(entry.getKey())) {
                    try {
                        // TODO: Check if JsonUtils.convert can be directly used.
                        String objectStr = JsonUtils.serialize(entry.getValue());
                        List<Map> list = JsonUtils.deserialize(objectStr, List.class);
                        if (null != list) {
                            if (null == inRelations)
                                inRelations = new ArrayList<Relation>();
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) JsonUtils.convert(obj, NodeDTO.class);
                                Relation relation = new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null);
                                if (null != dto.getIndex() && dto.getIndex().intValue() >= 0) {
                                    Map<String, Object> relMetadata = new HashMap<String, Object>();
                                    relMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), dto.getIndex());
                                    relation.setMetadata(relMetadata);
                                }
                                inRelations.add(relation);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    try {
                        // TODO: Check if JsonUtils.convert can be directly used.
                        String objectStr = JsonUtils.serialize(entry.getValue());
                        List<Map> list = JsonUtils.deserialize(objectStr, List.class);
                        if (CollectionUtils.isNotEmpty(list)) {
                            if (null == outRelations)
                                outRelations = new ArrayList<Relation>();
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) JsonUtils.convert(obj, NodeDTO.class);
                                Relation relation = new Relation(null, outRelDefMap.get(entry.getKey()), dto.getIdentifier());
                                if (null != dto.getIndex() && dto.getIndex().intValue() >= 0) {
                                    Map<String, Object> relMetadata = new HashMap<String, Object>();
                                    relMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), dto.getIndex());
                                    relation.setMetadata(relMetadata);
                                }
                                outRelations.add(relation);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                } else {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
            node.setInRelations(inRelations);
            node.setOutRelations(outRelations);
            node.setMetadata(metadata);
        }
        return node;
    }


    private static boolean isVisibilityDefault(Map<String, Object> endNodeMetadata) {
        return MapUtils.isNotEmpty(endNodeMetadata) ? (!StringUtils.equalsIgnoreCase("Default", (String) endNodeMetadata.get("visibility"))) : false;
    }


    private static String getDescription(Map<String, Object> metadata) {
        return (MapUtils.isNotEmpty(metadata)) ? (String) metadata.get("description") : null;
    }


    private static String getStatus(Map<String, Object> metadata) {
        return (MapUtils.isNotEmpty(metadata)) ? (String) metadata.get("status") : null;
    }


    private static void
    getRelDefMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
                  Map<String, String> outRelDefMap) {
        if (null != definition) {
            if (CollectionUtils.isNotEmpty(definition.getInRelations())) {
                definition.getInRelations().stream().filter(rDef -> StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())).forEach(rDef -> inRelDefMap.put(rDef.getTitle(), rDef.getRelationName()));
            }
            if (CollectionUtils.isNotEmpty(definition.getOutRelations())) {
                definition.getOutRelations().stream().filter(rDef -> StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())).forEach(rDef -> outRelDefMap.put(rDef.getTitle(), rDef.getRelationName()));
            }
        }
    }

    public static void getRelationDefinitionMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
                                                 Map<String, String> outRelDefMap) {
        if (null != definition) {
            if (CollectionUtils.isNotEmpty(definition.getInRelations())) {
                definition.getInRelations().forEach(rDef -> getRelationDefinitionKey(rDef, inRelDefMap));
            }
            if (CollectionUtils.isNotEmpty(definition.getOutRelations())) {
                definition.getOutRelations().forEach(rDef -> getRelationDefinitionKey(rDef, outRelDefMap));
            }
        }
    }

    private static void getRelationDefinitionKey(RelationDefinition rDef, Map<String, String> relDefMap) {
        if (null != rDef && CollectionUtils.isNotEmpty(rDef.getObjectTypes())) {
            rDef.getObjectTypes().stream().map(type -> rDef.getRelationName() + type).forEach(key -> relDefMap.put(key, rDef.getTitle()));
        }
    }

    private static List<String> getJSONProperties(DefinitionDTO definition) {
        List<String> props = new ArrayList<String>();
        if (null != definition && CollectionUtils.isNotEmpty(definition.getProperties())) {
            props = definition.getProperties().stream().filter(mDef -> StringUtils.equalsIgnoreCase("json", mDef.getDataType()) && StringUtils.isNotBlank(mDef.getPropertyName())).map(mDef -> mDef.getPropertyName().toLowerCase()).collect(Collectors.toList());
        }
        TelemetryManager.log("JSON properties: " + props);
        return props;
    }

    private static void updateReturnMap(Map<String, Object> map, Map<String, List<NodeDTO>> relMap,
                                        Map<String, String> relDefMap) {
        if (MapUtils.isNotEmpty(relMap)) {
            relMap.entrySet().stream().filter(entry -> relDefMap.containsKey(entry.getKey())).forEach(entry -> {
                String returnKey = relDefMap.get(entry.getKey());
                if (map.containsKey(returnKey)) {
                    List<NodeDTO> nodes = (List<NodeDTO>) map.get(returnKey);
                    nodes.addAll(entry.getValue());
                    map.put(returnKey, nodes);
                } else {
                    map.put(returnKey, entry.getValue());
                }
            });
        } else if (MapUtils.isNotEmpty(relDefMap)) {
            List<Object> list = new ArrayList<Object>();
            relDefMap.values().stream().filter(StringUtils::isNotBlank).forEach(val -> map.put(val, list));
        }
    }

    private static Map<String, List<Relation>> getDBRelations(DefinitionDTO definition, Node graphNode, Map<String, Object> map) {
        List<Relation> inRelations = null;
        List<Relation> outRelations = null;
        if (null != graphNode) {
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
            if (CollectionUtils.isNotEmpty(graphNode.getInRelations())) {
                for (Relation inRel : graphNode.getInRelations()) {
                    String key = inRel.getRelationType() + inRel.getStartNodeObjectType();
                    if (inRelDefMap.containsKey(key)) {
                        String value = inRelDefMap.get(key);
                        if (!map.containsKey(value)) {
                            if (null == inRelations)
                                inRelations = new ArrayList<Relation>();
                            TelemetryManager.log("adding " + value + " to inRelations");
                            inRelations.add(inRel);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(graphNode.getOutRelations())) {
                for (Relation outRel : graphNode.getOutRelations()) {
                    String key = outRel.getRelationType() + outRel.getEndNodeObjectType();
                    if (outRelDefMap.containsKey(key)) {
                        String value = outRelDefMap.get(key);
                        if (!map.containsKey(value)) {
                            if (null == outRelations)
                                outRelations = new ArrayList<Relation>();
                            TelemetryManager.log("adding " + value + " to outRelations");
                            outRelations.add(outRel);
                        }
                    }
                }
            }
        }
        Map<String, List<Relation>> relationMaps = new HashMap<String, List<Relation>>();
        relationMaps.put("in", inRelations);
        relationMaps.put("out", outRelations);
        return relationMaps;
    }

    public static void filterNodeRelationships(Map<String, Object> responseMap, DefinitionDTO definition) {
        if (null != definition) {
            if (CollectionUtils.isNotEmpty(definition.getInRelations())) {
                List<String> inRelations = definition.getInRelations().stream().map(RelationDefinition::getTitle).collect(Collectors.toList());
                inRelations.stream().map(rel -> (List<NodeDTO>) responseMap.get(rel)).filter(CollectionUtils::isNotEmpty).forEach(relMetaData -> {
                    Predicate<NodeDTO> predicate = p -> StringUtils.isNotBlank(p.getStatus()) && !StringUtils.equalsIgnoreCase(p.getStatus(), "Live");
                    relMetaData.removeIf(predicate);
                });
            }
            if (CollectionUtils.isNotEmpty(definition.getOutRelations())) {
                List<String> outRelations = definition.getOutRelations().stream().map(RelationDefinition::getTitle).collect(Collectors.toList());
                outRelations.stream().map(rel -> (List<NodeDTO>) responseMap.get(rel)).filter(CollectionUtils::isNotEmpty).forEach(relMetaData -> {
                    Predicate<NodeDTO> predicate = p -> StringUtils.isNotBlank(p.getStatus()) && !StringUtils.equalsIgnoreCase(p.getStatus(), "Live");
                    relMetaData.removeIf(predicate);
                });
            }
        }
    }

    public static Map<String, Object> convertGraphNodeWithoutRelations(Node node, String domainId, DefinitionDTO definition, List<String> fieldList) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != node) {
            Map<String, Object> metadata = node.getMetadata();
            if (MapUtils.isNotEmpty(metadata)) {
                metadata.forEach((key, value) -> {
                    if (CollectionUtils.isNotEmpty(fieldList)) {
                        if (fieldList.contains(key))
                            map.put(key, value);
                    } else {
                        if (StringUtils.isNotBlank(key)) {
                            char c[] = key.toCharArray();
                            c[0] = Character.toLowerCase(c[0]);
                            key = new String(c);
                            map.put(key, value);
                        }
                    }
                });
            }
            map.put("identifier", node.getIdentifier());
        }
        return map;
    }
}
