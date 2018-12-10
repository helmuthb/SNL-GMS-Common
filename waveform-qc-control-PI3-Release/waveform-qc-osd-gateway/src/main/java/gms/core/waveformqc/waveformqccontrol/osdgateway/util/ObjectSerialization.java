package gms.core.waveformqc.waveformqccontrol.osdgateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.dto.InvokeInputDataDto;
import gms.core.waveformqc.waveformqccontrol.objects.dto.WaveformQcChannelSohStatusJacksonUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingControlJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import java.io.IOException;

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

  public static com.mashape.unirest.http.ObjectMapper getClientObjectMapper() {
    return new com.mashape.unirest.http.ObjectMapper() {
      @Override
      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return objectMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public String writeValue(Object value) {
        try {
          return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.findAndRegisterModules();

    WaveformsJacksonMixins.register(objectMapper);
    SignalDetectionJacksonMixins.register(objectMapper);
    ProvenanceJacksonMixins.register(objectMapper);
    ProcessingControlJacksonMixins.register(objectMapper);

    objectMapper.registerModule(WaveformQcChannelSohStatusJacksonUtility.getModule());
    objectMapper.addMixIn(InvokeInputData.class, InvokeInputDataDto.class);

    return objectMapper;
  }

}
