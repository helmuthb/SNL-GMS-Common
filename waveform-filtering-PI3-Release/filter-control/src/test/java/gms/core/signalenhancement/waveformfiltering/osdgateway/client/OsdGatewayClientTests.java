package gms.core.signalenhancement.waveformfiltering.osdgateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import gms.core.signalenhancement.waveformfiltering.http.ObjectSerialization;
import gms.core.signalenhancement.waveformfiltering.objects.RegistrationInfo;
import gms.core.signalenhancement.waveformfiltering.objects.dto.InvokeInputDataRequestDto;
import gms.core.signalenhancement.waveformfiltering.objects.dto.StoreChannelSegmentsDto;
import gms.core.signalenhancement.waveformfiltering.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
  private static final String BASE_URL = "/signal-enhancement/waveform-filtering/osd-gateway";

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
  public void testLoadChannelSegmentsNullArgumentsValidated()
      throws IllegalAccessException {
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

    TestUtilities.checkMethodValidatesNullArguments(gatewayClient, "loadChannelSegments",
        channelUuids, startTime, endTime);
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

  private ChannelSegment createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {
    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegmentType.ACQUIRED, startTime, endTime,
            new TreeSet<>(), CreationInfo.DEFAULT);
  }

  @Test
  public void testStoreNullArgumentsValidated() throws IllegalAccessException {
    TestUtilities.checkMethodValidatesNullArguments(gatewayClient, "store",
        List.of(), List.of(), StorageVisibility.PRIVATE);
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
  public void testLoadFirFilterPluginConfiguration() {
  }

  @Test
  public void testLoadPluginConfiguration() {
    assertEquals(PluginConfiguration.from(Map.of()),
        gatewayClient.loadPluginConfiguration(mock(RegistrationInfo.class)));
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
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final List<ChannelSegment> channelSegments = List.of(
        createMockChannelSegment(UUID.randomUUID(), startTime, endTime),
        createMockChannelSegment(UUID.randomUUID(), startTime, endTime));

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(new StoreChannelSegmentsDto(channelSegments, storageVisibility));

    // Post MessagePack requestBody to the /store endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()));

    gatewayClient.store(channelSegments, List.of(), storageVisibility);

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody)));
  }
}
