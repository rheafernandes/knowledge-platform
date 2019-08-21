package org.sunbird.schema;

import org.leadpony.justify.api.JsonSchema;
import org.sunbird.schema.dto.Result;

import java.io.StringReader;
import java.net.URI;
import java.util.List;

public interface ISchema {

    JsonSchema resolveSchema(URI id);

    Result validate(String data);

    List<String> validate(StringReader input);

    String withDefaultValues(String data);
}
