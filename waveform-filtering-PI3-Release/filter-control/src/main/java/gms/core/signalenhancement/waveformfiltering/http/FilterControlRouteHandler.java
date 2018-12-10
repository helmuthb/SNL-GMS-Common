package gms.core.signalenhancement.waveformfiltering.http;

import gms.core.signalenhancement.waveformfiltering.control.ExecuteClaimCheckCommand;
import gms.core.signalenhancement.waveformfiltering.control.ExecuteStreamingCommand;
import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP invocation to {@link FilterControl}
 */
public class FilterControlRouteHandler {

  private Logger logger = LoggerFactory.getLogger(FilterControlRouteHandler.class);

  private final FilterControl filterControl;

  private FilterControlRouteHandler(FilterControl filterControl) {
    this.filterControl = filterControl;
  }

  /**
   * Obtains a new {@link FilterControlRouteHandler} that delegates calls to the {@link
   * FilterControl}
   *
   * @param filterControl FilterControl, not null
   * @return constructed {@link FilterControlRouteHandler}, not null
   */
  public static FilterControlRouteHandler create(FilterControl filterControl) {
    Objects.requireNonNull(filterControl,
        "FilterControlRouteHandler cannot be constructed with null FilterControl");
    return new FilterControlRouteHandler(filterControl);
  }

  /**
   * Route handler for streaming invocation of {@link gms.core.signalenhancement.waveformfiltering.control.FilterControl}
   * Body must be a serialized {@link StreamingDto}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse streaming(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "FilterControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "FilterControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "FilterControlRouteHandler requires non-null responseBodyType");

    logger
        .info("Invoked Filter Control / Streaming with Content-Type: {}, Accept: {}, and body length: {}",
            requestBodyType, requestBodyType, body.length);

    if (!isAcceptableStreamingType(requestBodyType)) {
      final String message =
          "FilterControl streaming invocation cannot accept inputs in format " + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableStreamingType(responseBodyType)) {
      final String message =
          "FilterControl streaming invocation cannot provide outputs in format " + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke FilterControl and construct a StandardResponse from the results
    ExecuteStreamingCommand streamingCommand = executeCommandFrom(
        getStreamingDtoDeserializer(requestBodyType).apply(body));

    logger.info("Created ExecuteStreamingCommand: {}", streamingCommand);

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType).apply(filterControl.execute(streamingCommand)),
        responseBodyType);
  }

  /**
   * Obtains an {@link ExecuteStreamingCommand} from a {@link ClaimCheckDto}
   *
   * @param streamingDto ClaimCheckDto, not null
   * @return ExecuteStreamingCommand, not null
   */
  private ExecuteStreamingCommand executeCommandFrom(StreamingDto streamingDto) {
    return ExecuteStreamingCommand
        .create(streamingDto.getChannelSegmentToOutputChannelIdMap(),
            streamingDto.getFilterDefinition(),
            streamingDto.getProcessingContext());
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the streaming
   * filter service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the streaming filter service
   */
  private static boolean isAcceptableStreamingType(ContentType contentType) {
    return ContentType.APPLICATION_JSON == contentType
        || ContentType.APPLICATION_MSGPACK == contentType;
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link StreamingDto}.
   * Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a StreamingDto
   */
  private static Function<byte[], StreamingDto> getStreamingDtoDeserializer(ContentType type) {
    return getDeserializer(type, StreamingDto.class);
  }

  /**
   * Route handler for claim check invocation of {@link gms.core.signalenhancement.waveformfiltering.control.FilterControl}
   * Body must be a serialized {@link ClaimCheckDto}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse claimCheck(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "FilterControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "FilterControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "FilterControlRouteHandler requires non-null responseBodyType");

    logger.info(
        "Invoked Filter Control / Claim Check with Content-Type: {}, Accept: {}, and body length: {}",
        requestBodyType, requestBodyType, body.length);

    if (!isAcceptableClaimCheckType(requestBodyType)) {
      final String message =
          "FilterControl claim check invocation cannot accept inputs in format " + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableClaimCheckType(responseBodyType)) {
      final String message =
          "FilterControl claim check invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke FilterControl and construct a StandardResponse from the results
    final ExecuteClaimCheckCommand claimCheckCommand = executeCommandFrom(
        getClaimCheckDtoDeserializer(requestBodyType).apply(body));

    logger.info("Created ExecuteClaimCheckCommand: {}", claimCheckCommand);

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType).apply(filterControl.execute(claimCheckCommand)),
        responseBodyType);
  }

  /**
   * Obtains an {@link ExecuteClaimCheckCommand} from a {@link ClaimCheckDto}
   *
   * @param claimCheckDto ClaimCheckDto, not null
   * @return ExecuteClaimCheckCommand, not null
   */
  private static ExecuteClaimCheckCommand executeCommandFrom(ClaimCheckDto claimCheckDto) {
    return ExecuteClaimCheckCommand
        .create(claimCheckDto.getInputToOutputChannelIds(),
            claimCheckDto.getChannelProcessingStepId(),
            claimCheckDto.getStartTime(),
            claimCheckDto.getEndTime(),
            claimCheckDto.getProcessingContext());
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the claim check
   * filter service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the claim check filter service
   */
  private static boolean isAcceptableClaimCheckType(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON;
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link
   * ClaimCheckDto}. Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a ClaimCheckDto
   */
  private static Function<byte[], ClaimCheckDto> getClaimCheckDtoDeserializer(ContentType type) {
    return getDeserializer(type, ClaimCheckDto.class);
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
}
