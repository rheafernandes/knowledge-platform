package org.sunbird.graph.schema.validator

import org.sunbird.common.dto.Request
import org.sunbird.common.exception.ResourceNotFoundException
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.exception.GraphEngineErrorCodes
import org.sunbird.graph.schema.IDefinitionNode
import org.sunbird.graph.service.operation.{Neo4JBoltNodeOperations, Neo4JBoltSearchOperations}

trait VersioningNode extends IDefinitionNode {

    val statusList = List("Live", "Unlisted")
    val IMAGE_SUFFIX = ".img"
    val IMAGE_OBJECT_SUFFIX = "Image"


    abstract override def getNode(identifier: String, operation: String, mode: String): Node = {
        operation match {
            case "update" => getNodeToUpdate(identifier);
            case "read" => getNodeToRead(identifier, mode)
            case _ => getNodeToRead(identifier, mode)
        }
    }

    private def getNodeToUpdate(identifier: String) = {
        val node:Node = super.getNode(identifier , "update", null)
        if(null == node)
            throw new ResourceNotFoundException(GraphEngineErrorCodes.ERR_INVALID_NODE.name, "Node Not Found With Identifier : " + identifier)
        if(schemaValidator.getConfig.hasPath("version") && "enable".equalsIgnoreCase(schemaValidator.getConfig.getString("version"))){
            getEditableNode(identifier, node)
        } else {
            node
        }
    }

    private def getNodeToRead(identifier: String, mode: String) = {
        val node: Node = {
            if("edit".equalsIgnoreCase(mode)){
                val imageNode = super.getNode(identifier + IMAGE_SUFFIX , "read", mode)
                if(null == imageNode){
                    super.getNode(identifier , "read", mode)
                }else {
                    imageNode
                }
            } else {
                super.getNode(identifier , "read", mode)
            }
        }

        if(null == node)
            throw new ResourceNotFoundException(GraphEngineErrorCodes.ERR_INVALID_NODE.name, "Node Not Found With Identifier : " + identifier)

        node
    }


    private def getEditableNode(identifier: String, node: Node): Node = {
        val status = node.getMetadata.get("status").asInstanceOf[String]
        if(statusList.contains(status)) {
            val imageId = node.getIdentifier + IMAGE_SUFFIX
            val imageNode = Neo4JBoltSearchOperations.getNodeByUniqueId(node.getGraphId, imageId, false, new Request())
            if(null == imageNode) {
                node.setIdentifier(imageId)
                node.setObjectType(node.getObjectType + IMAGE_OBJECT_SUFFIX)
                Neo4JBoltNodeOperations.addNode(node.getGraphId, node)
            } else
                imageNode
        } else
            node
    }

}
