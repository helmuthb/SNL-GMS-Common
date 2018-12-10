package gms.core.signalenhancement.waveformfiltering.osdgateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingControlJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import java.io.IOException;
import java.util.Objects;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class ObjectSerialization {

  /**
   * Serializes and deserializes JSON
   */
  private static final ObjectMapper jsonMapper;

  /**
   * Serializes and deserializes MessagePack
   */
  private static final ObjectMapper messagePackMapper;

  static {
    jsonMapper = configureObjectMapper(new ObjectMapper());
    messagePackMapper = configureObjectMapper(new ObjectMapper(new MessagePackFactory()));
  }

  /**
   * Configures the {@link ObjectMapper} with the required modules and mixins
   *
   * @param objectMapper ObjectMapper to configure, not null
   * @return the input ObjectMapper
   */
  private static ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
    objectMapper.findAndRegisterModules();
    WaveformsJacksonMixins.register(objectMapper);
    SignalDetectionJacksonMixins.register(objectMapper);
    ProcessingControlJacksonMixins.register(objectMapper);

    return objectMapper;
  }

  /**
   * Serialize the provided object to JSON.
   *
   * @param object object to serialize to JSON, not null
   * @return String containing JSON serialization of the input object
   * @throws IllegalArgumentException if there is a serialization error
   * @throws NullPointerException if object is null
   */
  public static byte[] writeJson(Object object) {
    Objects.requireNonNull(object, "Unable to serialize null to json");

    try {
      return jsonMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object to json", e);
    }
  }

  /**
   * Deserializes the provided json into an instance of outputType
   *
   * @param json json in a byte[], not null
   * @param outputType deserialized object type, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if json or outputType are null
   * @throws IllegalArgumentException if the json can't be deserialized into a T
   */
  public static <T> T readJson(byte[] json, Class<T> outputType) {
    Objects.requireNonNull(json, "Unable to deserialize null json");
    Objects.requireNonNull(outputType, "Unable to deserialize to null class type");

    try {
      return jsonMapper.readValue(json, outputType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object from json", e);
    }
  }

  /**
   * Serialize the provided object to MessagePack.
   *
   * @param object object to serialize to MessagePack, not null
   * @return String containing JSON serialization of the input object
   * @throws IllegalArgumentException if there is a serialization error
   * @throws NullPointerException if object is null
   */
  public static byte[] writeMessagePack(Object object) {
    Objects.requireNonNull(object, "Unable to serialize null to MessagePack");

    try {
      return messagePackMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object to MessagePack", e);
    }
  }

  /**
   * Deserializes the provided MessagePack bytes into an instance of outputType
   *
   * @param messagePack byte array containing MessagePack, not null
   * @param outputType deserialized object type, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if messagePack or outputType are null
   * @throws IllegalArgumentException if the messagePack can't be deserialized into a T
   */
  public static <T> T readMessagePack(byte[] messagePack, Class<T> outputType) {
    Objects.requireNonNull(messagePack, "Unable to deserialize null MessagePack");
    Objects.requireNonNull(outputType, "Unable to deserialize to null class type");

    try {
      return messagePackMapper.readValue(messagePack, outputType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }
}
