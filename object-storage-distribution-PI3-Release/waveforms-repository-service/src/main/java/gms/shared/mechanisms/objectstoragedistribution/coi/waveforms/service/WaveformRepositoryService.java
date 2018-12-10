package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.ExceptionHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.HttpErrorHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.WaveformRepositoryHttpRouteHandlers;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import spark.ExceptionHandler;
import spark.Spark;

/**
 * Service implementation of the waveform-repository.
 *
 * Provides a minimal wrapper around a singleton {@link Spark} service.
 */
public class WaveformRepositoryService {

  private WaveformRepositoryService() {

  }

  /**
   * Starts the service with the provided {@link Configuration} to determine service properties.
   * Routes repository calls to the provided {@link WaveformRepositoryInterface}, {@link
   * StationSohRepositoryInterface}.
   *
   * @param configuration the configuration of the service
   * @param waveformRepository the Waveform Repository Interface
   * @param stationSohRepository the Station Soh Repository Interface
   */
  public static void startService(Configuration configuration,
      WaveformRepositoryInterface waveformRepository,
      StationSohRepositoryInterface stationSohRepository,
      RawStationDataFrameRepositoryInterface frameRepository) {
    Objects.requireNonNull(configuration,
        "Cannot create WaveformRepositoryService with null configuration");
    Objects.requireNonNull(waveformRepository,
        "Cannot create WaveformRepositoryService with null waveformRepository");
    Objects.requireNonNull(stationSohRepository,
        "Cannot create WaveformRepositoryService with null stationSohRepository");
    Objects.requireNonNull(frameRepository,
        "Cannot create WaveformRepositoryService with null frameRepository");

    configureHttpServer(configuration);
    configureRoutesAndFilters(configuration, waveformRepository, stationSohRepository,
        frameRepository);
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
   *
   * @param configuration the configuration to be used
   */
  private static void configureHttpServer(Configuration configuration) {
    // Set the listening port.
    Spark.port(configuration.port);

    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(configuration.maxThreads,
        configuration.minThreads,
        configuration.idleTimeOutMillis);
  }

  /**
   * Registers all routes and filters. Sets exceptions for endpoints to specific pre implemented
   * responses.
   *
   * @param configuration the configuration to be used
   * @param waveformRepository the Waveform Repository Interface
   * @param stationSohRepository the Station Soh Repository Interface
   * @param frameRepository the frame repository interface
   */
  private static void configureRoutesAndFilters(Configuration configuration,
      WaveformRepositoryInterface waveformRepository,
      StationSohRepositoryInterface stationSohRepository,
      RawStationDataFrameRepositoryInterface frameRepository) {

    // exception handler.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);

    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);
    // requests normally return JSON
    Spark.before((request, response) -> response.type("application/json"));

    //Get routes
    Spark.get(configuration.getBaseUrl() + "channel-segment",
        ((request, response) -> WaveformRepositoryHttpRouteHandlers
            .retrieveChannelSegment(request, response, waveformRepository)));

    Spark.get(configuration.getBaseUrl() + "acquired-channel-soh/analog/:id",
        ((request, response) -> WaveformRepositoryHttpRouteHandlers
            .getAcquiredChannelSohAnalog(request, response, stationSohRepository)));

    Spark.get(configuration.getBaseUrl() + "acquired-channel-soh/boolean/:id",
        ((request, response) -> WaveformRepositoryHttpRouteHandlers
            .getAcquiredChannelSohBoolean(request, response, stationSohRepository)));

    Spark.get(configuration.getBaseUrl() + "acquired-channel-soh/boolean",
        ((request, response) -> WaveformRepositoryHttpRouteHandlers
            .getAcquiredChannelSohBooleanTimeRange(request, response, stationSohRepository)));

    Spark.get(configuration.getBaseUrl() + "acquired-channel-soh/analog",
        ((request, response) -> WaveformRepositoryHttpRouteHandlers
            .getAcquiredChannelSohAnalogTimeRange(request, response, stationSohRepository)));

    Spark.get(configuration.getBaseUrl() + "frames",
        (request, response) -> WaveformRepositoryHttpRouteHandlers
            .getRawStationDataFrames(request, response, frameRepository));

    Spark.get(configuration.getBaseUrl() + "alive", (WaveformRepositoryHttpRouteHandlers::alive));
  }
}
