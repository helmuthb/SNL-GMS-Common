package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.mashape.unirest.http.HttpResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.SignalDetectionRepositoryService;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.UnirestTestUtilities;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

@Ignore
public class QcMaskRouteHandlersTests {

  // TODO: consider making these integration tests

  private static final String processingChannelId = "ba8a2aa5-ae09-46ea-a15c-87f222980572";
  private static final String startTime = "2010-05-20T00:59:59.108Z";
  private static final String endTime = "2010-05-20T01:00:01.991Z";

  private static final Configuration config;
  private static final String RETRIEVE_QC_MASK_URL; // set in static initializer

  private static spark.Request mockRequest = Mockito.mock(spark.Request.class);
  private static spark.Response mockResponse = Mockito.mock(spark.Response.class);
  private static QcMaskRepository mockQcMaskRepository = Mockito.mock(QcMaskRepository.class);
  private static SignalDetectionRepository mockSignalDetectionRepository = Mockito
      .mock(SignalDetectionRepository.class);
  private static ProcessingStationReferenceFactory mockStationReferenceFactory = Mockito
      .mock(ProcessingStationReferenceFactory.class);

  static {
    config = ConfigurationLoader.load();
    RETRIEVE_QC_MASK_URL = "http://localhost:" + config.getPort() + config.getBaseUrl() + "qc-mask";
  }

  @BeforeClass
  public static void setup() {

    SignalDetectionRepositoryService
        .startService(config, mockQcMaskRepository, mockSignalDetectionRepository,
            mockStationReferenceFactory);

    given(mockQcMaskRepository
        .findCurrentByProcessingChannelIdAndTimeRange(
            UUID.fromString(processingChannelId),
            Instant.parse(startTime),
            Instant.parse(endTime)))
        .willReturn(List.of(TestFixtures.qcMask));
  }

  @AfterClass
  public static void teardown() {
    SignalDetectionRepositoryService.stopService();
  }

  @Test
  public void testStartServiceChecksNull() throws Exception {
    QcMaskRouteHandlers handlers = new QcMaskRouteHandlers();

    TestUtilities.checkMethodValidatesNullArguments(handlers,
        "findCurrentByProcessingChannelIdAndTimeRange", mockRequest, mockResponse,
        mockQcMaskRepository);
  }

  /**
   * Tests that posting a null request to all specified endpoints,
   * which should return a 'bad request'.
   */
  @Test
  public void testBadParametersForEndpoints() throws Exception {
    HttpResponse<String> response = UnirestTestUtilities.getJson(RETRIEVE_QC_MASK_URL);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  @Test
  public void testEndpointQcMaskReturnsNotAcceptable() throws Exception {
    // Can't test the service only produces json but can test it returns 406 (Not Acceptable)
    // for some non-json accept types
    String queryUrl = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + processingChannelId + "&" +
        "start-time=" + startTime + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.get(queryUrl, "text/plain");
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_ACCEPTABLE_406, response.getStatus());
    assertEquals("Error (406 - Not Acceptable)", response.getBody());

    response = UnirestTestUtilities.get(queryUrl, "application/msgpack");
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_ACCEPTABLE_406, response.getStatus());
    assertEquals("Error (406 - Not Acceptable)", response.getBody());
  }

  /**
   * Test QcMask endpoint, checking query parameters are parsed correctly.
   * Mocking a call to the QcMaskRepository to return a QcMask. Testing endpoint
   * response matches expected status code, and json body.
   */
  @Test
  public void testQueryParametersEndpointQcMask() throws Exception {
    String queryUrl = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + processingChannelId + "&" +
        "start-time=" + startTime + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(queryUrl);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    assertEquals(TestFixtures.qcMaskJson, response.getBody());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad channel-id.
   * Expected a response code of HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadChannelIdQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=ba@8a2aa5-ae09-46ea-a15c-87f222980572" + "&" +
        "start-time=" + startTime + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad start time.
   * Expected a response code of HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadStartTimeQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + processingChannelId + "&" +
        "start-time=2010-05@-20T00:59:59.108Z" + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad end time.
   * Expected a response code of HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadEndTimeQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + processingChannelId + "&" +
        "start-time=" + startTime + "&" +
        "end-time=2010-05-2@0T01:00:01.991Z";

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }
}
