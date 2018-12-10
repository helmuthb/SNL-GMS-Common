package gms.dataacquisition.stationreceiver.osdgateway.service;

import gms.dataacquisition.stationreceiver.osdgateway.Endpoints;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.dataacquisition.stationreceiver.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.stationreceiver.osdgateway.service.handlers.RouteHandlers;
import gms.dataacquisition.stationreceiver.osdgateway.service.responsetransformers.JsonTransformer;
import org.apache.commons.lang3.Validate;
import spark.Spark;

/**
 * Service implementation of the OSD gateway service for the station data receiver. This class is a
 * singleton; no public constructor, and only create a single instance will be created internally.
 */
public final class StationReceiverOsdGatewayService {

  private static final String
      APPLICATION_JSON = "application/json",
      APPLICATION_MSGPACK = "application/msgpack";

  /**
   * Starts the service with default configuration.
   */
  public static void startService(StationReceiverOsdGatewayInterface osdGateway) {
    startService(osdGateway, Configuration.builder().build());
  }

  /**
   * Starts the service with the provided configuration.
   */
  public static void startService(StationReceiverOsdGatewayInterface osdGateway,
      Configuration config) {

    Validate.notNull(osdGateway, "OSD gateway cannot be null");
    Validate.notNull(config, "Configuration object cannot be null.");

    // Configure and run the Spark server.
    configureHttpServer(config);
    configureRoutesAndFilters(osdGateway);
    Spark.awaitInitialization();
  }

  /**
   * Stops the REST service.
   */
  public static void stopService() {
    Spark.stop();
  }

  /**
   * Configures the HTTP server before the it is started. NOTE: This method must be called before
   * routes and filters are declared, because Spark Java automatically starts the HTTP server when
   * routes and filters are declared.
   */
  private static void configureHttpServer(Configuration config) {
    // Set the listening port.
    Spark.port(config.port);
    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(
        config.maxThreads,
        config.minThreads,
        config.idleTimeOutMillis);
  }

  /**
   * Registers all routes and filters.
   */
  private static void configureRoutesAndFilters(StationReceiverOsdGatewayInterface gw) {
    // Exception handlers.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Filters.
    // All requests normally return JSON content-type.
    Spark.before((request, response) -> response.type(APPLICATION_JSON));
    // 'compressed' endpoints should return msgpack content-type.
    Spark.before("*/compressed/*", (request, response) -> response.type(APPLICATION_MSGPACK));
    // This is to allow cross-origin requests.
    Spark.after((request, response) -> response.header("access-control-allow-origin", "*"));
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GET routes
    registerGetAsJson(Endpoints.GET_STATION_ID_BY_NAME,
        (req, res) -> RouteHandlers.getStationId(req, res, gw));
    registerGetAsJson(Endpoints.GET_CHANNEL_ID_BY_NAME_AND_SITE,
        (req, res) -> RouteHandlers.getChannelId(req, res, gw));
    // POST routes
    registerPostAsJson(Endpoints.STORE_ANALOG_SOHS,
        (req, res) -> RouteHandlers.storeStationSohAnalogBatch(req, res, gw));
    registerPostAsJson(Endpoints.STORE_BOOLEAN_SOHS,
        (req, res) -> RouteHandlers.storeStationSohBooleanBatch(req, res, gw));
    registerPostAsJson(Endpoints.STORE_RAW_STATION_DATA_FRAME,
        (req, res) -> RouteHandlers.storeRawStationDataFrame(req, res, gw));
    registerPostAsJson(Endpoints.STORE_CHANNEL_SEGMENTS,
        (req, res) -> RouteHandlers.storeChannelSegments(req, res, gw));
  }

  /**
   * Registers a route on the given path for POST requests consisting of JSON.
   *
   * @param path the URL path string
   * @param route the route handler to register
   */
  private static void registerPostAsJson(String path, spark.Route route) {
    Spark.post(path, APPLICATION_JSON, route, new JsonTransformer());
  }

  private static void registerGetAsJson(String path, spark.Route route) {
    Spark.get(path, APPLICATION_JSON, route, new JsonTransformer());
  }

}
