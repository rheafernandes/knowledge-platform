package org.sunbird.content.external;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.CassandraConnector;
import org.sunbird.cassandra.CassandraStore;
import org.sunbird.cassandra.enums.CassandraParams;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentExternalStore extends CassandraStore {

    public ContentExternalStore(String keySpace, List<String> primaryKey) {
        super(keySpace, "content_data", primaryKey);
    }

    @Override
    public void insert(Map<String, Object> request) {
        String identifier = (String) request.get("content_id");
        String body = (String) request.get("body");
        Session session = CassandraConnector.getSession();
        String query = getUpdateQuery("body");
        if (StringUtils.isBlank(query))
            throw new ClientException(CassandraParams.ERR_INVALID_PROPERTY_NAME.name(),
                    "Invalid property name. Please specify a valid property name");
        System.out.println("Query: " + query);
        PreparedStatement ps = session.prepare(query);
        BoundStatement bound = ps.bind(body, identifier);
        try {
            session.execute(bound);
            Map<String, Object> map = new HashMap<String, Object>();
        } catch (Exception e) {
            TelemetryManager.error("Error! Executing update content property:" + e.getMessage(), e);
            throw new ServerException(ErrorCodes.ERR_SYSTEM_EXCEPTION.name(),
                    "Error updating property in Content Store.");
        }
    }

    private String getUpdateQuery(String property) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(property)) {
            sb.append("UPDATE " + getkeySpace() + "." + getTable() + " SET last_updated_on = dateOf(now()), ");
            sb.append(property.trim()).append(" = textAsBlob(?) where content_id = ?");
        }
        return sb.toString();
    }
}
