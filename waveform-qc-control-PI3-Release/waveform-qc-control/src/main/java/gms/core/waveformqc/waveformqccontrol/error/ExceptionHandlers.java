package gms.core.waveformqc.waveformqccontrol.error;


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
   * {@link ExceptionHandler} for all {@link Exception}s.
   */
  public static ExceptionHandler<Exception> handleDefault = (e, request, response) -> {
    logger.error("Exception caught by custom exception handler: "
        + ExceptionUtils.getStackTrace(e));

    // Set the response.
    response.status(500);
    response.body("{ message: \"Internal server error.\" }");
  };

  /**
   * {@link ExceptionHandler} for {@link IllegalArgumentException}.
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
   * {@link ExceptionHandler} for {@link NullPointerException}.
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
