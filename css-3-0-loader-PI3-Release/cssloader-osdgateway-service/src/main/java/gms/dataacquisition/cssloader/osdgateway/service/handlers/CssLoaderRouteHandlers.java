package gms.dataacquisition.cssloader.osdgateway.service.handlers;

import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.dataacquisition.cssloader.serialization.SerializationUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Contains handler functions for the service routes.
 */
public final class CssLoaderRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(CssLoaderRouteHandlers.class);

  /**
   * Handles a request to store Channel Segment data via an OSD gateway, in compressed format.
   *
   * @param request the request (HTTP)
   * @param response the resopnse (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeCompressedChannelSegmentBatch(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    byte[] rawBytes = request.bodyAsBytes();

    ChannelSegment[] channelSegments = SerializationUtility.msgPackMapper.readValue
        (rawBytes, ChannelSegment[].class);
    Validate.notNull(channelSegments);
    gateway.storeChannelSegments(Set.of(channelSegments));
  }

  /**
   * Handles a request to store AcquiredChannelSohBoolean data via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeAcquiredSohBatch(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store AcquiredChannelSoh[]");
    logger.debug("AcquiredChannelSoh[] JSON: " + json);

    AcquiredChannelSohBoolean[] sohBatch
        = SerializationUtility.objectMapper.readValue(json, AcquiredChannelSohBoolean[].class);
    Validate.notNull(sohBatch);
    gateway.storeChannelStatesOfHealth(new HashSet<>(Arrays.asList(sohBatch)));
  }

  /**
   * Handles a request to retrieve Channel ID  via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   * @return UUID of specific channel
   */
  public static UUID retrieveChannelIdBySiteNameChannelNameTime(
      spark.Request request, spark.Response response, CssLoaderOsdGatewayInterface gateway)
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

    return gateway.idForChannel(siteNameString, channelNameString, time);
  }

  /**
   * Handles a request to store a reference network via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeNetwork(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store ReferenceNetwork");
    logger.debug("ReferenceNetwork JSON: " + json);

    ReferenceNetwork network
        = SerializationUtility.objectMapper.readValue(json, ReferenceNetwork.class);
    Validate.notNull(network);
    gateway.storeNetwork(network);
  }

  /**
   * Handles a request to store a reference station via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeStation(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store ReferenceStation");
    logger.debug("ReferenceStation JSON: " + json);

    ReferenceStation station
        = SerializationUtility.objectMapper.readValue(json, ReferenceStation.class);
    Validate.notNull(station);
    gateway.storeStation(station);
  }

  /**
   * Handles a request to store a reference site via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeSite(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store ReferenceSite");
    logger.debug("ReferenceSite JSON: " + json);

    ReferenceSite site
        = SerializationUtility.objectMapper.readValue(json, ReferenceSite.class);
    Validate.notNull(site);
    gateway.storeSite(site);
  }

  /**
   * Handles a request to store a reference channel via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeChannel(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store ReferenceChannel");
    logger.debug("ReferenceChannel JSON: " + json);

    ReferenceChannel channel
        = SerializationUtility.objectMapper.readValue(json, ReferenceChannel.class);
    Validate.notNull(channel);
    gateway.storeChannel(channel);
  }

  /**
   * Handles a request to store a calibration via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeCalibration(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store calibration");
    logger.debug("ReferenceChannel JSON: " + json);

    ReferenceCalibration calibration
        = SerializationUtility.objectMapper.readValue(json, ReferenceCalibration.class);
    Validate.notNull(calibration);
    gateway.storeCalibration(calibration);
  }

  /**
   * Handles a request to store a response via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeResponse(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store response");
    logger.debug("ReferenceChannel JSON: " + json);

    ReferenceResponse refResponse
        = SerializationUtility.objectMapper.readValue(json, ReferenceResponse.class);
    Validate.notNull(refResponse);
    gateway.storeResponse(refResponse);
  }

  /**
   * Handles a request to store a sensor via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeSensor(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store sensor");
    logger.debug("ReferenceChannel JSON: " + json);

    ReferenceSensor sensor
        = SerializationUtility.objectMapper.readValue(json, ReferenceSensor.class);
    Validate.notNull(sensor);
    gateway.storeSensor(sensor);
  }

  /**
   * Handles a request to store network memberships via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeNetworkMemberships(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store network memberships");
    logger.debug("network memberships JSON: " + json);

    ReferenceNetworkMembership[] memberships
        = SerializationUtility.objectMapper.readValue(json, ReferenceNetworkMembership[].class);
    Validate.notNull(memberships);
    gateway.storeNetworkMemberships(new HashSet<>(Arrays.asList(memberships)));
  }

  /**
   * Handles a request to store station memberships via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeStationMemberships(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store station memberships");
    logger.debug("station memberships JSON: " + json);

    ReferenceStationMembership[] memberships
          = SerializationUtility.objectMapper.readValue(json, ReferenceStationMembership[].class);
    Validate.notNull(memberships);
    gateway.storeStationMemberships(new HashSet<>(Arrays.asList(memberships)));
  }

  /**
   * Handles a request to store site memberships via an OSD gateway.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   * @param gateway the OSD gateway to use
   */
  public static void storeSiteMemberships(spark.Request request,
      spark.Response response, CssLoaderOsdGatewayInterface gateway) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(gateway);

    String json = request.body();
    logger.info("Received request to store site memberships");
    logger.debug("site memberships JSON: " + json);

    ReferenceSiteMembership[] memberships
        = SerializationUtility.objectMapper.readValue(json, ReferenceSiteMembership[].class);
    Validate.notNull(memberships);
    gateway.storeSiteMemberships(new HashSet<>(Arrays.asList(memberships)));
  }
}
