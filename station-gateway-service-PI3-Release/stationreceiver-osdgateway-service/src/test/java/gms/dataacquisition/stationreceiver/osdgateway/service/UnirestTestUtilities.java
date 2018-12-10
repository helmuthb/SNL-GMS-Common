package gms.dataacquisition.stationreceiver.osdgateway.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RawBody;
import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import java.util.Map;

/**
 * Utilities for making REST calls using the Unirest. Used in tests.
 */
public class UnirestTestUtilities {

  /**
   * Configure the ObjectMapper Unirest will use to serialize requests
   * to use our custom setup.
   */
  static {

    Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return SerializationUtility.objectMapper.readValue(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return SerializationUtility.objectMapper.writeValueAsString(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Posts JSON serialized from the input obj to the specific url and gets back the HttpResponse<T>,
   * where T is a type parameter of the response type.
   *
   * @param obj the obj to post to the url as json
   * @param url the url to post to
   * @param responseType the response type expected back, a type parameter
   * @param <T> the type parameter of the response type
   * @return http response from the url, if any.
   * @throws Exception if the url is unreachable, serialization issues, malformed request, etc.
   */
  public static <T> HttpResponse<T> postJson(Object obj, String url, Class<T> responseType)
      throws Exception {
    return Unirest.post(url)
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .header("Connection", "close")
        .body(obj)
        .asObject(responseType);
  }

  /**
   * Sends the data to the OSD Gateway Service, via an HTTP post with msgpack.
   *
   * @param obj data to be sent
   * @param url endpoint
   * @param responseType type of the response object
   * @param <T> deserialized response
   * @return An object containing the OSD Gateway Service's response.
   * @throws Exception if for instance, the host cannot be reached
   */
  public static <T> HttpResponse<T> postMsgPack(Object obj, String url, Class<T> responseType)
      throws Exception {
    RawBody body = Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(SerializationUtility.msgPackMapper.writeValueAsBytes(obj));
    return body.asObject(responseType);
  }

  public static <T> HttpResponse<T> getJson(String url,
      Map<String, Object> params, Class<T> responseType) throws Exception {

    return Unirest.get(url)
        .header("Accept", "application/json")
        .header("Connection", "close")
        .queryString(params)
        .asObject(responseType);
  }

}
