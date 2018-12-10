package gms.core.waveformqc.waveformqccontrol.error;

import spark.Route;

/**
 * HTTP error handler {@link Route}s for the service.  HTTP errors get routed to these handlers
 * to return appropriate HTTP responses.
 */
public final class HttpErrorHandler {

  // Prevent class from being instantiated, since it is a "static" class.
  private HttpErrorHandler() {
  }

  public static Route handle400 = (request, response) -> {
    response.type("application/json");
    return "{ \"message\": \"Custom 400 - Bad Request\" }";
  };

  public static Route handle404 = (request, response) -> {
    response.type("application/json");
    return "{ \"message\": \"Custom 404 - Not Found\" }";
  };

  public static Route handle500 = (request, response) -> {
    response.type("application/json");
    return "{ \"message\": \"Custom 500 - Internal Server Error\" }";
  };
}
