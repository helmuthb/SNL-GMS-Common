package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Contains handler functions for the service routes.
 */
public class QcMaskRouteHandlers {

  private static Logger logger = LoggerFactory
      .getLogger(QcMaskRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    SignalDetectionJacksonMixins.register(objectMapper);
  }

  /**
   * Handles a request to retrieve QC Mask via {@link QcMaskRepository}.
   *
   * @param request the request (HTTP), not null
   * @param response the response (HTTP); this can be modified before responding, not null
   * @param qcMaskRepository the interface used, not null
   * @return Collection of QcMask resulting from the query, not null
   * @throws NullPointerException if request, response, or qcMaskRepository are null
   */
  public static String findCurrentByProcessingChannelIdAndTimeRange(
      Request request, Response response, QcMaskRepository qcMaskRepository) throws Exception {

    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Objects.requireNonNull(qcMaskRepository, "Cannot accept null qcMaskRepository");

    String processingChannelIdString = request.queryParams("channel-id");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");

    logger.info(
        "findCurrentByProcessingChannelIdAndTimeRange endpoint hit with parameters: "
            + "channel-id = {}, start-time = {}, end-time = {}",
        processingChannelIdString, startTimeString, endTimeString);

    // Client requested json
    if (RequestUtil.clientAcceptsJson(request)) {
      response.type("application/json");

      // Registered ExceptionHandlers return 400 (Bad Request) if the parameters do not parse
      List<QcMask> qcMasks = qcMaskRepository
          .findCurrentByProcessingChannelIdAndTimeRange(
              UUID.fromString(processingChannelIdString),
              Instant.parse(startTimeString),
              Instant.parse(endTimeString));

      // ExceptionHandler will catch the exception and return a 500
      try {
        return objectMapper.writeValueAsString(qcMasks);
      } catch (JsonProcessingException e) {
        logger.debug("Error converting QcMasks to json", e);
        throw new Exception("Could not convert List<QcMask> to json", e);
      }
    }

    // Only json supported at this time
    else {
      response.status(HttpStatus.NOT_ACCEPTABLE_406);
      return "Error (406 - Not Acceptable)";
    }
  }

}
