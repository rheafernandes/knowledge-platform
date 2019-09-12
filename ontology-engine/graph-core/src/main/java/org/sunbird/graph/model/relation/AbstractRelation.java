package org.sunbird.graph.model.relation;

import akka.dispatch.Futures;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.mgr.BaseGraphManager;
import org.sunbird.graph.model.AbstractDomainObject;
import org.sunbird.graph.model.IRelation;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRelation extends AbstractDomainObject implements IRelation {

	protected Node startNode;
	protected Node endNode;
	protected Map<String, Object> metadata;

	protected AbstractRelation(BaseGraphManager manager, String graphId, Node startNode, Node endNode,
							   Map<String, Object> metadata) {
		this(manager, graphId, startNode, endNode);
		this.metadata = metadata;
	}

	protected AbstractRelation(BaseGraphManager manager, String graphId, Node startNode, Node endNode) {
		super(manager, graphId);
		if (null == manager || StringUtils.isBlank(graphId) || StringUtils.isBlank(startNode.getIdentifier())
				|| StringUtils.isBlank(endNode.getIdentifier())) {
			System.out.println("GraphId: " + graphId + " startNodeId: " + startNode.getIdentifier() + " endNodeId: " +endNode.getIdentifier());
			throw new ClientException(GraphRelationErrorCodes.ERR_INVALID_RELATION.name(), "Invalid Relation");
		}
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public void create(final Request req) {
		try {
			Boolean skipValidation = (Boolean) req.get(GraphDACParams.skip_validations.name());
			Map<String, List<String>> messageMap = null;
			if (null == skipValidation || !skipValidation)
				messageMap = validateRelation(req);
			List<String> errMessages = getErrorMessages(messageMap);
			if (null == errMessages || errMessages.isEmpty()) {
				Request request = new Request(req);
				request.put(GraphDACParams.start_node_id.name(), getStartNode().getIdentifier());
				request.put(GraphDACParams.relation_type.name(), getRelationType());
				request.put(GraphDACParams.end_node_id.name(), getEndNode().getIdentifier());
				request.put(GraphDACParams.metadata.name(), getMetadata());
				Future<Object> response = Futures.successful(graphMgr.addRelation(request));
				manager.returnResponse(response, getParent());
			} else {
				manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
			}
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(),
					"Error occured while creating the Relation", e);
		}
	}

	public String createRelation(final Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.start_node_id.name(), getStartNode().getIdentifier());
		request.put(GraphDACParams.relation_type.name(), getRelationType());
		request.put(GraphDACParams.end_node_id.name(), getEndNode().getIdentifier());
		request.put(GraphDACParams.metadata.name(), getMetadata());

		Response res = graphMgr.addRelation(request);
		if (manager.checkError(res)) {
			return manager.getErrorMessage(res);
		}
		return null;
	}

	@Override
	public void delete(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(graphMgr.deleteRelation(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_DELETE.name(),
					"Error occured while deleting the relation", e);
		}
	}

	@Override
	public String deleteRelation(Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.start_node_id.name(), getStartNode().getIdentifier());
		request.put(GraphDACParams.relation_type.name(), getRelationType());
		request.put(GraphDACParams.end_node_id.name(), getEndNode().getIdentifier());

		Response res = graphMgr.deleteRelation(request);
		if (manager.checkError(res)) {
			return manager.getErrorMessage(res);
		}
		return null;
	}

	@Override
	public void validate(final Request request) {
		try {
			Map<String, List<String>> messageMap = validateRelation(request);

			List<String> errMessages = getErrorMessages(messageMap);
			if (null == errMessages || errMessages.isEmpty()) {
				manager.OK(getParent());
			} else {
				manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
			}
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(),
					"Error Validating the relation", e);
		}
	}

	public Relation toRelation() {
		Relation relation = new Relation(this.startNode.getIdentifier(), getRelationType(), this.endNode.getIdentifier());
		return relation;
	}

	public Node getStartNode() {
		return this.startNode;
	}

	public Node getEndNode() {
		return this.endNode;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	public boolean isType(String relationType) {
		return StringUtils.equalsIgnoreCase(getRelationType(), relationType);
	}

	public void getProperty(Request req) {
		try {
			String key = (String) req.get(GraphDACParams.property_key.name());
			Request request = new Request(req);
			request.put(GraphDACParams.start_node_id.name(), this.startNode.getIdentifier());
			request.put(GraphDACParams.relation_type.name(), getRelationType());
			request.put(GraphDACParams.end_node_id.name(), this.endNode.getIdentifier());
			request.put(GraphDACParams.property_key.name(), key);
			Future<Object> response = Futures.successful(searchMgr.getRelationProperty(request));
					manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(),
					"Error in fetching the relation properties", e);
		}
	}

	public void removeProperty(Request req) {
		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
				"Remove Property is not supported on relations");
	}

	public void setProperty(Request req) {
		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
				"Set Property is not supported on relations");
	}

	protected Map<String, List<String>> getMessageMap(List<String> aggregate) {
		Map<String, List<String>> map = new HashMap<>();
		List<String> messages = new ArrayList<String>();
		if(CollectionUtils.isNotEmpty(aggregate)){
				map.put(getStartNode().getIdentifier(), messages);
		}
		return map;

	}

	protected String checkCycle(Request req) {
		try {
			Request request = new Request(req);
			request.put(GraphDACParams.start_node_id.name(), this.endNode.getIdentifier());
			request.put(GraphDACParams.relation_type.name(), getRelationType());
			request.put(GraphDACParams.end_node_id.name(), this.startNode.getIdentifier());
			Response res = searchMgr.checkCyclicLoop(request);
			if (manager.checkError(res)) {
				return manager.getErrorMessage(res);
			} else {
				Boolean loop = (Boolean) res.get(GraphDACParams.loop.name());
				if (null != loop && loop.booleanValue()) {
					String msg = (String) res.get(GraphDACParams.message.name());
					return msg;
				} else {
					if (StringUtils.equals(startNode.getIdentifier(), endNode.getIdentifier()))
						return "Relation '" + getRelationType() + "' cannot be created between: " + getStartNode().getIdentifier()
								+ " and " + getEndNode().getIdentifier();
					else
						return null;
				}
			}

		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(),
					"Error occured while validing the relation", e);
		}
	}

	protected String getNodeTypeFuture(Node node, List<String> nodeTypes) {
			if (nodeTypes.contains(node.getNodeType())) {
				return null;
			} else {
				return "Node " + node.getIdentifier() + " is not a " + nodeTypes;
			}
		}

	protected String getNodeTypeFuture(Node nodeFuture) {
		if (null != nodeFuture)
			return nodeFuture.getNodeType();
		else
			return null;

	}

	protected String getObjectTypeFuture(Node node) {
		if (null != node)
			return node.getObjectType();
		else
			return null;

	}


	protected String validateObjectTypes(String objectType, final String endNodeObjectType) {

		if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(endNodeObjectType)) {
			// TODO: update after implementing Definition Factory.
			List<String> outRelations = new ArrayList();
//			List<String> outRelations = DefinitionFactory.getOutRelationObjectTypes(graphId, objectType, "1.0");
			boolean found = false;
			if (null != outRelations && !outRelations.isEmpty()) {
				for (String outRel : outRelations) {
					if (StringUtils.equals(getRelationType() + ":" + endNodeObjectType, outRel)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				return (getRelationType() + " is not allowed between " + objectType + " and " + endNodeObjectType);
			}
		}
		return null;

	}

}
