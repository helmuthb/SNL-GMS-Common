package gms.dataacquisition.stationreceiver.osdgateway.service.handlers;

import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains handler functions for station SOH routes. Created by jwvicke on 9/6/17.
 */
public final class RouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(RouteHandlers.class);

  /**
   * Handles a request to store a batch of acquired State-Of-Health (analog status) via an OSD
   * gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return An empty string for success or an error message.
   */
  public static String storeStationSohAnalogBatch(spark.Request request,
      spark.Response response, StationReceiverOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String sohBatchJson = request.body();
    logger.debug("Store Station SOH Analog batch, json: " + sohBatchJson);
    AcquiredChannelSohAnalog[] sohs = SerializationUtility.objectMapper.readValue(
        sohBatchJson, AcquiredChannelSohAnalog[].class);
    Validate.notNull(sohs);
    gateway.storeAnalogChannelStatesOfHealth(Set.of(sohs));
    return "";
  }

  /**
   * Handles a request to store a batch of acquired State-Of-Health (boolean status) via an OSD
   * gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return An empty string for success or an error message.
   */
  public static String storeStationSohBooleanBatch(spark.Request request,
      spark.Response response, StationReceiverOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);
    String sohBatchJson = request.body();
    logger.debug("Store Station SOH Boolean batch, json: " + sohBatchJson);

    AcquiredChannelSohBoolean[] sohs = SerializationUtility.objectMapper.readValue(
          sohBatchJson, AcquiredChannelSohBoolean[].class);
    Validate.notNull(sohs);
    gateway.storeBooleanChannelStatesOfHealth(Set.of(sohs));
    return "";
  }

  /**
   * Handles a request to store a batch of acquired Channel Segments (boolean status) via an OSD
   * gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return true for success and false for failure
   */
  public static String storeChannelSegments(spark.Request request,
      spark.Response response, StationReceiverOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);
    byte[] body = request.bodyAsBytes();
    logger.debug("storeChannelSegments (msgpack)");
    ChannelSegment[] segments = SerializationUtility.msgPackMapper.readValue(body, ChannelSegment[].class);
    Validate.notNull(segments);
    gateway.storeChannelSegments(Set.of(segments));
    return "";
  }

  /**
   * Handles a request to store a RawStationDataFrame (boolean status) via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return true for success and false for failure
   */
  public static String storeRawStationDataFrame(spark.Request request,
      spark.Response response, StationReceiverOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String frameJson = request.body();
    logger.debug("storeRawStationDataFrame, json: " + frameJson);
    RawStationDataFrame frame = SerializationUtility.objectMapper.readValue(frameJson,
          RawStationDataFrame.class);
    Validate.notNull(frame);
    gateway.storeRawStationDataFrame(frame);
    return "";  // empty body means 'no error'
  }

  /**
   * Handles a request to retrieve Station ID via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return UUID of specific station, or null if none exists.
   */
  public static Optional<UUID> getStationId(spark.Request request,
      spark.Response response, StationReceiverOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String stationName = request.queryParams("station-name");
    logger.debug("getStation: parameter station-name = " + stationName);
    Validate.notEmpty(stationName);
    return gateway.getStationId(stationName);
  }

  /**
   * Handles a request to retrieve Channel ID via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return UUID of specific channel, or null if none exists.
   */
  public static Optional<UUID> getChannelId(
      spark.Request request, spark.Response response, StationReceiverOsdGatewayInterface gateway)
      throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String siteNameString = request.queryParams("site-name");
    String channelNameString = request.queryParams("channel-name");
    Instant time = Instant.parse(request.queryParams("time"));

    logger.info("Received request to retrieve Channel ID");
    logger.debug("Query parameters (siteName, channelName, time): " + siteNameString +
        ", " + channelNameString + ", " + time);

    return gateway.getChannelId(siteNameString, channelNameString, time);
  }
}
