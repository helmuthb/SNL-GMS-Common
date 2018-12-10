package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ResponseUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import spark.Request;
import spark.Response;

/**
 * Suite of route handler methods for handling signal detection http requests
 */
public class SignalDetectionRouteHandlers {

  private final SignalDetectionRepository signalDetectionRepository;

  private SignalDetectionRouteHandlers(
      SignalDetectionRepository signalDetectionRepository) {
    this.signalDetectionRepository = signalDetectionRepository;
  }

  /**
   * Factory method for creating {@link SignalDetectionRouteHandlers}
   *
   * @param signalDetectionRepository Signal Detection Repository class for retrieving
   * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}
   * objects from persistence
   * @return The route handlers object using the input repository
   */
  public static SignalDetectionRouteHandlers create(
      SignalDetectionRepository signalDetectionRepository) {
    return new SignalDetectionRouteHandlers(Objects.requireNonNull(signalDetectionRepository));
  }

  /**
   * Base method for retrieving Signal Detections. Handles the majority GET requests via parameters and query strings.
   *
   * @param request The Spark Java Request object containing all request information
   * @param response The Spark Java response object provided to set things like status codes.
   * @return The response body representing the result of a query for Signal Detections.
   * Spark Java sets this into the response body automatically.
   */
  public String getSignalDetection(Request request, Response response) {
    Objects.requireNonNull(request);
    Objects.requireNonNull(response);

    Optional<UUID> id = Optional.ofNullable(request.params(":id")).map(UUID::fromString);

    if (RequestUtil.clientAcceptsJson(request)) {
      //return a single signal detection if provided an id, otherwise return all signal detections
      return ObjectSerialization.writeValue(
          id.isPresent() ? signalDetectionRepository.findSignalDetectionById(id.get())
              : signalDetectionRepository.retrieveAll());
    } else {
      return ResponseUtil.notAcceptable(request, response);
    }

  }
}
