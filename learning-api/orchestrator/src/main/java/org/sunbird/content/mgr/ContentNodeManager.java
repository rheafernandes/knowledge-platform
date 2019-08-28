package org.sunbird.content.mgr;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.content.CommonContentNode;
import org.sunbird.content.ECMLContentNode;
import org.sunbird.content.IContentNode;
import org.sunbird.content.PluginContentNode;

import java.util.Map;

public class ContentNodeManager {

    public static IContentNode getContentNode(Map<String, Object> data) {
        String mimeType = (String) data.get("mimeType");
        if (StringUtils.isBlank(mimeType))
            throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "mimeType not found in content request.");
        IContentNode node;
        switch (mimeType) {
            case "application/vnd.ekstep.ecml-archive":
                node = new ECMLContentNode(data);
                break;
            case "application/vnd.ekstep.plugin-archive":
                node = new PluginContentNode(data);
                break;
            default:
                node = new CommonContentNode(data);
                break;
        }
        return node;
    }
}
