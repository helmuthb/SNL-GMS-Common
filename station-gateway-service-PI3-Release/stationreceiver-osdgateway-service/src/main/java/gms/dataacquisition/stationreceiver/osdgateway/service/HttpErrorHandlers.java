package gms.dataacquisition.stationreceiver.osdgateway.service;

import spark.Request;
import spark.Response;


public final class HttpErrorHandlers {

  // Prevent class from being instantiated, since it is a "static" class.
  private HttpErrorHandlers() {
  }

  /**
   * Responds to "bad" requests (i.e. requests that are incomplete, missing input parameters,
   * contain invalid input values, etc).
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occured
   * @return json string response
   */
  public static Object Http400(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 400 - Bad Request\" }";
  }

  /**
   * @param request the request that caused the exception
   * @param response the response being built when the exception occured
   * @return json string response
   */
  public static Object Http404(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 404 - Not Found\" }";
  }

  /**
   * Responds to requests when an unhandled internal exception is thrown.
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occured
   * @return json string response
   */
  public static Object Http500(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 500 - Internal Server Error\" }";
  }
}
