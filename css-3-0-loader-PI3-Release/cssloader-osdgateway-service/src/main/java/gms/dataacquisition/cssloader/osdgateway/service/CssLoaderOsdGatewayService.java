package gms.dataacquisition.cssloader.osdgateway.service;

import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.dataacquisition.cssloader.Endpoints;
import gms.dataacquisition.cssloader.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.cssloader.osdgateway.service.handlers.ExceptionHandlers;
import gms.dataacquisition.cssloader.osdgateway.service.handlers.HttpErrorHandlers;
import gms.dataacquisition.cssloader.osdgateway.service.responsetransformers.JsonTransformer;
import gms.dataacquisition.cssloader.osdgateway.service.handlers.CssLoaderRouteHandlers;

import java.time.format.DateTimeParseException;
import org.apache.commons.lang3.Validate;
import spark.Spark;

/**
 * Service implementation of the OSD gateway for the CSS 3.0. loader.
 */
public final class CssLoaderOsdGatewayService {

  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_MSGPACK = "application/msgpack";

  /**
   * Starts the service with default configuration.
   */
  public static void startService(CssLoaderOsdGatewayInterface osdGateway) {
    startService(osdGateway, Configuration.builder().build());
  }

  /**
   * Starts the service with the provided configuration.
   *
   * @param config the configuration of the service
   */
  public static void startService(CssLoaderOsdGatewayInterface osdGateway,
      Configuration config) {

    Validate.notNull(osdGateway);
    Validate.notNull(config);

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
    Spark.threadPool(config.maxThreads, config.minThreads, config.idleTimeOutMillis);
  }

  /**
   * Registers all routes and filters.
   */
  private static void configureRoutesAndFilters(CssLoaderOsdGatewayInterface gw) {
    // Exception handler.
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
    // POST routes
    registerPostAsJson(Endpoints.STORE_CHANNEL_SEGMENTS,
        voidRoute(CssLoaderRouteHandlers::storeCompressedChannelSegmentBatch, gw));
    registerPostAsJson(Endpoints.STORE_BOOLEAN_SOHS,
        voidRoute(CssLoaderRouteHandlers::storeAcquiredSohBatch, gw));
    registerPostAsJson(Endpoints.STORE_CALIBRATION,
        voidRoute(CssLoaderRouteHandlers::storeCalibration, gw));
    registerPostAsJson(Endpoints.STORE_CHANNEL,
        voidRoute(CssLoaderRouteHandlers::storeChannel, gw));
    registerPostAsJson(Endpoints.STORE_NETWORK,
        voidRoute(CssLoaderRouteHandlers::storeNetwork, gw));
    registerPostAsJson(Endpoints.STORE_RESPONSE,
        voidRoute(CssLoaderRouteHandlers::storeResponse, gw));
    registerPostAsJson(Endpoints.STORE_SENSOR,
        voidRoute(CssLoaderRouteHandlers::storeSensor, gw));
    registerPostAsJson(Endpoints.STORE_SITE,
        voidRoute(CssLoaderRouteHandlers::storeSite, gw));
    registerPostAsJson(Endpoints.STORE_STATION,
        voidRoute(CssLoaderRouteHandlers::storeStation, gw));
    registerPostAsJson(Endpoints.STORE_NETWORK_MEMBERSHIPS,
        voidRoute(CssLoaderRouteHandlers::storeNetworkMemberships, gw));
    registerPostAsJson(Endpoints.STORE_STATION_MEMBERSHIPS,
        voidRoute(CssLoaderRouteHandlers::storeStationMemberships, gw));
    registerPostAsJson(Endpoints.STORE_SITE_MEMBERSHIPS,
        voidRoute(CssLoaderRouteHandlers::storeSiteMemberships, gw));
    // GET routes
    registerGetAsJson(Endpoints.RETRIEVE_CHANNEL_ID,
        (req, res) -> CssLoaderRouteHandlers
            .retrieveChannelIdBySiteNameChannelNameTime(req, res, gw));
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

  /**
   * Registers a route on the given path for GET requests consisting of JSON.
   *
   * @param path the URL path string
   * @param route the route handler to register
   */
  private static void registerGetAsJson(String path, spark.Route route) {
    Spark.get(path, APPLICATION_JSON, route, new JsonTransformer());
  }

  @FunctionalInterface
  private interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c) throws Exception;
  }

  // used to ease creation of route handlers that have no return value under normal operation.
  private static spark.Route voidRoute(
      TriConsumer<spark.Request, spark.Response, CssLoaderOsdGatewayInterface> consumer,
      CssLoaderOsdGatewayInterface osdGateway) {
    return (req, res) -> {
      consumer.accept(req, res, osdGateway);
      return "";
    };
  }

}
