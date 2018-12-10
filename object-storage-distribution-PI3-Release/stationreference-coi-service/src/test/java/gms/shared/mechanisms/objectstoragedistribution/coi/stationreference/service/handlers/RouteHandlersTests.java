package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import com.mashape.unirest.http.HttpResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.StationReferenceCoiService;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Endpoints;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.UnirestTestUtilities;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RouteHandlersTests {

  private static final Configuration configuration;
  private static final String BASE_URL; // set in static initializer

  private final String NETWORKS_URL = BASE_URL + Endpoints.NETWORKS;
  private final String STATIONS_URL = BASE_URL + Endpoints.STATIONS;
  private final String SITES_URL = BASE_URL + Endpoints.SITES;
  private final String CHANNELS_URL = BASE_URL + Endpoints.CHANNELS;
  private final String DIGITIZERS_URL = BASE_URL + Endpoints.DIGITIZERS;
  private final String SENSORS_URL = BASE_URL + Endpoints.SENSORS;
  private final String RESPONSES_URL = BASE_URL + Endpoints.RESPONSES;
  private final String CALIBRATIONS_URL = BASE_URL + Endpoints.CALIBRATIONS;
  private final String NETWORK_MEMBERSHIPS_URL = BASE_URL + Endpoints.NETWORK_MEMBERSHIPS;
  private final String STATION_MEMBERSHIPS_URL = BASE_URL + Endpoints.STATION_MEMBERSHIPS;
  private final String SITE_MEMBERSHIPS_URL = BASE_URL + Endpoints.SITE_MEMBERSHIPS;
  private final String DIGITIZER_MEMBERSHIPS_URL = BASE_URL + Endpoints.DIGITIZER_MEMBERSHIPS;

  private final UUID UNKNOWN_UUID = UUID.fromString("611e4cf1-4a3d-40a9-8e88-60a85cf76d67");
  private final String UNKNOWN_NAME = "unknownName";

  private static final StationReferenceRepositoryInterface repo
      = mock(StationReferenceRepositoryInterface.class);

  static {
    configuration = ConfigurationLoader.load();
    BASE_URL = "http://localhost:" + configuration.getPort() + configuration.getBaseUrl();
  }

  @Before
  public void setUp() throws Exception {
    reset(repo);
    // networks
    when(repo.retrieveNetworksByEntityId(any()))
        .thenReturn(List.of());
    when(repo.retrieveNetworksByEntityId(TestFixtures.network.getEntityId()))
        .thenReturn(List.of(TestFixtures.network));
    when(repo.retrieveNetworksByEntityId(TestFixtures.network2.getEntityId()))
        .thenReturn(List.of(TestFixtures.network2));
    when(repo.retrieveNetworks())
        .thenReturn(TestFixtures.allNetworks);
    // name queries
    when(repo.retrieveNetworksByName(any()))
        .thenReturn(List.of());
    when(repo.retrieveNetworksByName(TestFixtures.network.getName()))
        .thenReturn(List.of(TestFixtures.network));
    when(repo.retrieveNetworksByName(TestFixtures.network2.getName()))
        .thenReturn(List.of(TestFixtures.network2));
    // stations
    when(repo.retrieveStationsByEntityId(any()))
        .thenReturn(List.of());
    when(repo.retrieveStationsByEntityId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.station));
    when(repo.retrieveStationsByEntityId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.station2));
    when(repo.retrieveStationsByName(any()))
        .thenReturn(List.of());
    when(repo.retrieveStationsByName(TestFixtures.station.getName()))
        .thenReturn(List.of(TestFixtures.station));
    when(repo.retrieveStationsByName(TestFixtures.station2.getName()))
        .thenReturn(List.of(TestFixtures.station2));
    when(repo.retrieveStations())
        .thenReturn(TestFixtures.allStations);
    // sites
    when(repo.retrieveSitesByEntityId(any()))
        .thenReturn(List.of());
    when(repo.retrieveSitesByEntityId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.site));
    when(repo.retrieveSitesByEntityId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.site2));
    when(repo.retrieveSitesByName(any()))
        .thenReturn(List.of());
    when(repo.retrieveSitesByName(TestFixtures.site.getName()))
        .thenReturn(List.of(TestFixtures.site));
    when(repo.retrieveSitesByName(TestFixtures.site2.getName()))
        .thenReturn(List.of(TestFixtures.site2));
    when(repo.retrieveSites())
        .thenReturn(TestFixtures.allSites);
    // channels
    when(repo.retrieveChannelsByEntityId(any()))
        .thenReturn(List.of());
    when(repo.retrieveChannelsByEntityId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.channel));
    when(repo.retrieveChannelsByEntityId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.channel2));
    when(repo.retrieveChannelsByName(any()))
        .thenReturn(List.of());
    when(repo.retrieveChannelsByName(TestFixtures.channel.getName()))
        .thenReturn(List.of(TestFixtures.channel));
    when(repo.retrieveChannelsByName(TestFixtures.channel2.getName()))
        .thenReturn(List.of(TestFixtures.channel2));
    when(repo.retrieveChannels())
        .thenReturn(TestFixtures.allChannels);
    // calibrations
    when(repo.retrieveCalibrations())
        .thenReturn(TestFixtures.allCalibrations);
    when(repo.retrieveCalibrationsByChannelId(any()))
        .thenReturn(List.of());
    when(repo.retrieveCalibrationsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_calibrations);
    when(repo.retrieveCalibrationsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_calibrations);
    // responses
    when(repo.retrieveResponses())
        .thenReturn(TestFixtures.allResponses);
    when(repo.retrieveResponsesByChannelId(any()))
        .thenReturn(List.of());
    when(repo.retrieveResponsesByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_responses);
    when(repo.retrieveResponsesByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_responses);
    // sensors
    when(repo.retrieveSensors())
        .thenReturn(TestFixtures.allSensors);
    when(repo.retrieveSensorsByChannelId(any()))
        .thenReturn(List.of());
    when(repo.retrieveSensorsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_sensors);
    when(repo.retrieveSensorsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_sensors);
    // digitizers
    when(repo.retrieveDigitizersByEntityId(any()))
        .thenReturn(List.of());
    when(repo.retrieveDigitizersByEntityId(TestFixtures.digitizer.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizer));
    when(repo.retrieveDigitizersByEntityId(TestFixtures.digitizer2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizer2));
    when(repo.retrieveDigitizersByName(any()))
        .thenReturn(List.of());
    when(repo.retrieveDigitizersByName(TestFixtures.digitizer.getName()))
        .thenReturn(List.of(TestFixtures.digitizer));
    when(repo.retrieveDigitizersByName(TestFixtures.digitizer2.getName()))
        .thenReturn(List.of(TestFixtures.digitizer2));
    when(repo.retrieveDigitizers())
        .thenReturn(TestFixtures.allDigitizers);
    // network memberships
    when(repo.retrieveNetworkMembershipsByNetworkId(any()))
        .thenReturn(List.of());
    when(repo.retrieveNetworkMembershipsByNetworkId(TestFixtures.network.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(repo.retrieveNetworkMembershipsByNetworkId(TestFixtures.network2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(repo.retrieveNetworkMembershipsByStationId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(repo.retrieveNetworkMembershipsByStationId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(repo.retrieveNetworkMembershipsByNetworkAndStationId(
        TestFixtures.network.getEntityId(), TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(repo.retrieveNetworkMembershipsByNetworkAndStationId(
        TestFixtures.network2.getEntityId(), TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(repo.retrieveNetworkMemberships())
        .thenReturn(TestFixtures.allNetworkMemberships);
    // station memberships
    when(repo.retrieveStationMembershipsByStationId(any()))
        .thenReturn(List.of());
    when(repo.retrieveStationMembershipsByStationId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(repo.retrieveStationMembershipsByStationId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(repo.retrieveStationMembershipsBySiteId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(repo.retrieveStationMembershipsBySiteId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(repo.retrieveStationMembershipsByStationAndSiteId(
        TestFixtures.station.getEntityId(), TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(repo.retrieveStationMembershipsByStationAndSiteId(
        TestFixtures.station2.getEntityId(), TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(repo.retrieveStationMemberships())
        .thenReturn(TestFixtures.allStationMemberships);
    // site memberships
    when(repo.retrieveSiteMembershipsBySiteId(any()))
        .thenReturn(List.of());
    when(repo.retrieveSiteMembershipsBySiteId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(repo.retrieveSiteMembershipsBySiteId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(repo.retrieveSiteMembershipsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(repo.retrieveSiteMembershipsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(repo.retrieveSiteMembershipsBySiteAndChannelId(
        TestFixtures.site.getEntityId(), TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(repo.retrieveSiteMembershipsBySiteAndChannelId(
        TestFixtures.site2.getEntityId(), TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(repo.retrieveSiteMemberships())
        .thenReturn(TestFixtures.allSiteMemberships);
    // digitizer memberships
    when(repo.retrieveDigitizerMembershipsByDigitizerId(any()))
        .thenReturn(List.of());
    when(repo.retrieveDigitizerMembershipsByDigitizerId(TestFixtures.digitizer.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(repo.retrieveDigitizerMembershipsByDigitizerId(TestFixtures.digitizer2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(repo.retrieveDigitizerMembershipsByChannelId(any()))
        .thenReturn(List.of());
    when(repo.retrieveDigitizerMembershipsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(repo.retrieveDigitizerMembershipsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(repo.retrieveDigitizerMembershipsByDigitizerAndChannelId(
        TestFixtures.digitizer.getEntityId(), TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(repo.retrieveDigitizerMembershipsByDigitizerAndChannelId(
        TestFixtures.digitizer2.getEntityId(), TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(repo.retrieveDigitizerMemberships())
        .thenReturn(TestFixtures.allDigitizerMemberships);
  }

  @BeforeClass
  public static void runService() throws Exception {
    StationReferenceCoiService.startService(configuration, repo);
  }

  @AfterClass
  public static void teardown() {
    StationReferenceCoiService.stopService();
  }

  /**
   * Tests the 'networks' endpoint with no parameters.
   */
  @Test
  public void testQueryNetworksNoParams() throws Exception {
    testGetJson(NETWORKS_URL, TestFixtures.allNetworks);
  }

  @Test
  public void testQueryNetworksStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveNetworks();
    testGetJsonErrorResponse(NETWORKS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'networks' endpoint with an id parameter.
   */
  @Test
  public void testQueryNetworksById() throws Exception {
    String url = NETWORKS_URL + "/id/" + TestFixtures.network.getEntityId();
    testGetJson(url, List.of(TestFixtures.network));
    // make request with bad id, expect empty list and status OK back.
    url = NETWORKS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryNetworksByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveNetworksByEntityId(any());
    String url = NETWORKS_URL + "/id/" + TestFixtures.network.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'networks' endpoint with a name parameter.
   */
  @Test
  public void testQueryNetworksByName() throws Exception {
    String url = NETWORKS_URL + "/name/" + TestFixtures.network.getName();
    testGetJson(url, List.of(TestFixtures.network));
    url = NETWORKS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryNetworksByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveNetworksByName(any());
    String url = NETWORKS_URL + "/name/" + TestFixtures.network.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  @Test
  public void testQueryNetworksByStationName() throws Exception {
    String url = NETWORKS_URL + "?station-name=" + TestFixtures.stationName;
    testGetJson(url, List.of(TestFixtures.network));
    url = NETWORKS_URL + "?station-name=" + TestFixtures.stationName2;
    testGetJson(url, List.of(TestFixtures.network2));
    url = NETWORKS_URL + "?station-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'networks' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryNetworksByStartAndEndTime() throws Exception {
    // do a query for all networks, but within a time range that will exclude network 2.
    // assert that the result is only a list of network 1.
    String url = NETWORKS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.network.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.network));
  }

  /**
   * Tests the 'networks' endpoint with bad start time format
   * which should return a bad response.
   */
  @Test
  public void testQueryNetworksWithBadStartTime() throws Exception {
    String url = NETWORKS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceNetwork[]> response = UnirestTestUtilities.getJson(
        url, ReferenceNetwork[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceNetwork[] nets = response.getBody();
    assertNotNull(nets);
    assertEquals(0, nets.length);
  }

  /**
   * Tests the 'networks' endpoint with bad end time format
   * which should return a bad response.
   */
  @Test
  public void testQueryNetworksWithBadEndTime() throws Exception {
    String url = NETWORKS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceNetwork[]> response = UnirestTestUtilities.getJson(
        url, ReferenceNetwork[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceNetwork[] nets = response.getBody();
    assertNotNull(nets);
    assertEquals(0, nets.length);
  }

  /**
   * Tests the 'stations' endpoint with no parameters.
   */
  @Test
  public void testQueryStationNoParams() throws Exception {
    testGetJson(STATIONS_URL, TestFixtures.allStations);
  }

  @Test
  public void testQueryStationsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveStations();
    testGetJsonErrorResponse(STATIONS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint with an id parameter.
   */
  @Test
  public void testQueryStationById() throws Exception {
    String url = STATIONS_URL + "/id/" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.station));
    // make request with bad id, expect empty list and status OK back.
    url = STATIONS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveStationsByEntityId(any());
    String url = STATIONS_URL + "/id/" + TestFixtures.station.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint with a name parameter.
   */
  @Test
  public void testQueryStationsByName() throws Exception {
    String url = STATIONS_URL + "/name/" + TestFixtures.station.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveStationsByName(any());
    String url = STATIONS_URL + "/name/" + TestFixtures.station.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint providing argument 'network-name'.
   */
  @Test
  public void testQueryStationsByNetworkName() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.network.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsBySiteName() throws Exception {
    String url = STATIONS_URL + "?site-name=" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsByNetworkAndSiteName() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.network.getName()
        + "&site-name=" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName()
        + "&site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME
        + "&site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of());
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName()
        + "&site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME + "&site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'station' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryStationsByStartAndEndTime() throws Exception {
    // do a query for all station, but within a time range that will exclude station 2.
    // assert that the result is only a list of station 1.
    String url = STATIONS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.station.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.station));
  }

  /**
   * Tests the 'station' endpoint with bad start time format
   * which should return a bad response.
   */
  @Test
  public void testQueryStationsWithBadStartTime() throws Exception {
    String url = STATIONS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'station' endpoint with bad end time format
   * which should return a bad response.
   */
  @Test
  public void testQueryStationsWithBadEndTime() throws Exception {
    String url = STATIONS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'sites' endpoint with no parameters.
   */
  @Test
  public void testQuerySitesNoParams() throws Exception {
    testGetJson(SITES_URL, TestFixtures.allSites);
  }

  @Test
  public void testQuerySitesStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveSites();
    testGetJsonErrorResponse(SITES_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint with an id parameter.
   */
  @Test
  public void testQuerySitesById() throws Exception {
    String url = SITES_URL + "/id/" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.site));
    // make request with bad id, expect empty list and status OK back.
    url = SITES_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySitesByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveSitesByEntityId(any());
    String url = SITES_URL + "/id/" + TestFixtures.site.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint with a name parameter.
   */
  @Test
  public void testQuerySitesByName() throws Exception {
    String url = SITES_URL + "/name/" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySitesByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveSitesByName(any());
    String url = SITES_URL + "/name/" + TestFixtures.site.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint providing argument 'channel-id'.
   */
  @Test
  public void testQuerySitesByChannel() throws Exception {
    String url = SITES_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.site2));
    url = SITES_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites' endpoint with start and end time parameters.
   */
  @Test
  public void testQuerySitesByStartAndEndTime() throws Exception {
    // do a query for all sites, but within a time range that will exclude site 2.
    // assert that the result is only a list of site 1.
    String url = SITES_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.site.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.site));
  }

  /**
   * Tests the 'sites' endpoint with bad start time format
   * which should return a bad response.
   */
  @Test
  public void testQuerySitesWithBadStartTime() throws Exception {
    String url = SITES_URL + "?start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites' endpoint with bad end time format
   * which should return a bad response.
   */
  @Test
  public void testQuerySitesWithBadEndTime() throws Exception {
    String url = SITES_URL + "?end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'channels' endpoint with no parameters.
   */
  @Test
  public void testQueryChannelsNoParams() throws Exception {
    testGetJson(CHANNELS_URL, TestFixtures.allChannels);
  }

  @Test
  public void testQueryChannelsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveChannels();
    testGetJsonErrorResponse(CHANNELS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint with an id parameter.
   */
  @Test
  public void testQueryChannelsById() throws Exception {
    String url = CHANNELS_URL + "/id/" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    // make request with bad id, expect empty list and status OK back.
    url = CHANNELS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryChannelsByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveChannelsByEntityId(any());
    String url = CHANNELS_URL + "/id/" + TestFixtures.channel.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint with a name parameter.
   */
  @Test
  public void testQueryChannelsByName() throws Exception {
    String url = CHANNELS_URL + "/name/" + TestFixtures.channel.getName();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryChannelsByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveChannelsByName(any());
    String url = CHANNELS_URL + "/name/" + TestFixtures.channel.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint providing argument 'digitizer-id'.
   */
  @Test
  public void testQueryChannelsByDigitizerId() throws Exception {
    String url = CHANNELS_URL + "?digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint providing argument 'site-name'.
   */
  @Test
  public void testQueryChannelsBySite() throws Exception {
    String url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName;
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2;
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint providing arguments 'site-name' and 'digitizer-id'.
   */
  @Test
  public void testQueryChannelsBySiteAndDigitizer() throws Exception {
    String url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName
        + "&digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of());
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2
        + "&digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME
        + "&digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryChannelsByStartAndEndTime() throws Exception {
    // do a query for all channels, but within a time range that will exclude channel 2.
    // assert that the result is only a list of channel 1.
    String url = CHANNELS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.channel.getActualTime();
    testGetJson(url, List.of(TestFixtures.channel));
  }

  /**
   * Tests the 'channels' endpoint with bad start time format
   * which should return a bad response.
   */
  @Test
  public void testQueryChannelsWithBadStartTime() throws Exception {
    String url = CHANNELS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'channels' endpoint with bad end time format
   * which should return a bad response.
   */
  @Test
  public void testQueryChannelsWithBadEndTime() throws Exception {
    String url = CHANNELS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'digitizers' endpoint with no parameters.
   */
  @Test
  public void testQueryDigitizersNoParams() throws Exception {
    testGetJson(DIGITIZERS_URL, TestFixtures.allDigitizers);
  }

  @Test
  public void testQueryDigitizersStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveDigitizers();
    testGetJsonErrorResponse(DIGITIZERS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint with an id parameter.
   */
  @Test
  public void testQueryDigitizersById() throws Exception {
    String url = DIGITIZERS_URL + "/id/" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
    // make request with bad id, expect empty list and status OK back.
    url = DIGITIZERS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryDigitizersByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveDigitizersByEntityId(any());
    String url = DIGITIZERS_URL + "/id/" + TestFixtures.digitizer.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint with a name parameter.
   */
  @Test
  public void testQueryDigitizersByName() throws Exception {
    String url = DIGITIZERS_URL + "/name/" + TestFixtures.digitizer.getName();
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryDigitizersByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveDigitizersByName(any());
    String url = DIGITIZERS_URL + "/name/" + TestFixtures.digitizer.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint providing argument 'channel-id'.
   */
  @Test
  public void testQueryDigitizersByChannelId() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer2));
    url = DIGITIZERS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryDigitizersByStartAndEndTime() throws Exception {
    // do a query for all digitizers, but within a time range that will exclude digitizer 2.
    // assert that the result is only a list of digitizer 1.
    String url = DIGITIZERS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.digitizer.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.digitizer));
  }

  /**
   * Tests the 'digitizers' endpoint with bad start time format
   * which should return a bad response.
   */
  @Test
  public void testQueryDigitizersWithBadStartTime() throws Exception {
    String url = DIGITIZERS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceDigitizer[]> response = UnirestTestUtilities.getJson(
        url, ReferenceDigitizer[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceDigitizer[] digis = response.getBody();
    assertNotNull(digis);
    assertEquals(0, digis.length);
  }

  /**
   * Tests the 'digitizers' endpoint with bad end time format
   * which should return a bad response.
   */
  @Test
  public void testQueryDigitizersWithBadEndTime() throws Exception {
    String url = DIGITIZERS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceDigitizer[]> response = UnirestTestUtilities.getJson(
        url, ReferenceDigitizer[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceDigitizer[] digis = response.getBody();
    assertNotNull(digis);
    assertEquals(0, digis.length);
  }

  /**
   * Tests the 'stations by network' endpoint with a network
   * known to be linked to one station, and times.
   * First, time range [EPOCH, actualTime] is used (which finds TestFixtures.network),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryStationsByNetworkAndTimes() throws Exception {
    String url = STATIONS_URL + "?network-name="
        + TestFixtures.networkName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name="
        + TestFixtures.networkName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
    * Tests the 'stations by network' endpoint with a bad start time.
  */
  @Test
  public void testQueryStationsByNetworkAndBadStartTime() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.networkName
        + "&start-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'stations by network' endpoint with a bad end time.
   */
  @Test
  public void testQueryStationsByNetworkAndBadEndTime() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.networkName
        + "&end-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'stations by network' endpoint with an unknown name,
   * checks that no results are found.
   */
  @Test
  public void testQueryStationsByBadNetworkName() throws Exception {
    String url = STATIONS_URL + "?network-name="
        + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites by station' endpoint with a station
   * known to be linked to one site.
   */
  @Test
  public void testQuerySitesByStation() throws Exception {
    String url = SITES_URL + "?station-name="
        + TestFixtures.stationName;
    testGetJson(url, List.of(TestFixtures.site));
  }

  /**
   * Tests the 'sites by station' endpoint with a station
   * known to be linked to one site, and times.
   * First, time range [EPOCH, actualTime] is used (which finds TestFixtures.site),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQuerySitesByStationAndTimes() throws Exception {
    String url = SITES_URL + "?station-name="
        + TestFixtures.stationName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "?station-name="
        + TestFixtures.stationName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites by station' endpoint with a bad start time.
   */
  @Test
  public void testQuerySitesByStationAndBadStartTime() throws Exception {
    String url = SITES_URL + "?station-name=" + TestFixtures.stationName
        + "&start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites by station' endpoint with a bad end time.
   */
  @Test
  public void testQuerySitesByStationAndBadEndTime() throws Exception {
    String url = SITES_URL + "?station-name=" + TestFixtures.stationName
        + "&end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites by station' endpoint with an unknown name,
   * checks that no results are found.
   */
  @Test
  public void testQuerySitesByBadStationName() throws Exception {
    String url = SITES_URL + "?station-name="
        + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a channel
   * known to be linked to one digitizer.
   */
  @Test
  public void testQueryDigitizersByChannel() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a channel
   * known to be linked to one digitizer, and times.
   * First, time range [EPOCH, actualTime] is used (which finds TestFixtures.digitizer),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryDigitizersByChannelAndTimes() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId() + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId() + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a bad start time.
   */
  @Test
  public void testQueryDigitizersByChannelIdAndBadStartTime() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a bad end time.
   */
  @Test
  public void testQueryDigitizersByChannelAndBadEndTime() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'digitizers by channel' endpoint with an unknown id,
   * checks that no results are found.
   */
  @Test
  public void testQueryDigitizersByBadSiteName() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a digitizer
   * known to be linked to one channel, and times.
   * First, time range [EPOCH, actualTime] is used (which finds TestFixtures.channel),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryChannelsByDigitizerAndTimes() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name="
        + TestFixtures.digitName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?digitizer-name="
        + TestFixtures.digitName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a bad start time.
   */
  @Test
  public void testQueryChannelsByDigitizerAndBadStartTime() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name=" + TestFixtures.digitName
        + "&start-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a bad end time.
   */
  @Test
  public void testQueryChannelsByDigitizerAndBadEndTime() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name=" + TestFixtures.digitName
        + "&end-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests querying for channel calibrations.
   */
  @Test
  public void testQueryCalibrations() throws Exception {
    testGetJson(CALIBRATIONS_URL, TestFixtures.allCalibrations);
    String url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_calibrations);
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_calibrations);
    // time query: during time of first response, but before time of second.
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v1));
    // time query: after time of second response, only finds second response.
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v2));
    // time query: at time of first response to time of 3rd response, finds responses 1-3.
    url = CALIBRATIONS_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v1,
        TestFixtures.calibration_chan1_v2, TestFixtures.calibration_chan2_v1));
    // unknown channel
    url = CALIBRATIONS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryCalibrationsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveCalibrations();
    testGetJsonErrorResponse(CALIBRATIONS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for channel sensors.
   */
  @Test
  public void testQuerySensors() throws Exception {
    testGetJson(SENSORS_URL, TestFixtures.allSensors);
    String url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_sensors);
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_sensors);
    // time query: during time of first sensor, but before time of second.
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v1));
    // time query: after time of second sensor, only finds second sensor.
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v2));
    // time query: at time of first sensor to time of 3rd sensor, finds sensor 1-3.
    url = SENSORS_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v1,
        TestFixtures.sensor_chan1_v2, TestFixtures.sensor_chan2_v1));
    // unknown channel
    url = SENSORS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySensorsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveSensors();
    testGetJsonErrorResponse(SENSORS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for channel responses.
   */
  @Test
  public void testQueryResponses() throws Exception {
    testGetJson(RESPONSES_URL, TestFixtures.allResponses);
    String url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_responses);
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_responses);
    // time query: during time of first response, but before time of second.
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.response_chan1_v1));
    // time query: after time of second response, only finds second response.
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.response_chan1_v2));
    // time query: at time of first response to time of 3rd response, finds responses 1-3.
    url = RESPONSES_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.response_chan1_v1,
        TestFixtures.response_chan1_v2, TestFixtures.response_chan2_v1));
    // unknown channel
    url = RESPONSES_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryResponsesStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveResponses();
    testGetJsonErrorResponse(RESPONSES_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for network memberships.
   */
  @Test
  public void testNetworkMemberships() throws Exception {
    final String baseUrl = NETWORK_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allNetworkMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.netMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.netMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.netMember));
    // test filter by network-id
    url = baseUrl + "?network-id=" + TestFixtures.network.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?network-id=" + TestFixtures.network2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    url = baseUrl + "?network-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by station-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    url = baseUrl + "?station-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by network-id and station-id
    url = baseUrl + "?network-id=" + TestFixtures.network.getEntityId()
        + "&station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?network-id=" + TestFixtures.network2.getEntityId()
        + "&station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    // test with bad network-id
    url = baseUrl + "?network-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad station-id
    url = baseUrl + "?station-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryNetworkMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveNetworkMemberships();
    testGetJsonErrorResponse(NETWORK_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for station memberships.
   */
  @Test
  public void testStationMemberships() throws Exception {
    final String baseUrl = STATION_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allStationMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.stationMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.stationMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.stationMember));
    // test filter by station-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    url = baseUrl + "?station-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by site-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    url = baseUrl + "?site-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by station-id and site-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId()
        + "&site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId()
        + "&site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    // test with bad station-id
    url = baseUrl + "?station-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad site-id
    url = baseUrl + "?site-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryStationMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveStationMemberships();
    testGetJsonErrorResponse(STATION_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for site memberships.
   */
  @Test
  public void testSiteMemberships() throws Exception {
    final String baseUrl = SITE_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allSiteMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.siteMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.siteMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.siteMember));
    // test filter by site-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    url = baseUrl + "?site-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by channel-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    url = baseUrl + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by site-id and channel-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId()
        + "&channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId()
        + "&channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    // test with bad site-id
    url = baseUrl + "?site-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad channel-id
    url = baseUrl + "?channel-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQuerySiteMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveSiteMemberships();
    testGetJsonErrorResponse(SITE_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for digitizer memberships.
   */
  @Test
  public void testDigitizerMemberships() throws Exception {
    final String baseUrl = DIGITIZER_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allDigitizerMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    // test filter by channel-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    url = baseUrl + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by digitizer-id
    url = baseUrl + "?digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    url = baseUrl + "?digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by channel-id and digitizer-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId()
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    // test with bad channel-id
    url = baseUrl + "?channel-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad channel-id
    url = baseUrl + "?digitizer-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryDigitizerMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(repo).retrieveDigitizerMemberships();
    testGetJsonErrorResponse(DIGITIZER_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /////////////////////////////////////////////////
  /**
   * Tests the 'alive' endpoint.
   */
  @Test
  public void testGetAlive() throws Exception {
    String url = BASE_URL + "alive";

    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals(true, response.getBody().contains("alive at"));
  }

  private static void testGetJsonErrorResponse(String url, int expectedStatus) throws Exception {
    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);
    assertNotNull(response);
    assertEquals(expectedStatus, response.getStatus());
    assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/text");
  }

  private static void testGetJson(String url, Object expectedResponse) throws Exception {
    testGetJson(url, expectedResponse, HttpStatus.OK_200);
  }

  private static void testGetJson(String url, Object expectedResponse,
      int expectedStatus) throws Exception {
    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);
    assertNotNull(response);
    assertEquals(expectedStatus, response.getStatus());
    String expectedJson = TestFixtures.objectMapper.writeValueAsString(expectedResponse);
    assertEquals(expectedJson, response.getBody());
    assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
  }
}