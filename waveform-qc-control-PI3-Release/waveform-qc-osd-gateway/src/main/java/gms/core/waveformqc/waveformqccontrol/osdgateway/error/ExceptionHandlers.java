package gms.core.waveformqc.waveformqccontrol.osdgateway.error;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;

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
  public static ExceptionHandler<Exception> handleDefault = (e, request, response) -> {
    logger.error("Exception caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(500);
    response.body("{ message: \"Internal server error.\" }");
  };

  /**
   * Catches only the IllegalArgumentException.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static ExceptionHandler<IllegalArgumentException> handleIllegalArgument = (e, request,
      response) -> {
    logger.error("IllegalArgumentException caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(400);
    response.body("{ message: \"Bad Request\" }");
  };

  /**
   * Catches only the NullPointerException.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static ExceptionHandler<NullPointerException> handleNullPointer = (e, request,
      response) -> {
    logger.error("NullPointerException caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(400);
    response.body("{ message: \"Bad Request\" }");
  };
}
