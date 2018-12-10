package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingControlJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import java.io.IOException;

/**
 * Utility class for serializing and deserializing objects using Jackson
 */
public class ObjectSerialization {

  private static ObjectMapper objectMapper = objectMapper();


  public static <T> T readValue(String string, Class<T> type) {
    try {
      return objectMapper.readValue(string, type);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object string", e);
    }
  }

  public static String writeValue(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object, invalid object type", e);
    }
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.findAndRegisterModules();

    SignalDetectionJacksonMixins.register(objectMapper);
    ProvenanceJacksonMixins.register(objectMapper);
    ProcessingControlJacksonMixins.register(objectMapper);

    return objectMapper;
  }

}
