package gms.dataacquisition.cssloader.osdgateway.service.responsetransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.dataacquisition.cssloader.serialization.SerializationUtility;
import spark.ResponseTransformer;

/**
 * Transforms spark response objects to JSON.
 */
public class JsonTransformer implements ResponseTransformer {

  @Override
  public String render(Object model) throws JsonProcessingException {
    return SerializationUtility.objectMapper.writeValueAsString(model);
  }
}
