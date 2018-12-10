package gms.core.waveformqc.waveformqccontrol.osdgateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.mashape.unirest.http.Unirest;
import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.waveformqc.waveformqccontrol.objects.dto.StoreQcMasksDto;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link OsdGatewayClient} using {@link @RestClientTest} to mock
 * the OSD Gateway service.
 */
public class OsdGatewayClientTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private static final String HOST = "localhost";
  private static final String BASE_URI = "/waveform-qc/waveform-qc-control/osd-gateway";

  private OsdGatewayClient gatewayClient;

  @BeforeClass
  public static void initialize() {
    Unirest.setObjectMapper(ObjectSerialization.getClientObjectMapper());
  }

  @AfterClass
  public static void shutdown() {
    Unirest.setObjectMapper(null);
  }

  @Before
  public void setUp() {
    gatewayClient = new OsdGatewayClient(HOST,
        instanceRule.port(), BASE_URI);
  }

  @After
  public void tearDown() {
    gatewayClient = null;
  }

  @Test
  public void testLoadMissingPluginConfigurationThrowsIllegalArgument() {
    RegistrationInfo missingPluginInfo = RegistrationInfo
        .from("notarealplugin", PluginVersion.from(1, 0, 0));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Can't find plugin configuration for " + missingPluginInfo);

    gatewayClient.loadPluginConfiguration(missingPluginInfo);
  }

  @Test
  public void testLoadChannelSohQcPluginConfiguration() {
    PluginConfiguration pluginConfiguration = gatewayClient
        .loadPluginConfiguration(
            RegistrationInfo.from("channelSohQcPlugin", PluginVersion.from(1, 0, 0)));

    Optional<Object> defaults = pluginConfiguration.getParameter("defaults");
    Optional<Object> overrides = pluginConfiguration.getParameter("overrides");

    assertTrue(defaults.isPresent());
    assertTrue(overrides.isPresent());

    assertTrue(Map.class.isAssignableFrom(defaults.get().getClass()));
    assertTrue(List.class.isAssignableFrom(overrides.get().getClass()));
  }

  @Test
  public void testLoadWaveformGapQcPluginConfiguration() {
    PluginConfiguration pluginConfiguration = gatewayClient
        .loadPluginConfiguration(
            RegistrationInfo.from("waveformGapQcPlugin", PluginVersion.from(1, 0, 0)));

    checkValueIsCorrectType(pluginConfiguration, "minLongGapLengthInSamples", Integer.class);
  }

  @Test
  public void testLoadWaveformRepeatedAmplitudeQcPluginConfiguration() {
    PluginConfiguration pluginConfiguration = gatewayClient
        .loadPluginConfiguration(
            RegistrationInfo
                .from("waveformRepeatedAmplitudeQcPlugin", PluginVersion.from(1, 0, 0)));

    checkValueIsCorrectType(pluginConfiguration, "minSeriesLengthInSamples", Integer.class);
    checkValueIsCorrectType(pluginConfiguration, "maxDeltaFromStartAmplitude", Double.class);
    checkValueIsCorrectType(pluginConfiguration, "maskMergeThresholdSeconds", Double.class);
  }

  @Test
  public void testLoadWaveformSpike3ptQcPluginConfiguration() {
    PluginConfiguration pluginConfiguration = gatewayClient
        .loadPluginConfiguration(
            RegistrationInfo
                .from("waveformSpike3PtQcPlugin", PluginVersion.from(1, 0, 0)));

    checkValueIsCorrectType(pluginConfiguration, "minConsecutiveSampleDifferenceSpikeThreshold",
        Double.class);
  }


  private <T> void checkValueIsCorrectType(PluginConfiguration pluginConfiguration,
      String configKey, Class<T> type) {

    Optional<Object> minSeriesLength = pluginConfiguration.getParameter(configKey);
    assertTrue(minSeriesLength.isPresent());
    assertTrue(type.isAssignableFrom(minSeriesLength.get().getClass()));
  }

  @Test
  public void testLoadInvokeInputDataNullArgumentsValidated()
      throws IllegalAccessException {

    // TODO: fix TestUtilites to limit setup in null validation tests
    UUID processingChannelId1 = UUID.randomUUID();
    UUID processingChannelId2 = UUID.randomUUID();

    WaveformQcChannelSohStatus sohStatus1 = sohStatus(processingChannelId1);
    WaveformQcChannelSohStatus sohStatus2 = sohStatus(processingChannelId2);

    final InvokeInputDataRequestDto requestDto = new InvokeInputDataRequestDto(
        Set.of(), Instant.MIN, Instant.MAX);

    givenThat(post(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/invoke-input-data"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(requestDto)))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(ObjectSerialization.writeValue(invokeInputData(sohStatus1,
                sohStatus2)))));

    // TODO: Once TestUtilities is fixed this test should only require this line
    TestUtilities.checkMethodValidatesNullArguments(gatewayClient, "loadInvokeInputData",
        Set.of(), Instant.MIN, Instant.MAX);
  }

  @Test
  public void testLoadInvokeInputDataOutOfOrderTimesThrowsIllegalArgumentException() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.minus(Duration.ofMillis(10));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot invoke loadInvokeInputData with endTime before startTime");
    gatewayClient.loadInvokeInputData(new HashSet<>(), startTime, endTime);
  }

  @Test
  public void testLoadInvokeInputData() {
    UUID processingChannelId1 = UUID.randomUUID();
    UUID processingChannelId2 = UUID.randomUUID();

    WaveformQcChannelSohStatus sohStatus1 = sohStatus(processingChannelId1);
    WaveformQcChannelSohStatus sohStatus2 = sohStatus(processingChannelId2);

    final InvokeInputDataRequestDto requestDto = new InvokeInputDataRequestDto(
        Set.of(processingChannelId1, processingChannelId2),
        Instant.MIN, Instant.MAX);

    givenThat(post(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/invoke-input-data"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(requestDto)))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(ObjectSerialization.writeValue(invokeInputData(sohStatus1,
                sohStatus2)))));

    InvokeInputDataMap invokeInputData = gatewayClient
        .loadInvokeInputData(Set.of(processingChannelId1, processingChannelId2), Instant.MIN,
            Instant.MAX);

    verify(1, postRequestedFor(
        urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/invoke-input-data"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("accept", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(requestDto))));

    assertFalse(invokeInputData.getQcMasks(processingChannelId1).isPresent());
    assertFalse(invokeInputData.getQcMasks(processingChannelId2).isPresent());

    assertFalse(invokeInputData.getChannelSegments(processingChannelId1).isPresent());
    assertFalse(invokeInputData.getChannelSegments(processingChannelId2).isPresent());

    assertTrue(invokeInputData.getWaveformQcChannelSohStatuses(processingChannelId1).isPresent());
    assertTrue(invokeInputData.getWaveformQcChannelSohStatuses(processingChannelId2).isPresent());

    assertThat(invokeInputData.getWaveformQcChannelSohStatuses(processingChannelId1).get(),
        is(Set.of(sohStatus1)));
    assertThat(invokeInputData.getWaveformQcChannelSohStatuses(processingChannelId2).get(),
        is(Set.of(sohStatus2)));
  }

  private static InvokeInputData invokeInputData(WaveformQcChannelSohStatus... sohStatuses) {
    return InvokeInputData.create(Set.of(), Set.of(), Set.of(sohStatuses));
  }

  private static WaveformQcChannelSohStatus sohStatus(UUID processingChannelId) {
    return WaveformQcChannelSohStatus.builder(processingChannelId,
        QcMaskType.STATION_SECURITY, ChannelSohSubtype.VAULT_DOOR_OPENED,
        Instant.MIN, Instant.MAX, true, Duration.ZERO).build();
  }

  @Test
  public void testStoreNullArgumentsValidated() throws IllegalAccessException {

    // TODO: fix TestUtilites to limit setup in null validation tests
    final StoreQcMasksDto storeDto = new StoreQcMasksDto(List.of(), List.of(),
        StorageVisibility.PRIVATE);

    givenThat(post(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/store"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(storeDto)))
        .willReturn(ok()));

    // TODO: Test should only require this line once TestUtilities is fixed
    TestUtilities
        .checkMethodValidatesNullArguments(gatewayClient, "store", List.of(), List.of(),
            StorageVisibility.PRIVATE);
  }

  @Test
  public void testStorePrivateVisibility() {
    StoreQcMasksDto storeDto = new StoreQcMasksDto(List.of(), List.of(), StorageVisibility.PRIVATE);

    givenThat(post(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/store"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(storeDto)))
        .willReturn(ok()));

    gatewayClient
        .store(storeDto.getQcMasks(), storeDto.getCreationInfos(), storeDto.getStorageVisibility());

    verify(1, postRequestedFor(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/store"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(storeDto))));
  }

  @Test
  public void testStorePublicVisibility() {
    StoreQcMasksDto storeDto = new StoreQcMasksDto(List.of(), List.of(), StorageVisibility.PUBLIC);

    givenThat(post(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/store"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(storeDto)))
        .willReturn(ok()));

    gatewayClient
        .store(storeDto.getQcMasks(), storeDto.getCreationInfos(), storeDto.getStorageVisibility());

    verify(1, postRequestedFor(urlEqualTo("/waveform-qc/waveform-qc-control/osd-gateway/store"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(ObjectSerialization.writeValue(storeDto))));
  }

}
