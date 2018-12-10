package gms.dataacquisition.stationreceiver.osdgateway.service;

import com.mashape.unirest.http.HttpResponse;
import gms.dataacquisition.stationreceiver.osdgateway.Endpoints;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.dataacquisition.stationreceiver.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.stationreceiver.osdgateway.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


/**
 * Tests all route handlers.
 */
public class StationReceiverOsdGatewayServiceTest {

  private static StationReceiverOsdGatewayInterface mockGateway
      = mock(StationReceiverOsdGatewayInterface.class);
  private static Configuration config;
  private static final String host = "localhost";

  @BeforeClass
  public static void setup() throws Exception {
    config = ConfigurationLoader.load();
    StationReceiverOsdGatewayService.startService(mockGateway, config);
  }

  @AfterClass
  public static void teardown() {
    StationReceiverOsdGatewayService.stopService();
  }

  /**
   * Tests that posting a null object to the 'store analog soh' endpoint results in a 'bad request'
   * response.
   */
  @Test
  public void testStoreNullStationSohAnalogBatch() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSohAnalogUrl(host, config.port));
  }

  /**
   * Tests that posting a null object to the 'store boolean soh' endpoint results in a 'bad request'
   * response.
   */
  @Test
  public void testStoreNullStationSohBooleanBatch() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSohBooleanUrl(host, config.port));
  }

  /**
   * Tests that posting a null object to the 'store channel segments' endpoint results in a 'bad
   * request' response.
   */
  @Test
  public void testStoreNullChannelSegments() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeChannelSegmentsUrl(host, config.port));
  }

  /**
   * Tests that posting a null object to the 'store raw station data frame' endpoint results in a
   * 'bad request' response.
   */
  @Test
  public void testStoreNullRawStationDataFrame() throws Exception {
    HttpResponse<String> response = UnirestTestUtilities.postJson(null,
        Endpoints.storeRawStationDataFrameUrl(host, config.port), String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    assertFalse(response.getBody().isEmpty());
  }

  /**
   * Tests storing a valid Channel SOH Analog.
   */
  @Test
  public void storeChannelSohAnalogTest() throws Exception {
    Set<AcquiredChannelSohAnalog> sohs = Set.of(TestFixtures.channelSohAnalog);
    doNothing().when(mockGateway).storeAnalogChannelStatesOfHealth(sohs);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohAnalogUrl(host, config.port),
        sohs, false);
  }

  @Test
  public void storeChannelSohAnalogStorageUnavailableTest() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeAnalogChannelStatesOfHealth(any());
    Set<AcquiredChannelSohAnalog> sohs = Set.of(TestFixtures.channelSohAnalog);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohAnalogUrl(host, config.port),
        sohs, false, "application/text", HttpStatus.SC_SERVICE_UNAVAILABLE);
    reset(mockGateway);
  }

  @Test
  public void storeChannelSohAnalogDataExistsTest() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeAnalogChannelStatesOfHealth(any());
    Set<AcquiredChannelSohAnalog> sohs = Set.of(TestFixtures.channelSohAnalog);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohAnalogUrl(host, config.port),
        sohs, false, "application/text", HttpStatus.SC_CONFLICT);
    reset(mockGateway);
  }

  /**
   * Tests storing a valid Channel SOH Boolean.
   */
  @Test
  public void storeChannelSohBooleanTest() throws Exception {
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.channelSohBool);
    doNothing().when(mockGateway).storeBooleanChannelStatesOfHealth(sohs);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohBooleanUrl(host, config.port),
        sohs, false);
  }

  @Test
  public void storeChannelSohBooleanStorageUnavailableTest() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeBooleanChannelStatesOfHealth(any());
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.channelSohBool);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohBooleanUrl(host, config.port),
        sohs, false, "application/text", HttpStatus.SC_SERVICE_UNAVAILABLE);
    reset(mockGateway);
  }

  @Test
  public void storeChannelSohBooleanDataExistsTest() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeBooleanChannelStatesOfHealth(any());
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.channelSohBool);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohBooleanUrl(host, config.port),
        sohs, false, "application/text", HttpStatus.SC_CONFLICT);
    reset(mockGateway);
  }

  /**
   * Tests storing a valid Channel Segment
   */
  @Test
  public void storeChannelSegmentsTest() throws Exception {
    Set<ChannelSegment> segments = Set.of(TestFixtures.channelSegment);
    doNothing().when(mockGateway).storeChannelSegments(segments);
    TestUtilities.testStoreValidObject(
        Endpoints.storeChannelSegmentsUrl(host, config.port),
        segments,true);
  }

  @Test
  public void storeChannelSegmentsStorageUnavailableTest() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeChannelSegments(any());
    Set<ChannelSegment> segments = Set.of(TestFixtures.channelSegment);
    TestUtilities.testStoreValidObject(
        Endpoints.storeChannelSegmentsUrl(host, config.port),
        segments, true, "application/text", HttpStatus.SC_SERVICE_UNAVAILABLE);
    reset(mockGateway);
  }

  @Test
  public void storeChannelSegmentsDataExistsTest() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeChannelSegments(any());
    Set<ChannelSegment> segments = Set.of(TestFixtures.channelSegment);
    TestUtilities.testStoreValidObject(
        Endpoints.storeChannelSegmentsUrl(host, config.port),
        segments, true, "application/text", HttpStatus.SC_CONFLICT);
    reset(mockGateway);
  }

  /**
   * Tests storing a valid Raw Station Data Frame
   */
  @Test
  public void storeRawStationDataFrameTest() throws Exception {
    RawStationDataFrame frame = TestFixtures.frame1;
    doNothing().when(mockGateway).storeRawStationDataFrame(frame);
    TestUtilities.testStoreValidObject(
        Endpoints.storeRawStationDataFrameUrl(host, config.port), frame, false);
  }

  @Test
  public void storeRawStationDataFrameStorageUnavailableTest() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeRawStationDataFrame(any());
    RawStationDataFrame frame = TestFixtures.frame1;
    TestUtilities.testStoreValidObject(
        Endpoints.storeRawStationDataFrameUrl(host, config.port),
        frame, false, "application/text", HttpStatus.SC_SERVICE_UNAVAILABLE);
    reset(mockGateway);
  }

  @Test
  public void storeRawStationDataFrameDataExistsTest() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeRawStationDataFrame(any());
    RawStationDataFrame frame = TestFixtures.frame1;
    TestUtilities.testStoreValidObject(
        Endpoints.storeRawStationDataFrameUrl(host, config.port),
        frame, false, "application/text", HttpStatus.SC_CONFLICT);
    reset(mockGateway);
  }

  @Test
  public void getStationForNameTest() throws Exception {
    when(mockGateway.getStationId(TestFixtures.STATION_NAME))
        .thenReturn(Optional.of(TestFixtures.STATION_ID));
    // Test that a valid response can be returned
    TestUtilities.testGet(
        Endpoints.getStationIdByNameUrl(host, config.port),
        Map.of("station-name", TestFixtures.STATION_NAME),
        TestFixtures.STATION_ID, UUID.class);

    // Test that a null response can be returned
    when(mockGateway.getStationId("fake name"))
        .thenReturn(Optional.empty());
    TestUtilities.testGet(
        Endpoints.getStationIdByNameUrl(host, config.port),
        Map.of("station-name", "fake name"),
        null, UUID.class);
  }

  @Test
  public void getStationForNameStorageUnavailableTest() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).getStationId(any());
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        Endpoints.getStationIdByNameUrl(host, config.port),
        Map.of("station-name", TestFixtures.STATION_NAME),
        String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatus());
    reset(mockGateway);
  }

  /**
   * Tests that retrieving a ChannelID
   */
  @Test
  public void getChanIdForNameAndSiteTest() throws Exception {
    when(mockGateway
        .getChannelId(TestFixtures.SITE_NAME, TestFixtures.CHAN_NAME, TestFixtures.SEGMENT_START))
        .thenReturn(Optional.of(TestFixtures.PROCESSING_CHANNEL_ID));

    // Test that a valid response can be returned
    TestUtilities.testGet(
        Endpoints.getChannelIdByNameUrl(host, config.port),
        Map.of("site-name", TestFixtures.SITE_NAME,
            "channel-name", TestFixtures.CHAN_NAME, "time", TestFixtures.SEGMENT_START.toString()),
        TestFixtures.PROCESSING_CHANNEL_ID, UUID.class);

    // Test that a null response can be returned
    when(mockGateway.getChannelId("fake name",
        TestFixtures.CHAN_NAME, TestFixtures.SEGMENT_START))
        .thenReturn(Optional.empty());
    TestUtilities.testGet(
        Endpoints.getChannelIdByNameUrl(host, config.port),
        Map.of("site-name", "fake name",
            "channel-name", TestFixtures.CHAN_NAME, "time",
            TestFixtures.SEGMENT_START.toString()),
        null, UUID.class);
  }

  @Test
  public void getChanIdForNameAndSiteStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException()).when(mockGateway)
        .getChannelId(any(), any(), any());

    HttpResponse<String> response = UnirestTestUtilities.getJson(
        Endpoints.getChannelIdByNameUrl(host, config.port),
        Map.of("site-name", TestFixtures.SITE_NAME,
            "channel-name", TestFixtures.CHAN_NAME, "time", TestFixtures.SEGMENT_START.toString()),
        String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatus());
    reset(mockGateway);
  }

}
