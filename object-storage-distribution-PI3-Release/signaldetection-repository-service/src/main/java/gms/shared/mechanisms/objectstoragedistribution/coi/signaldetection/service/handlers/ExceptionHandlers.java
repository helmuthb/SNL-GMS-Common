package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import java.time.format.DateTimeParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Exception handlers for the service.  Exceptions get routed to these handlers
 * to return appropriate HTTP responses.
 */
public final class ExceptionHandlers {

  private static Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

  // Prevent class from being instantiated, since it is a "static" class.
  private ExceptionHandlers() {
  }

  /**
   * Catch-all exception handler.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static void ExceptionHandler(Exception e, Request request, Response response) {
    logger.error("Exception caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    response.body("{ message: \"Internal server error.\" }");
  }

  /**
   * Catches only the IllegalArgumentException.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static void IllegalArgumentExceptionHandler(IllegalArgumentException e, Request request,
      Response response) {
    logger.error("IllegalArgumentException caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(HttpStatus.BAD_REQUEST_400);
    response.body("{ message: \"Bad Request\" }");
  }

  /**
   * Catches only the DateTimeParseException.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static void DateTimeParseExceptionHandler(DateTimeParseException e, Request request,
      Response response) {
    logger.error("DateTimeParseException caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(HttpStatus.BAD_REQUEST_400);
    response.body("{ message: \"Bad Request\" }");
  }

  /**
   * Catches only the NullPointerException.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static void NullPointerExceptionHandler(NullPointerException e, Request request,
      Response response) {
    logger.error("NullPointerException caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(HttpStatus.BAD_REQUEST_400);
    response.body("{ message: \"Bad Request\" }");
  }
}
