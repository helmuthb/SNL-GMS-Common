package gms.core.signaldetection.signaldetectorcontrol.osdgateway.client;


import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.core.signaldetection.signaldetectorcontrol.http.ContentType;
import gms.core.signaldetection.signaldetectorcontrol.http.ObjectSerialization;
import gms.core.signaldetection.signaldetectorcontrol.objects.PluginVersion;
import gms.core.signaldetection.signaldetectorcontrol.objects.RegistrationInfo;
import gms.core.signaldetection.signaldetectorcontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.signaldetection.signaldetectorcontrol.objects.dto.StoreSignalDetectionsDto;
import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OsdGatewayClientTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private static final String HOST = "localhost";
  private static final String BASE_URL = "/signal-detection/signal-detector-control/osd-gateway";

  private OsdGatewayClient gatewayClient;
  private int port;

  @Before
  public void setUp() {
    port = instanceRule.port();
    gatewayClient = OsdGatewayClient.create(HttpClientConfiguration.create(HOST,
        port, BASE_URL));
  }

  @After
  public void tearDown() {
    gatewayClient = null;
  }

  @Test
  public void testCreate() {
    HttpClientConfiguration config = mock(HttpClientConfiguration.class);
    given(config.getBasePath()).willReturn(BASE_URL);
    given(config.getHost()).willReturn(HOST);
    given(config.getPort()).willReturn(port);

    OsdGatewayClient client = OsdGatewayClient.create(config);
    assertNotNull(client);
  }

  @Test
  public void testCreateNullConfigExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create OsdGatewayClient with null httpClientConfiguration");
    OsdGatewayClient.create(null);
  }

  @Test
  public void testLoadChannelSegmentsNullChannelSegmentsExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke loadChannelSegments with null channelIds");
    gatewayClient.loadChannelSegments(null, Instant.now(), Instant.now());
  }

  @Test
  public void testLoadChannelSegmentsNullStartTimeExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke loadChannelSegments with null startTime");
    gatewayClient.loadChannelSegments(List.of(), null, Instant.now());
  }

  @Test
  public void testLoadChannelSegmentsNullEndTimeExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke loadChannelSegments with null endTime");
    gatewayClient.loadChannelSegments(List.of(), Instant.now(), null);
  }

  @Test
  public void testLoadChannelSegmentsOutOfOrderTimesThrowsIllegalArgumentException() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.minus(Duration.ofMillis(10));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot invoke loadChannelSegments with endTime before startTime");
    gatewayClient.loadChannelSegments(List.of(UUID.randomUUID()), startTime, endTime);
  }

  @Test
  public void testLoadChannelSegments() {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channel1 = UUID.randomUUID();
    final UUID channel2 = UUID.randomUUID();
    final Collection<UUID> channelUuids = List.of(channel1, channel2);
    final byte[] requestBody = ObjectSerialization
        .writeJson(new InvokeInputDataRequestDto(channelUuids, startTime, endTime));

    final ChannelSegment out1 = createMockChannelSegment(channel1, startTime, endTime);
    final ChannelSegment out2 = createMockChannelSegment(channel2, startTime, endTime);

    // Post json requestBody to the /invoke-input-data endpoint; return messagepack ChannelSegments
    givenThat(post(urlEqualTo(BASE_URL + "/invoke-input-data"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/msgpack")
            .withBody(ObjectSerialization.writeMessagePack(List.of(out1, out2)))));

    final Collection<ChannelSegment> actualChannelSegments = gatewayClient
        .loadChannelSegments(channelUuids, startTime, endTime);

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_URL + "/invoke-input-data"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody)));

    // Make sure the correct ChannelSegments come back
    assertEquals(2, actualChannelSegments.size());
    assertTrue(actualChannelSegments.containsAll(List.of(out1, out2)));
  }

  @Test
  public void testLoadChannelSegmentsWrongResponseTypeExpectIllegalStateException() {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channel1 = UUID.randomUUID();
    final Collection<UUID> channelUuids = List.of(channel1);
    final byte[] requestBody = ObjectSerialization
        .writeJson(new InvokeInputDataRequestDto(channelUuids, startTime, endTime));

    // Post json requestBody to the /invoke-input-data endpoint; return server error
    final ChannelSegment out1 = createMockChannelSegment(channel1, startTime, endTime);
    givenThat(post(urlEqualTo(BASE_URL + "/invoke-input-data"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody(ObjectSerialization.writeMessagePack(List.of(out1)))));

    exception.expect(IllegalStateException.class);
    exception.expectMessage("Expected Content-Type: application/msgpack but server provided text/plain");
    gatewayClient.loadChannelSegments(channelUuids, startTime, endTime);
  }

  @Test
  public void testLoadChannelSegmentsFailureExpectIllegalStateException() throws Exception {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channel1 = UUID.randomUUID();
    final Collection<UUID> channelUuids = List.of(channel1);
    final byte[] requestBody = ObjectSerialization
        .writeJson(new InvokeInputDataRequestDto(channelUuids, startTime, endTime));

    // Post json requestBody to the /invoke-input-data endpoint; return server error
    final String errorMsg = "signal-detector-control osd-gateway could not service loadChannelSegments request";
    givenThat(post(urlEqualTo(BASE_URL + "/invoke-input-data"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody(errorMsg)));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(errorMsg);
    gatewayClient.loadChannelSegments(channelUuids, startTime, endTime);
  }

  private ChannelSegment createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {
    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegmentType.ACQUIRED, startTime, endTime,
            new TreeSet<>(), CreationInfo.DEFAULT);
  }

  @Test
  public void testLoadStaLtaPowerDetectorPluginConfiguration() {
    PluginConfiguration pluginConfiguration = gatewayClient
        .loadPluginConfiguration(
            RegistrationInfo.from("staLtaPowerDetectorPlugin", PluginVersion.from(1, 0, 0)));
  }

  @Test
  public void testLoadPluginConfigurationNullRegInfoExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("loadPluginConfiguration require non-null RegistrationInfo");
    gatewayClient.loadPluginConfiguration(null);
  }

  @Test
  public void testStorePrivateVisibility() {
    testStore(StorageVisibility.PRIVATE);
  }

  @Test
  public void testStorePublicVisibility() {
    testStore(StorageVisibility.PUBLIC);
  }

  private void testStore(StorageVisibility storageVisibility) {
    final Set<SignalDetection> signalDetections = Set.of(
        TestFixtures.randomSignalDetection(), TestFixtures.randomSignalDetection());

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(new StoreSignalDetectionsDto(signalDetections, storageVisibility));

    // Post MessagePack requestBody to the /store endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()));

    assertTrue(gatewayClient.store(signalDetections, List.of(), storageVisibility));

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody)));
  }

  @Test
  public void testStoreFailureReturnsFalse() throws Exception {
    final Set<SignalDetection> signalDetections = Set.of(
        TestFixtures.randomSignalDetection(), TestFixtures.randomSignalDetection());

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(new StoreSignalDetectionsDto(signalDetections, StorageVisibility.PUBLIC));

    // Post MessagePack requestBody to the /store endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody("Could not store SignalDetection"))
    );

    assertFalse(gatewayClient.store(signalDetections, List.of(), StorageVisibility.PUBLIC));
  }

  @Test
  public void testStoreNullSignalDetectionsExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("OsdGatewayClient store requires non-null signalDetections");
    gatewayClient.store(null, List.of(), StorageVisibility.PRIVATE);
  }

  @Test
  public void testStoreNullCreationInformationsExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("OsdGatewayClient store requires non-null creationInformations");
    gatewayClient.store(Set.of(), null, StorageVisibility.PRIVATE);
  }

  @Test
  public void testStoreNullStorageVisibilityExpectNullPointerException()
      throws IllegalAccessException {

    exception.expect(NullPointerException.class);
    exception.expectMessage("OsdGatewayClient store requires non-null storageVisibility");
    gatewayClient.store(Set.of(), List.of(), null);
  }
}
