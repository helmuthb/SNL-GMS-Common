package gms.core.signaldetection.signaldetectorcontrol.osdgateway;

import gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration.HttpServiceConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration.HttpServiceConfigurationLoader;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration.OsdGatewayConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration.OsdGatewayConfigurationLoader;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway.ContentType;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway.ErrorHandler;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway.GatewayHandler;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway.OsdGateway;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway.StandardResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.SignalDetectionEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.SignalDetectionRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.WaveformsEntityManagerFactories;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  private static final String invokeInputDataPath = "/invoke-input-data";

  private static final String storePath = "/store";

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  private Application() {
  }

  public static void start() {
    logger.info("Initializing signal-detector-control service...");
    final String propertiesFile = "gms/core/signaldetection/signaldetectorcontrol/osdgateway/util/application.properties";
    HttpServiceConfiguration serviceConfig = HttpServiceConfigurationLoader
        .load(getUrlToResourceFile(propertiesFile));

    logger.info("Initializing embedded server...");
    configureServer(serviceConfig);
    logger.info("Embedded server initialized.");

    logger.info("Initializing signal-detector-control routes...");
    OsdGatewayConfiguration osdGatewayConfig = OsdGatewayConfigurationLoader.load();
    configureRoutes(serviceConfig, osdGatewayConfig);
    logger.info("signal-detector-control routes initialized.");

    Spark.awaitInitialization();
    logger.info("Service initialized, ready to accept requests");
  }

  /**
   * Obtains a {@link URL} to the file at the provided path within this Jar's resources directory
   *
   * @param path String file path relative to this Jar's root, not null
   * @return URL to the resources file at the provided path
   */
  private static URL getUrlToResourceFile(String path) {
    final URL propFileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
    if (null == propFileUrl) {
      final String message = "signal-detector-control application can't find file in resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, Application.class.getName(), path);
    }
    return propFileUrl;
  }

  public static void stop() {
    Spark.stop();
  }


  private static void configureServer(HttpServiceConfiguration config) {
    Spark.port(config.getPort());
    Spark.threadPool(config.getMaxThreads(), config.getMinThreads(), config.getIdleTimeOutMillis());

    // Exception handlers.  Replicate calls to exception to avoid unchecked assignment warnings
    Spark.exception(Exception.class, exception(ErrorHandler::handle500));
    Spark.exception(IllegalArgumentException.class, exception(ErrorHandler::handle500));
    Spark.exception(NullPointerException.class, exception(ErrorHandler::handle500));

    // Error handlers
    Spark.notFound(route(r -> ErrorHandler.handle404(r.url())));
    Spark.internalServerError(route(r -> ErrorHandler.handle500("")));
  }

  /**
   * Obtains an {@link ExceptionHandler} using exceptionToString to extract a String from an {@link
   * Exception} and the exceptionHandler to create a {@link StandardResponse} from the String.
   *
   * @param exceptionHandler function creating a StandardResponse from a String, not null
   * @return ExceptionHandler, not null
   */
  private static <U extends Exception> ExceptionHandler<U> exception(
      Function<String, StandardResponse> exceptionHandler) {

    // Fills in a Response from a StandardResponse
    final BiConsumer<StandardResponse, Response> fillResponseFromStdRes = (stdRes, res) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      res.body(stdRes.getResponseBody().toString());
    };

    // Creates a StandardResponse from an Exception
    final Function<Exception, StandardResponse> exceptionToStdRes = e -> {
      //TODO: This needs to be logged, otherwise there is no indication of what went wrong.
      //For now, just printing the stack trace here.
      e.printStackTrace();
      return exceptionHandler.apply(Application.messageOrEmpty(e));
    };

    // Create the ExceptionHandler
    return (e, req, res) -> fillResponseFromStdRes.accept(exceptionToStdRes.apply(e), res);
  }

  /**
   * Obtain a {@link Route} from the routeOp function mapping a {@link Request} to a {@link
   * StandardResponse}.
   *
   * @param routeOp function mapping a {@link Request} to a {@link StandardResponse}, not null
   * @return Route, not null
   */
  private static Route route(Function<Request, StandardResponse> routeOp) {

    // Fills in a Response from a StandardResponse
    final BiFunction<Response, StandardResponse, Object> responseHandler = (res, stdRes) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      return stdRes.getResponseBody();
    };

    // Create a Route from a function mapping a Request to a StandardResponse
    final Function<Function<Request, StandardResponse>, Route> routeClosure =
        f -> (req, res) -> responseHandler.apply(res, f.apply(req));

    return routeClosure.apply(routeOp);
  }

  /**
   * Obtains the {@link Exception#getMessage()} for exception or the empty string if it does
   * not have a message
   *
   * @return String, possibly empty, not null
   */
  private static String messageOrEmpty(Exception exception) {
    return Optional.ofNullable(exception.getMessage()).orElse("");
  }

  private static void configureRoutes(HttpServiceConfiguration serviceConfig,
      OsdGatewayConfiguration osdGatewayConfig) {

    logger.info("Configuring SignalDetectorControl OSD Gateway Service routes.  Base URL: {}",
        serviceConfig.getBaseUrl());

    // Fills in a Response from a StandardResponse
    final BiFunction<Response, StandardResponse, Object> responseHandler = (res, stdRes) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      return stdRes.getResponseBody();
    };

    // Create a Route from a function mapping a Request to a StandardResponse
    final Function<Function<Request, StandardResponse>, Route> routeClosure =
        f -> (req, res) -> responseHandler.apply(res, f.apply(req));

    GatewayHandler gatewayHandler = getGatewayHandler(osdGatewayConfig);

    Function<Request, StandardResponse> invokeInputDataClosure = r -> gatewayHandler
        .fetchInvokeInputData(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(serviceConfig.getBaseUrl() + invokeInputDataPath,
        routeClosure.apply(invokeInputDataClosure));

    Function<Request, StandardResponse> storeClosure = r -> gatewayHandler
        .storeSignalDetections(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(serviceConfig.getBaseUrl() + storePath, routeClosure.apply(storeClosure));
  }

  /**
   * Obtains the content type of the {@link Request}'s body
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseContentType(Request request) {
    return ContentType.parse(request.headers("Content-Type"));
  }

  /**
   * Obtains the clients expected body type
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseAcceptType(Request request) {
    return ContentType.parse(request.headers("Accept"));
  }

  private static OsdGateway getOsdGateway(OsdGatewayConfiguration config) {
    return OsdGateway.create(getWaveformRepository(config.getWaveformPersistenceUrl()),
        getSignalDetectionRepository(config.getSignalDetectionUrl()));
  }

  private static GatewayHandler getGatewayHandler(OsdGatewayConfiguration config) {
    return GatewayHandler.create(getOsdGateway(config));
  }

  private static WaveformRepositoryInterface getWaveformRepository(String waveformRepositoryUrl) {
    return new WaveformRepositoryInfluxDb(WaveformsEntityManagerFactories
        .create(Map.of("hibernate.connection.url", waveformRepositoryUrl)));
  }

  private static SignalDetectionRepository getSignalDetectionRepository(String signalDetectionRepositoryUrl) {
    return SignalDetectionRepositoryJpa.create(SignalDetectionEntityManagerFactories.
        create(Map.of("hibernate.connection.url", signalDetectionRepositoryUrl)));
  }
}
