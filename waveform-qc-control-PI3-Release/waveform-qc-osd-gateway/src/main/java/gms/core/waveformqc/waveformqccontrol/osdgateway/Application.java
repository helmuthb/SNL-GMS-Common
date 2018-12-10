package gms.core.waveformqc.waveformqccontrol.osdgateway;

import com.mashape.unirest.http.Unirest;
import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.ConfigurationManagement;
import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.OsdGatewayConfiguration;
import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.ServiceConfiguration;
import gms.core.waveformqc.waveformqccontrol.osdgateway.error.ExceptionHandlers;
import gms.core.waveformqc.waveformqccontrol.osdgateway.error.HttpErrorHandler;
import gms.core.waveformqc.waveformqccontrol.osdgateway.gateway.GatewayHandler;
import gms.core.waveformqc.waveformqccontrol.osdgateway.gateway.OsdGateway;
import gms.core.waveformqc.waveformqccontrol.osdgateway.util.ObjectSerialization;
import gms.core.waveformqc.waveformqccontrol.osdgateway.util.Path;
import gms.core.waveformqc.waveformqccontrol.osdgateway.util.Path.Web;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.ProvenanceRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.ProvenanceEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.ProvenanceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.QcMaskRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.SignalDetectionEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.WaveformsEntityManagerFactories;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

/**
 * Application for running the waveform qc service.
 */

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static OsdGateway gateway;

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  public static void start() {
    logger.info("Initializing waveform-qc-control service...");

    logger.info("Initializing Configuration management...");
    ConfigurationManagement.initialize();
    logger.info("ServiceConfiguration management initialized");

    logger.info("Generating Configuration...");
    ServiceConfiguration serviceConfiguration = ServiceConfiguration.create();
    OsdGatewayConfiguration gatewayConfiguration = OsdGatewayConfiguration.create();
    logger.info("Configuration generated");

    logger.info("Initializing WaveformQcControl...");
    initializeOsdGateway(gatewayConfiguration);
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

  private static void initializeOsdGateway(OsdGatewayConfiguration config) {
    Unirest.setObjectMapper(ObjectSerialization.getClientObjectMapper());

    WaveformRepositoryInterface waveformRepository = new WaveformRepositoryInfluxDb(
        WaveformsEntityManagerFactories.create(Map.of("hibernate.connection.url",
            config.getWaveformPersistenceUrl()))
    );

    StationSohRepositoryInterface stationSohRepository = new StationSohRepositoryJpa(
        WaveformsEntityManagerFactories.create(Map.of("hibernate.connection.url",
            config.getStationSohPersistenceUrl())));

    QcMaskRepository qcMaskRepository = new QcMaskRepositoryJpa(
        SignalDetectionEntityManagerFactories.create(Map.of("hibernate.connection.url",
            config.getQcMaskPersistenceUrl())));

    ProvenanceRepository provenanceRepository = new ProvenanceRepositoryJpa(
        ProvenanceEntityManagerFactories.create(Map.of("hibernate.connection.url",
            config.getProvenancePersistenceUrl())));

    gateway = new OsdGateway(waveformRepository, stationSohRepository, qcMaskRepository,
        provenanceRepository);
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
    Spark.post(Web.INVOKE_INPUT_DATA, GatewayHandler::fetchInvokeInputData);
    Spark.post(Web.STORE, GatewayHandler::storeQcMasks);
    Spark.awaitInitialization();
  }

}