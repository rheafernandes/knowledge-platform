package org.sunbird.external.store;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.CassandraConnector;
import org.sunbird.cassandra.CassandraStore;
import org.sunbird.common.exception.ErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeExternalStore extends CassandraStore {

    public NodeExternalStore(String keySpace, String table, List<String> primaryKey) {
        super(keySpace, table, primaryKey);
    }

    @Override
    public void insert(Map<String, Object> request) {
        Map<String, Object> keyMap = primaryKey.stream().filter(request::containsKey).collect(Collectors.toMap(key -> key, request::remove, (a, b) -> b));
        Insert insertQuery = QueryBuilder.insertInto(keySpace, table);
        keyMap.forEach((key, value) -> insertQuery.value(key, value));
        request.remove("last_updated_on");
        insertQuery.value("last_updated_on",new Timestamp(new Date().getTime()));
        request.forEach((key, value) -> insertQuery.value(key, "textAsBlob(" + value + ")"));
        try {
            Session session = CassandraConnector.getSession();
            System.out.println("Query : " + insertQuery.toString() );
            session.execute(insertQuery);
        }catch(Exception e) {
            TelemetryManager.error("Exception Occurred While Saving The Record. | Exception is : " + e.getMessage(), e);
            throw new ServerException(ErrorCodes.ERR_SYSTEM_EXCEPTION.name(),
                    "Exception Occurred While Saving The Record. Exception is : "+e.getMessage());
        }

    }

    //TODO: Remove it later.
    private String getUpdateQuery(String property) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(property)) {
            sb.append("UPDATE " + getkeySpace() + "." + getTable() + " SET last_updated_on = dateOf(now()), ");
            sb.append(property.trim()).append(" = textAsBlob(?) where identifier = ?");
        }
        return sb.toString();
    }
}
