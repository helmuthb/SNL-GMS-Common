package gms.dataacquisition.cssloader.osdgateway.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mashape.unirest.http.HttpResponse;
import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.dataacquisition.cssloader.Endpoints;
import gms.dataacquisition.cssloader.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.cssloader.osdgateway.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;
import java.util.Set;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Tests route handlers
 */
public class CssLoaderOsdGatewayServiceTest {

  private static CssLoaderOsdGatewayInterface mockGateway = mock(CssLoaderOsdGatewayInterface.class);
  private static Configuration config;
  private static final String host = "localhost";

  @BeforeClass
  public static void setup() {
    config = ConfigurationLoader.load();
    CssLoaderOsdGatewayService.startService(mockGateway, config);
  }

  @AfterClass
  public static void teardown() {
    CssLoaderOsdGatewayService.stopService();
  }

  /**
   * Tests that storing a valid ChannelSegment can return true or false, depending on the return
   * value of the call to the OSD gateway in the service.
   */
  @Test
  public void testStoreValidChannelSegment() throws Exception {
    Set<ChannelSegment> channelSegmentSet = Set.of(TestFixtures.channelSegment);
    doNothing().when(mockGateway).storeChannelSegments(channelSegmentSet);
    TestUtilities.testStoreValidObject(
        Endpoints.storeChannelSegmentsUrl(config.port, host),
        channelSegmentSet, true);
  }

