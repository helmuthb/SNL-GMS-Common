package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.ExceptionHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.HttpErrorHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.QcMaskRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.SignalDetectionRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.StationProcessingRouteHandlers;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import spark.Spark;

/**
 * Service implementation of the signaldetection-repository.
 *
 * Provides a minimal wrapper around a singleton {@link Spark} service.
 */
public class SignalDetectionRepositoryService {

  private SignalDetectionRepositoryService() {
  }

  /**
   * Start the service using the provided {@link Configuration} to determine service properties.
   * Routes repository calls to the provided {@link QcMaskRepository}.
   *
   * @param configuration Configuration for this service, not null
   * @param qcMaskRepository route repository calls to this QcMaskRepository
   */
  public static void startService(Configuration configuration, QcMaskRepository qcMaskRepository,
      SignalDetectionRepository signalDetectionRepository,
      ProcessingStationReferenceFactory processingStationReferenceFactory) {
    Objects.requireNonNull(configuration,
        "Cannot create SignalDetectionRepositoryService with null configuration");
    Objects.requireNonNull(qcMaskRepository,
        "Cannot create SignalDetectionRepositoryService with null qcMaskRepository");
    Objects.requireNonNull(signalDetectionRepository,
        "Cannot create SignalDetectionRepositoryService with null signalDetectionRepository");
    Objects.requireNonNull(processingStationReferenceFactory,
        "Cannot create SignalDetectionRepositoryService with null processingStationReferenceFactory");

    configureHttpServer(configuration);
    configureRoutesAndFilters(configuration, qcMaskRepository, signalDetectionRepository,
        processingStationReferenceFactory);
    Spark.awaitInitialization();
  }

  /**
   * Configures the HTTP server before the it is started. NOTE: This method must be called before
   * routes and filters are declared, because Spark Java automatically starts the HTTP server when
   * routes and filters are declared.
   */
  private static void configureHttpServer(Configuration configuration) {
    // Set the listening port.
    Spark.port(configuration.getPort());

    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(configuration.getMaxThreads(),
        configuration.getMinThreads(),
        configuration.getIdleTimeOutMillis());
  }

  /**
   * Registers all routes and filters. Sets exceptions for endpoints to specific pre implemented
   * responses.
   */
  private static void configureRoutesAndFilters(Configuration configuration,
      QcMaskRepository qcMaskRepository,
      SignalDetectionRepository signalDetectionRepository,
      ProcessingStationReferenceFactory processingStationReferenceFactory) {

    // Exception handlers.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);
    Spark.exception(IllegalArgumentException.class,
        ExceptionHandlers::IllegalArgumentExceptionHandler);
    Spark.exception(DateTimeParseException.class, ExceptionHandlers::DateTimeParseExceptionHandler);
    Spark.exception(NullPointerException.class, ExceptionHandlers::NullPointerExceptionHandler);

    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);

    // qc masks
    Spark.get(configuration.getBaseUrl() + "qc-mask",
        ((request, response) -> QcMaskRouteHandlers
            .findCurrentByProcessingChannelIdAndTimeRange(request, response, qcMaskRepository)));

    // signal detections
    SignalDetectionRouteHandlers signalDetectionRoutes = SignalDetectionRouteHandlers
        .create(signalDetectionRepository);
    Spark.get(configuration.getBaseUrl() + "signal-detections/:id",
        signalDetectionRoutes::getSignalDetection);

    //processing objects
    StationProcessingRouteHandlers processingRoutes = StationProcessingRouteHandlers
        .create(processingStationReferenceFactory);

    Spark.get(configuration.getBaseUrl() + "network", processingRoutes::getNetwork);
  }

  /**
   * Stops the REST service.
   */
  public static void stopService() {
    Spark.stop();
  }
}
