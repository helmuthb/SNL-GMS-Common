package gms.dataacquisition.cssloader.osdgateway.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.dataacquisition.cssloader.serialization.SerializationUtility;

import java.util.List;
import org.eclipse.jetty.http.HttpStatus;

import static org.junit.Assert.*;

/**
 * Utility methods useful for reducing verbosity and repetition in tests.
 */
public final class TestUtilities {

  /**
   * Helper method to test post a null request to a specified endpoint, which should return a 'bad
   * request'.
   *
   * @param URI the endpoint to hit
   */
  public static void testPostNullRequest(String URI) throws Exception {
    String json = SerializationUtility.objectMapper.writeValueAsString(null);
    System.out.println("Test: posting json to URI " + URI + " : " + json);

    HttpResponse<String> response = UnirestTestUtilities.postJson(null, URI, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Helper method to post a valid (non-null) object request to a URI as JSON and get back an
   * expected response, application/json and status 200 (OK).
   *
   * @param URI the endpoint to hit
   * @param request the request object, which will be serialized to JSON using the
   * ObjectSerializationUtility
   * @param compressed if true, stores the object as compressed format
   */
  public static void testStoreValidObject(String URI, Object request,
      boolean compressed) throws Exception {
    testStoreValidObject(URI, request, compressed, "application/json", HttpStatus.OK_200);
  }

  /**
   * Helper method to post a valid (non-null) object request to a URI as JSON and get back an
   * expected response
   *
   * @param URI the endpoint to hit
   * @param request the request object, which will be serialized to JSON using the
   * ObjectSerializationUtility
   * @param compressed if true, stores the object as compressed format
   * @param contentType the expected content type of the response
   * @param expectedHttpStatus the expected status code to come back
   */
  public static void testStoreValidObject(String URI, Object request,
      boolean compressed, String contentType, int expectedHttpStatus) throws Exception {
    String json = SerializationUtility.objectMapper.writeValueAsString(request);
    System.out.println("Test: posting json to URI " + URI + " : " + json);
    // post the request and get the response
    HttpResponse<String> response;
    if (compressed) {
      response = UnirestTestUtilities.postMsgPack(request, URI, String.class);
    } else {
      response = UnirestTestUtilities.postJson(request, URI, String.class);
    }
    // assert the response isn't null and returned the right status
    assertNotNull(response);
    assertEquals(response.getStatus(), expectedHttpStatus);
    // check the headers' content-type is 'application/json'
    List<String> content_types = response.getHeaders().get("Content-Type");
    assertTrue(content_types != null && content_types.contains(contentType));
  }

  public static void testGetWithException(String URI, int expectedHttpStatus) throws Exception {
    HttpResponse<String> response = UnirestTestUtilities.getJson(URI, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), expectedHttpStatus);
    // check the headers' content-type is 'application/json'
    List<String> content_types = response.getHeaders().get("Content-Type");
    assertTrue(content_types != null && content_types.contains("application/text"));
  }
}