  @Test
  public void testStoreChannelSegmentStorageUnavailable() throws Exception {
    Set<ChannelSegment> channelSegmentSet = Set.of(TestFixtures.channelSegment);
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeChannelSegments(any());
    TestUtilities.testStoreValidObject(Endpoints.storeChannelSegmentsUrl(config.port, host),
        channelSegmentSet, true, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreChannelSegmentsDataExists() throws Exception {
    Set<ChannelSegment> channelSegmentSet = Set.of(TestFixtures.channelSegment);
    doThrow(new DataExistsException())
        .when(mockGateway).storeChannelSegments(channelSegmentSet);
    TestUtilities.testStoreValidObject(Endpoints.storeChannelSegmentsUrl(config.port, host),
        channelSegmentSet, true, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  /**
   * Tests that storing a valid ChannelStatesOfHealth can return true or false, depending on the
   * return value of the call to the OSD gateway in the service.
   */
  @Test
  public void testStoreChannelStatesOfHealth() throws Exception {
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.acquiredChannelSohBoolean);

    doNothing().when(mockGateway).storeChannelStatesOfHealth(sohs);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSohBooleanUrl(config.port, host),
        sohs, false);
  }

  @Test
  public void testStoreStoreChannelStatesOfHealthStorageUnavailable() throws Exception {
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.acquiredChannelSohBoolean);
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeChannelStatesOfHealth(any());
    TestUtilities.testStoreValidObject(Endpoints.storeSohBooleanUrl(config.port, host),
        sohs, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreStoreChannelStatesOfHealthDataExists() throws Exception {
    Set<AcquiredChannelSohBoolean> sohs = Set.of(TestFixtures.acquiredChannelSohBoolean);
    doThrow(new DataExistsException())
        .when(mockGateway).storeChannelStatesOfHealth(sohs);
    TestUtilities.testStoreValidObject(Endpoints.storeSohBooleanUrl(config.port, host),
        sohs, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  /**
   * Tests that retrieving a ChannelID by query parameter
   */
  @Test
  public void testRetrieveChannelIDBadQueryParameter() throws Exception {
    when(mockGateway.idForChannel("sitename", "channelname", TestFixtures.SEGMENT_START))
        .thenReturn(TestFixtures.TEST_UUID);

    HttpResponse<UUID> response = UnirestTestUtilities.getJson(
        Endpoints.retreiveChannelIdUrl(config.port, host) +
            "?site-name=sitename" +
            "&channnel-name=channelname" + "&time=" + "1970-01-02T03:04:05.123456Z",
        UUID.class);

    assertNotNull(response);
    assertEquals(null, response.getBody());
  }

  /**
   * Tests that retrieving a ChannelID
   */
  @Test
  public void testRetrieveChannelId() throws Exception {
    when(mockGateway.idForChannel("sitename", "channelname", TestFixtures.SEGMENT_START))
        .thenReturn(TestFixtures.TEST_UUID);

    HttpResponse<UUID> response = UnirestTestUtilities.getJson(
        Endpoints.retreiveChannelIdUrl(config.port, host) +
            "?site-name=sitename"
        + "&channel-name=channelname" + "&time=" + TestFixtures.SEGMENT_START.toString(), UUID.class);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals(TestFixtures.TEST_UUID, response.getBody());
  }

  @Test
  public void testRetrieveChannelIdStorageUnavailable() throws Exception {
    String url = Endpoints.retreiveChannelIdUrl(config.port, host) +
        "?site-name=sitename"
        + "&channel-name=channelname" + "&time=" + TestFixtures.SEGMENT_START.toString();
    doThrow(new StorageUnavailableException())
        .when(mockGateway).idForChannel(any(), any(), any());
    TestUtilities.testGetWithException(url, HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  // Test 'store reference object' endpoints
  @Test
  public void testStoreChannel() throws Exception {
    ReferenceChannel channel = TestFixtures.channel;
    doNothing().when(mockGateway).storeChannel(channel);
    TestUtilities.testStoreValidObject(
        Endpoints.storeChannelUrl(config.port, host),
        channel, false);
  }

  @Test
  public void testStoreChannelStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeChannel(any());
    TestUtilities.testStoreValidObject(Endpoints.storeChannelUrl(config.port, host),
        TestFixtures.channel, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreChannelDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeChannel(TestFixtures.channel);
    TestUtilities.testStoreValidObject(Endpoints.storeChannelUrl(config.port, host),
        TestFixtures.channel, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreCalibration() throws Exception {
    ReferenceCalibration calibration = TestFixtures.calibration;
    doNothing().when(mockGateway).storeCalibration(calibration);
    TestUtilities.testStoreValidObject(
        Endpoints.storeCalibrationUrl(config.port, host),
        calibration, false);
  }

  @Test
  public void testStoreCalibrationStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeCalibration(any());
    TestUtilities.testStoreValidObject(Endpoints.storeCalibrationUrl(config.port, host),
        TestFixtures.calibration, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreCalibrationDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeCalibration(TestFixtures.calibration);
    TestUtilities.testStoreValidObject(Endpoints.storeCalibrationUrl(config.port, host),
        TestFixtures.calibration, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreResponse() throws Exception {
    ReferenceResponse response = TestFixtures.response;
    doNothing().when(mockGateway).storeResponse(response);
    TestUtilities.testStoreValidObject(
        Endpoints.storeResponseUrl(config.port, host),
        response, false);
  }

  @Test
  public void testStoreResponseStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeResponse(any());
    TestUtilities.testStoreValidObject(Endpoints.storeResponseUrl(config.port, host),
        TestFixtures.response, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreResponseDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeResponse(TestFixtures.response);
    TestUtilities.testStoreValidObject(Endpoints.storeResponseUrl(config.port, host),
        TestFixtures.response, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreSensor() throws Exception {
    ReferenceSensor sensor = TestFixtures.sensor;
    doNothing().when(mockGateway).storeSensor(sensor);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSensorUrl(config.port, host),
        sensor, false);
  }

  @Test
  public void testStoreSensorStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeSensor(any());
    TestUtilities.testStoreValidObject(Endpoints.storeSensorUrl(config.port, host),
        TestFixtures.sensor, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreSensorDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeSensor(TestFixtures.sensor);
    TestUtilities.testStoreValidObject(Endpoints.storeSensorUrl(config.port, host),
        TestFixtures.sensor, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreNetwork() throws Exception {
    ReferenceNetwork network = TestFixtures.network;
    doNothing().when(mockGateway).storeNetwork(network);
    TestUtilities.testStoreValidObject(
        Endpoints.storeNetworkUrl(config.port, host),
        network, false);
  }

  @Test
  public void testStoreNetworkStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeNetwork(any());
    TestUtilities.testStoreValidObject(Endpoints.storeNetworkUrl(config.port, host),
        TestFixtures.network, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreNetworkDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeNetwork(TestFixtures.network);
    TestUtilities.testStoreValidObject(Endpoints.storeNetworkUrl(config.port, host),
        TestFixtures.network, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreSite() throws Exception {
    ReferenceSite site = TestFixtures.site;
    doNothing().when(mockGateway).storeSite(site);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSiteUrl(config.port, host),
        site, false);
  }

  @Test
  public void testStoreSiteStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeSite(any());
    TestUtilities.testStoreValidObject(Endpoints.storeSiteUrl(config.port, host),
        TestFixtures.site, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreSiteDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeSite(TestFixtures.site);
    TestUtilities.testStoreValidObject(Endpoints.storeSiteUrl(config.port, host),
        TestFixtures.site, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreStation() throws Exception {
    ReferenceStation station = TestFixtures.station;
    doNothing().when(mockGateway).storeStation(station);
    TestUtilities.testStoreValidObject(
        Endpoints.storeStationUrl(config.port, host),
        station, false);
  }

  @Test
  public void testStoreStationStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeStation(any());
    TestUtilities.testStoreValidObject(Endpoints.storeStationUrl(config.port, host),
        TestFixtures.station, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreStationDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeStation(TestFixtures.station);
    TestUtilities.testStoreValidObject(Endpoints.storeStationUrl(config.port, host),
        TestFixtures.station, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreNetworkMemberships() throws Exception {
    Set<ReferenceNetworkMembership> memberships = TestFixtures.networkMemberships;

    doNothing().when(mockGateway).storeNetworkMemberships(memberships);
    TestUtilities.testStoreValidObject(
        Endpoints.storeNetworkMembershipsUrl(config.port, host),
        memberships, false);
  }

  @Test
  public void testStoreNetworkMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeNetworkMemberships(any());
    TestUtilities.testStoreValidObject(Endpoints.storeNetworkMembershipsUrl(config.port, host),
        TestFixtures.networkMemberships, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreNetworkMembershipsDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeNetworkMemberships(TestFixtures.networkMemberships);
    TestUtilities.testStoreValidObject(Endpoints.storeNetworkMembershipsUrl(config.port, host),
        TestFixtures.networkMemberships, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreStationMemberships() throws Exception {
    Set<ReferenceStationMembership> memberships = TestFixtures.stationMemberships;
    doNothing().when(mockGateway).storeStationMemberships(memberships);
    TestUtilities.testStoreValidObject(
        Endpoints.storeStationMembershipsUrl(config.port, host),
        memberships,false);
  }

  @Test
  public void testStoreStationMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeStationMemberships(any());
    TestUtilities.testStoreValidObject(Endpoints.storeStationMembershipsUrl(config.port, host),
        TestFixtures.stationMemberships, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreStationMembershipsDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeStationMemberships(TestFixtures.stationMemberships);
    TestUtilities.testStoreValidObject(Endpoints.storeStationMembershipsUrl(config.port, host),
        TestFixtures.stationMemberships, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  @Test
  public void testStoreSiteMemberships() throws Exception {
    Set<ReferenceSiteMembership> memberships = TestFixtures.siteMemberships;
    doNothing().when(mockGateway).storeSiteMemberships(memberships);
    TestUtilities.testStoreValidObject(
        Endpoints.storeSiteMembershipsUrl(config.port, host),
        memberships, false);
  }

  @Test
  public void testStoreSiteMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(mockGateway).storeSiteMemberships(any());
    TestUtilities.testStoreValidObject(Endpoints.storeSiteMembershipsUrl(config.port, host),
        TestFixtures.siteMemberships, false, "application/text", HttpStatus.SERVICE_UNAVAILABLE_503);
    reset(mockGateway);
  }

  @Test
  public void testStoreSiteMembershipsDataExists() throws Exception {
    doThrow(new DataExistsException())
        .when(mockGateway).storeSiteMemberships(TestFixtures.siteMemberships);
    TestUtilities.testStoreValidObject(Endpoints.storeSiteMembershipsUrl(config.port, host),
        TestFixtures.siteMemberships, false, "application/text", HttpStatus.CONFLICT_409);
    reset(mockGateway);
  }

  // Tests that post null requests

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullChannelSegmentBatch() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeChannelSegmentsUrl(config.port, host));
  }

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullChannelStatesOfHealth() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSohBooleanUrl(config.port, host));
  }

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullChannel() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeChannelUrl(config.port, host));
  }

  @Test
  public void testStoreNullCalibration() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeCalibrationUrl(config.port, host));
  }

  @Test
  public void testStoreNullResponse() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeResponseUrl(config.port, host));
  }

  @Test
  public void testStoreNullSensor() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSensorUrl(config.port, host));
  }

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullNetwork() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeNetworkUrl(config.port, host));
  }

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullSite() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSiteUrl(config.port, host));
  }

  /**
   * Tests that posting a null request to a specified endpoint, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreNullStation() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeStationUrl(config.port, host));
  }

  @Test
  public void testStoreNullNetworkMemberships() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeNetworkMembershipsUrl(config.port, host));
  }

  @Test
  public void testStoreNullStationMemberships() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeStationMembershipsUrl(config.port, host));
  }

  @Test
  public void testStoreNullSiteMemberships() throws Exception {
    TestUtilities.testPostNullRequest(
        Endpoints.storeSiteMembershipsUrl(config.port, host));
  }
}
