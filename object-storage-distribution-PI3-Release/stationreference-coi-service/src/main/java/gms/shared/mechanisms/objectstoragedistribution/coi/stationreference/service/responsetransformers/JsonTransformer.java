package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.responsetransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceJacksonMixins;
import spark.ResponseTransformer;

/**
 * Transforms spark response objects to JSON.
 */
public class JsonTransformer implements ResponseTransformer {

  /**
   * Serializes and deserializes objects
   */
  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    ReferenceJacksonMixins.register(objectMapper);
    ProvenanceJacksonMixins.register(objectMapper);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public String render(Object model) throws JsonProcessingException {
    return objectMapper.writeValueAsString(model);
  }
}
