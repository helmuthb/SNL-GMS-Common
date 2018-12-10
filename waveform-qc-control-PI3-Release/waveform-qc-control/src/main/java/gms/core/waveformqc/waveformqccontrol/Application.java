package gms.core.waveformqc.waveformqccontrol;

import com.mashape.unirest.http.Unirest;
import gms.core.waveformqc.waveformqccontrol.configuration.ServiceConfiguration;
import gms.core.waveformqc.waveformqccontrol.configuration.ConfigurationManagement;
import gms.core.waveformqc.waveformqccontrol.configuration.OsdGatewayClientConfiguration;
import gms.core.waveformqc.waveformqccontrol.control.ControlHandler;
import gms.core.waveformqc.waveformqccontrol.control.WaveformQcControl;
import gms.core.waveformqc.waveformqccontrol.error.ExceptionHandlers;
import gms.core.waveformqc.waveformqccontrol.error.HttpErrorHandler;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.OsdGatewayClient;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry;
import gms.core.waveformqc.waveformqccontrol.util.ObjectSerialization;
import gms.core.waveformqc.waveformqccontrol.util.Path;
import gms.core.waveformqc.waveformqccontrol.util.Path.Web;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

/**
 * Application for running the waveform qc service.
 */

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static WaveformQcControl control;

  private Application() {
  }

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  public static void start() {
    logger.info("Initializing waveform-qc-control service...");

    logger.info("Initializing configuration management...");
    ConfigurationManagement.initialize();
    logger.info("Configuration management initialized");

    logger.info("Generating configuration...");
    ServiceConfiguration serviceConfiguration = ServiceConfiguration.create();
    OsdGatewayClientConfiguration gatewayClientConfiguration = OsdGatewayClientConfiguration
        .create();
    logger.info("Configuration generated");

    logger.info("Initializing WaveformQcControl...");
    initializeWaveformQcControl(gatewayClientConfiguration);
    logger.info("WaveformQcControl initialized");

    logger.info("Configuring service...");
    configureService(serviceConfiguration);
    logger.info("Service configured");

    Spark.awaitInitialization();
    logger.info("Service initialized, ready to accept requests");
  }

  public static void stop() {
    Spark.stop();
  }

  private static void initializeWaveformQcControl(OsdGatewayClientConfiguration config) {
    Unirest.setObjectMapper(ObjectSerialization.getClientObjectMapper());
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();

    // TODO: use module system for plugin discovery?
    logger.info("Registering {} plugins",
        ServiceLoader.load(WaveformQcPlugin.class).stream().count());
    ServiceLoader.load(WaveformQcPlugin.class).stream()
        .map(Provider::get)
        .forEach(registry::register);

    logger.info("Configuring OSD Gateway Client");
    OsdGatewayClient gatewayClient = new OsdGatewayClient(config.getHost(),
        config.getPort(), config.getBaseUri());

    control = new WaveformQcControl(registry, gatewayClient);
    control.initialize();
  }

  private static void configureService(ServiceConfiguration config) {
    //Configure port
    Spark.port(config.getPort());
    //Configure thread pool
    Spark.threadPool(config.getMaxThreads(), config.getMinThreads(),
        config.getIdleTimeOutMillis());

    // Add Exception handlers.
    Spark.exception(Exception.class, ExceptionHandlers.handleDefault);
    Spark.exception(IllegalArgumentException.class,
        ExceptionHandlers.handleIllegalArgument);
    Spark.exception(NullPointerException.class, ExceptionHandlers.handleNullPointer);

    // Add HTTP custom error handlers.
    Spark.notFound(HttpErrorHandler.handle404);
    Spark.internalServerError(HttpErrorHandler.handle500);

    // Setup routes
    Path.baseUri = config.getBaseUri();
    Spark.post(Web.INVOKE, ControlHandler::invokeControl);
    Spark.post(Web.INVOKE+ "/no-context", ControlHandler::invokeControlNoContext);
    Spark.get(Web.ALIVE, ControlHandler::alive);
  }

}
