package org.sunbird.cache.mgr;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.cache.common.CacheHandlerOperation;
import org.sunbird.cache.handler.ICacheHandler;
import org.sunbird.cache.util.RedisCacheUtil;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * Base Class for Redis Based Cache Implementation
 *
 * @author Kumar Gauraw
 */
public abstract class RedisCacheManager implements ICacheManager {

    ICacheHandler handler;

    protected String getStringData(String key, String identifier) {
        try {
            String data = RedisCacheUtil.getString(key);
            if (StringUtils.isBlank(data) && null != handler) {
                data = (String) handler.execute(CacheHandlerOperation.READ_STRING.name(), identifier);
            }
            return data;
        } catch (Exception e) {
            TelemetryManager.error("Exception Occurred While  Fetching Data For Key : " + key + " | Exception is : ", e);
            if (e instanceof ResourceNotFoundException)
                throw e;
        }
        return null;
    }

    @Override
    public Object getObject(String key) {
        return null;
    }

    @Override
    public void setObject(String key, Object data) {

    }

    @Override
    public void publish(String channel, String message) {

    }

    @Override
    public void subscribe(String... channels) {

    }
}
