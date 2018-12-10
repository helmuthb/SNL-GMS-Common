package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.eclipse.jetty.http.HttpStatus;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

/**
 * Contains handler functions for the service routes.
 */
public class WaveformRepositoryHttpRouteHandlers {

  private static Logger logger = LoggerFactory
      .getLogger(WaveformRepositoryHttpRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper;
  private static final ObjectMapper messagePackMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    messagePackMapper = new ObjectMapper(new MessagePackFactory());
    messagePackMapper.findAndRegisterModules();

    WaveformsJacksonMixins.register(objectMapper);
    WaveformsJacksonMixins.register(messagePackMapper);
  }


  /**
   * Handles a request to retrieve {@link ChannelSegment} via {@link WaveformRepositoryInterface}.
   *
   * Returns ad body with a single ChannelSegment, either JSON or msgpack as per client request header.
   *
   * Returns HTTP status codes: 200 when query finds an ChannelSegment by id, startTime, endTime 400
   * when the parameters is not valid 400 when no ChannelSegment with the provided identity exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param waveformRepositoryInterface the interface used
   * @return return HTTP response with body containing a single AcquiredChannelSohAnalog
   */
  public static Object retrieveChannelSegment(spark.Request request,
      spark.Response response,
      WaveformRepositoryInterface waveformRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepositoryInterface);

    String processingChannelId = request.queryParams("channel-id");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");
    String withWaveforms = request.queryParams("with-waveforms");

    logger.info(
        "retrieveChannelSegment endpoint hit with parameters: " + "channel-id = "
            + processingChannelId
            + " start-time = " + startTimeString + "end-time = " + endTimeString
            + " with-waveforms = " + withWaveforms);

