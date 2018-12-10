package gms.core.signalenhancement.waveformfiltering.osdgateway.gateway;

import gms.core.signalenhancement.waveformfiltering.objects.dto.InvokeInputDataRequestDto;
import gms.core.signalenhancement.waveformfiltering.objects.dto.StoreChannelSegmentsDto;
import gms.core.signalenhancement.waveformfiltering.osdgateway.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayHandler {

  private static Logger logger = LoggerFactory.getLogger(GatewayHandler.class);

  private OsdGateway osdGateway;

  private GatewayHandler(OsdGateway osdGateway) {
    this.osdGateway = osdGateway;
  }

  /**
   * Obtains a new {@link GatewayHandler} that delegates calls to the {@link OsdGateway}
   *
   * @param osdGateway an {@link OsdGateway}, not null
   * @return {@link GatewayHandler}, not null
   */
  public static GatewayHandler create(OsdGateway osdGateway) {
    Objects.requireNonNull(osdGateway, "GatewayHandler requires a non-null OsdGateway");

    return new GatewayHandler(osdGateway);
  }

  public StandardResponse fetchInvokeInputData(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    InvokeInputDataRequestDto dataRequestDto = getDeserializer(requestBodyType,
        InvokeInputDataRequestDto.class).apply(body);

    logger.info("load invoke request received: {}", dataRequestDto);

    Collection<ChannelSegment> result = osdGateway
        .loadInvokeInputData(dataRequestDto.getChannelIds(),
            dataRequestDto.getStartTime(), dataRequestDto.getEndTime());

    logger.info("Load invoke input result: {} ", result);
    result.stream().forEach(cs -> logger
        .info("Loaded ChannelSegment {} has {} waveforms", cs.getId(), cs.getWaveforms().size()));

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType).apply(result),
        responseBodyType);
  }

  /**
   * Channel Segment storage request. Accepts a JSON body representing the object to store (a {@link
   * Collection}). <p> Delegates to the {@link OsdGateway} to perform the store.
   */
  public StandardResponse storeChannelSegments(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    logger.info("Store request received, Content-Type: {}, body length: {}, Accept: {}",
        requestBodyType, body.length, responseBodyType);

    StoreChannelSegmentsDto dto = getDeserializer(requestBodyType, StoreChannelSegmentsDto.class)
        .apply(body);

    logger.info("Deserialized into StoreChannelSegmentsDto: {}", dto);

    osdGateway.store(dto.getChannelSegments(), dto.getStorageVisibility());

    logger.info("Stored ChannelSegments");

    return StandardResponse.create(HttpStatus.OK_200, "", responseBodyType);
  }

  /**
   * Obtains the deserialization operation to invoke to deserialize a byte[] in format {@link
   * ContentType} into a T
   *
   * @param type ContentType, not null
   * @param <T> type of the deserialized object
   * @return BiFunction to map a (byte[], Class) into a T
   */
  private static <T> BiFunction<byte[], Class<T>, T> getDeserializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type) {
      return ObjectSerialization::readJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::readMessagePack;
    }

    throw new IllegalStateException(
        "FilterControlRouteHandler cannot instantiate a deserializer for an unsupported ContentType.");
  }

  /**
   * Obtains the serialization operation to invoke to serialize an object into a byte[] in format
   * {@link ContentType}
   *
   * @param type ContentType, not null
   * @return Function to serialize an Object into a byte[]
   */
  private static Function<Object, byte[]> getSerializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type) {
      return ObjectSerialization::writeJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::writeMessagePack;
    }

    throw new IllegalStateException(
        "FilterControlRouteHandler cannot instantiate a serializer for an unsupported ContentType.");
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a T. Assumes the
   * content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @param classType Class object for type T
   * @param <T> type of the deserialized object
   * @return Function to map a byte[] into a T
   */
  private static <T> Function<byte[], T> getDeserializer(ContentType type, Class<T> classType) {
    // There is a type error when all of this is on one line.  Doubtless it involves type erasure.
    BiFunction<byte[], Class<T>, T> deserialization = getDeserializationOp(type);
    return b -> deserialization.apply(b, classType);
  }
}
