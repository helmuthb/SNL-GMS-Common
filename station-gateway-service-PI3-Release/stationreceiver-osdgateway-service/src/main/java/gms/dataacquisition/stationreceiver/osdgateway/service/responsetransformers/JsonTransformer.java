package gms.dataacquisition.stationreceiver.osdgateway.service.responsetransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import spark.ResponseTransformer;

/**
 * Transforms spark response objects to JSON.
 */
public class JsonTransformer implements ResponseTransformer {

  @Override
  public String render(Object model) {
    try {
      return SerializationUtility.objectMapper.writeValueAsString(model);
    } catch (JsonProcessingException ex) {
      return null;
    }
  }
}