    UUID processingChannelID = UUID.fromString(processingChannelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    Boolean waveforms = Boolean.parseBoolean(withWaveforms);

    Optional<ChannelSegment> cs = waveformRepositoryInterface
        .retrieveChannelSegment(processingChannelID, startTime, endTime, waveforms);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(cs);
    }
    else {
      return objectMapper.writeValueAsString(cs);
    }
  }

  /**
   * Retrieves an {@link AcquiredChannelSohAnalog} by identity.
   *
   * Returns a JSON body with a single AcquiredChannelSohAnalog.
   *
   * Returns HTTP status codes: 200 when query finds an AcquiredChannelSohAnalog by id 400 when the
   * {id} parameter is not a UUID 404 when no AcquiredChannelSohAnalog with the provided identity
   * exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used query param acquiredChannelSohId query
   * parameter AcquiredChannelSohAnalog identifier, not null
   * @return return HTTP response with body containing a single AcquiredChannelSohAnalog
   */
  public static String getAcquiredChannelSohAnalog(
      spark.Request request,
      spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String processingChannelId = request.params(":id");

    logger.info("getAcquiredChannelSohAnalog endpoint hit with id: " + processingChannelId);

    UUID acquiredChannelSohId = UUID.fromString(processingChannelId);
    Optional<AcquiredChannelSohAnalog> result = stationSohRepositoryInterface
        .retrieveAcquiredChannelSohAnalogById(acquiredChannelSohId);

    if (!result.isPresent()) {
      return HttpErrorHandlers
          .Http404Custom(request, response, "No result found for ChannelSohAnalog ID provided");
    }
    return objectMapper.writeValueAsString(result);
  }

  /**
   * Retrieves an {@link AcquiredChannelSohBoolean} by identity.
   *
   * Returns a JSON body with a single AcquiredChannelSohBoolean.
   *
   * Returns HTTP status codes: 200 when query finds an AcquiredChannelSohBoolean by id 400 when the
   * {id} parameter is not a UUID 404 when no AcquiredChannelSohBoolean with the provided identity
   * exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * AcquiredChannelSohBoolean identifier, not null
   * @return HTTP response with body containing a single AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohBoolean(
      spark.Request request,
      spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String processingChannelId = request.params(":id");

    logger.info("getAcquiredChannelSohAnalog endpoint hit with id: " + processingChannelId);

    UUID acquiredChannelSohId = UUID.fromString(processingChannelId);
    Optional<AcquiredChannelSohBoolean> result = stationSohRepositoryInterface
        .retrieveAcquiredChannelSohBooleanById(acquiredChannelSohId);

    if (!result.isPresent()) {
      return HttpErrorHandlers
          .Http404Custom(request, response, "No result found for ChannelSohBoolean ID provided");
    }

    return objectMapper.writeValueAsString(result);
  }

  /**
   * Obtains the {@link AcquiredChannelSohBoolean} objects stored for the provided ProcessingChannel
   * identity between the start and end times.
   *
   * Returns a JSON body with a list of AcquiredChannelSohBoolean.  The list is empty when the query
   * succeeds without results.
   *
   * Returns HTTP status codes: 200 when query is successful, even if there are no results 400 if
   * any required query parameters are missing
   *
   * Query Parameters:
   * channelId ProcessingChannel identifier, not null
   * startTime query start time, not null
   * endTime query end time, not null
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * @return HTTP response with body containing a list of AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohBooleanTimeRange(
      spark.Request request,
      spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String processingChannelId = request.queryParams("channel-id");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");

    logger.info(
        "getAcquiredChannelSohBooleanTimeRange endpoint hit with parameters: " + "channel-id = "
            + processingChannelId
            + " start-time = " + startTimeString + "end-time = " + endTimeString);

    UUID processingChannelID = UUID.fromString(processingChannelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    return objectMapper.writeValueAsString(stationSohRepositoryInterface
        .retrieveBooleanSohByProcessingChannelAndTimeRange(processingChannelID, startTime,
            endTime));
  }

  /**
   * Obtains the {@link AcquiredChannelSohBoolean} objects stored for the provided ProcessingChannel
   * identity between the start and end times.
   *
   * Returns a JSON body with a list of AcquiredChannelSohBoolean.  The list is empty when the query
   * succeeds without results.
   *
   * Returns HTTP status codes: 200 when query is successful, even if there are no results 400 if
   * any required query parameters are missing
   *
   * Query Parameters:
   * channelId ProcessingChannel identifier, not null
   * startTime query start time, not null
   * endTime query end time, not null
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * @return HTTP response with body containing a list of AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohAnalogTimeRange(
      spark.Request request,
      spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String processingChannelId = request.queryParams("channel-id");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");

    logger.info(
        "getAcquiredChannelSohAnalogTimeRange endpoint hit with parameters: " + "channel-id = "
            + processingChannelId
            + " start-time = " + startTimeString + "end-time = " + endTimeString);

    UUID processingChannelID = UUID.fromString(processingChannelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    return objectMapper.writeValueAsString(stationSohRepositoryInterface
        .retrieveAnalogSohByProcessingChannelAndTimeRange(processingChannelID, startTime,
            endTime));
  }

  /**
   * Retrieves a list of raw station data frames, which may be empty.
   *
   * Returns HTTP status codes: 200 when query is successful, even if there are no results;
   * 400 if any required query parameters are missing.
   *
   * Query Parameters:
   * start-time, not null, IS0-8601 format, required
   * end-time, not null, IS0-8601 format, required
   * station-name, optional
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param frameRepository the interface used to retrieve raw station data frames
   * @return HTTP response with body containing a list of RawStationDataFrame
   */
  public static Object getRawStationDataFrames(
      spark.Request request,
      spark.Response response,
      RawStationDataFrameRepositoryInterface frameRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(frameRepository);

    String stationName = request.queryParams("station-name");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");

    logger.info(
        "getRawStationDataFrames endpoint hit with parameters: " + "station-name = "
            + stationName + " start-time = " + startTimeString
            + "end-time = " + endTimeString);

    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    List<RawStationDataFrame> frames = stationName != null ?
        frameRepository.retrieveByStationName(stationName, startTime, endTime)
        : frameRepository.retrieveAll(startTime, endTime);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(frames);
    }
    else {
      return objectMapper.writeValueAsString(frames);
    }
  }

  /**
   * State of health operation to determine if the waveforms-repository-service is running.  Returns
   * a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public static String alive(
      spark.Request request,
      spark.Response response) {

    response.status(HttpStatus.OK_200);
    return "alive at " + Instant.now()
        .toString();
  }

  /**
   * Determines if the {@link Request} indicates the client accepts message pack
   *
   * @param request Request, not null
   * @return true if the client accepts application/msgpack
   */
  private static boolean shouldReturnMessagePack(Request request) {
    String accept = request.headers("Accept");
    return accept != null && accept.contains("application/msgpack");
  }
}

