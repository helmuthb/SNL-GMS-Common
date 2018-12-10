package gms.core.signaldetection.signaldetectorcontrol.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.core.signaldetection.signaldetectorcontrol.objects.PluginVersion;
import gms.core.signaldetection.signaldetectorcontrol.objects.RegistrationInfo;
import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorParameters;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.client.OsdGatewayClient;
import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.plugin.SignalDetectorControlPluginRegistry;
import gms.core.signaldetection.signaldetectorcontrol.plugin.SignalDetectorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

@RunWith(MockitoJUnitRunner.class)

public class SignalDetectorControlTests {

  private SignalDetectorControl signalDetectorControl;

  @Mock
  private SignalDetectorPlugin mockSignalDetectorPlugin1;
  @Mock
  private SignalDetectorPlugin mockSignalDetectorPlugin2;
  @Mock
  private OsdGatewayClient mockOsdGatewayClient;
  @Mock
  private SignalDetectorParameters mockSignalDetectorParameters;
  @Mock
  private SignalDetectorControlPluginRegistry mockSignalDetectorControlPluginRegistry;

  @Mock
  private SignalDetectorConfiguration mockSignalDetectorConfiguration;

  @Mock
  private SignalDetectorPlugin mockPlugin;

  @Before
  public void setup() {
    this.signalDetectorControl = SignalDetectorControl
        .create(mockSignalDetectorControlPluginRegistry, mockOsdGatewayClient);
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullRegistryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error creating SignalDetectorControl: registry cannot be null");
    SignalDetectorControl.create(null, mock(OsdGatewayClient.class));
  }

  @Test
  public void testCreateNullOsdGatewayAccessLibraryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating SignalDetectorControl: osdGatewayAccessLibrary cannot be null");
    SignalDetectorControl.create(new SignalDetectorControlPluginRegistry(), null);
  }

  @Test
  public void testCreate() {
    assertNotNull(
        SignalDetectorControl
            .create(new SignalDetectorControlPluginRegistry(), mock(OsdGatewayClient.class)));
  }

  @Test
  public void testInitialize() {
    RegistrationInfo mockInfo = RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0));

    //load mock configuration
    given(mockOsdGatewayClient.loadConfiguration())
        .willReturn(mockSignalDetectorConfiguration);
    given(mockSignalDetectorControlPluginRegistry.entrySet())
        .willReturn(Set.of(SignalDetectorControlPluginRegistry.Entry.create(mockInfo, mockPlugin)));

    given(mockOsdGatewayClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(PluginConfiguration.from(Map.of()));

    signalDetectorControl.initialize();

    verify(mockOsdGatewayClient, times(1)).loadConfiguration();
    verify(mockPlugin, times(1)).initialize(any(PluginConfiguration.class));
  }

  private static PluginConfiguration buildTestPluginConfiguration() {
    InputStream pluginConfigInputStream = SignalDetectorControlTests.class.getClassLoader()
        .getResourceAsStream(
            "gms/core/signaldetection/signaldetectorcontrol/osdgateway/client/sta_lta_power_detector_plugin_config.yaml"
        );

    Yaml yaml = new Yaml();
    Object pluginConfiguration = yaml.load(pluginConfigInputStream);
    return PluginConfiguration.from((Map<String, Object>) pluginConfiguration);
  }

  @Test
  public void testExecuteClaimCheckNullClaimCheckExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControl cannot execute a null ClaimCheck");
    signalDetectorControl.initialize();
    signalDetectorControl.execute((ExecuteClaimCheckCommand) null);
  }

  @Test
  public void testSignalDetectorControlExecuteClaimCheckBeforeInitializeExpectIllegalStateException() {

    exception.expect(IllegalStateException.class);
    exception.expectMessage("SignalDetectorControl must be initialized before execution");
    signalDetectorControl.execute(mock(ExecuteClaimCheckCommand.class));
  }

  @Test
  public void testExecuteClaimCheck() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);
    final UUID channelId = UUID.randomUUID();

    ChannelSegment channelSegment = TestFixtures.randomChannelSegment();

    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("mockDetector1", PluginVersion.from(1, 0, 0));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("mockDetector2", PluginVersion.from(1, 0, 0));
    List<RegistrationInfo> registrationInfos = List.of(registrationInfo1, registrationInfo2);

    given(mockSignalDetectorPlugin1.detectSignals((ChannelSegment) notNull()))
        .willReturn(List.of(start.plusSeconds(1000), start.plusSeconds(1060)));
    given(mockSignalDetectorPlugin1.getName()).willReturn("mockDetector1");
    given(mockSignalDetectorPlugin1.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockSignalDetectorControlPluginRegistry.lookup(registrationInfo1)).willReturn(
        Optional.of(mockSignalDetectorPlugin1));

    given(mockSignalDetectorPlugin2.detectSignals((ChannelSegment) notNull()))
        .willReturn(List.of(start.plusSeconds(1000), start.plusSeconds(1060)));
    given(mockSignalDetectorPlugin2.getName()).willReturn("mockDetector2");
    given(mockSignalDetectorPlugin2.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockSignalDetectorControlPluginRegistry.lookup(registrationInfo2)).willReturn(
        Optional.of(mockSignalDetectorPlugin2));

    given(mockOsdGatewayClient.loadConfiguration())
        .willReturn(mockSignalDetectorConfiguration);
    given(mockOsdGatewayClient
        .loadChannelSegments((Collection<UUID>) notNull(), (Instant) notNull(),
            (Instant) notNull())).willReturn(Set.of(channelSegment));
    given(mockSignalDetectorConfiguration.createParameters(channelSegment.getProcessingChannelId()))
        .willReturn(Optional.of(mockSignalDetectorParameters));
    given(mockSignalDetectorParameters.signalDetectorPlugins())
        .willReturn(registrationInfos.stream());

    signalDetectorControl.initialize();

    ExecuteClaimCheckCommand command = ExecuteClaimCheckCommand.create(
        channelId, start, end, ProcessingContext
            .createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                StorageVisibility.PRIVATE));

    Collection<UUID> signalDetectionUUIDs = signalDetectorControl.execute(command);

    verify(mockOsdGatewayClient, times(1))
        .loadChannelSegments(List.of(channelId), start, end);

    verify(mockOsdGatewayClient, times(1))
        .store(isNotNull(), isNotNull(), eq(StorageVisibility.PRIVATE));

    assertEquals(4, signalDetectionUUIDs.size());
  }

  @Test
  public void testExecuteClaimCheckNoParametersExpectIllegalArgumentException() throws Exception {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);
    final UUID channelId = UUID.randomUUID();

    ChannelSegment channelSegment = TestFixtures.randomChannelSegment();
    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockSignalDetectorConfiguration);
    given(mockOsdGatewayClient.loadChannelSegments(notNull(), notNull(), notNull()))
        .willReturn(Set.of(channelSegment));
    given(mockSignalDetectorConfiguration.createParameters(channelSegment.getProcessingChannelId()))
        .willReturn(Optional.empty());

    signalDetectorControl.initialize();

    exception.expect(IllegalStateException.class);
    exception.expectMessage("No SignalDetectorParameters for channel");
    signalDetectorControl.execute(ExecuteClaimCheckCommand.create(channelId, start, end,
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE)));
  }

  @Test
  public void testExecuteStreamingNullStreamingExpectNullPointerException() {
    signalDetectorControl.initialize();
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControl cannot execute a null StreamingCommand");
    signalDetectorControl.execute((ExecuteStreamingCommand) null);
  }

  @Test
  public void testSignalDetectorControlExecuteStreamingBeforeInitializeExpectIllegalStateException() {

    exception.expect(IllegalStateException.class);
    exception.expectMessage("SignalDetectorControl must be initialized before execution");
    signalDetectorControl.execute(mock(ExecuteStreamingCommand.class));
  }

  @Test
  public void testExecuteStreaming() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);
    ChannelSegment channelSegment = TestFixtures.randomChannelSegment();

    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("mockDetector1", PluginVersion.from(1, 0, 0));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("mockDetector2", PluginVersion.from(1, 0, 0));
    List<RegistrationInfo> registrationInfos = List.of(registrationInfo1, registrationInfo2);

    given(mockSignalDetectorPlugin1.detectSignals((ChannelSegment) notNull()))
        .willReturn(List.of(start.plusSeconds(1000), start.plusSeconds(1060)));
    given(mockSignalDetectorPlugin1.getName()).willReturn("mockDetector1");
    given(mockSignalDetectorPlugin1.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockSignalDetectorControlPluginRegistry.lookup(registrationInfo1)).willReturn(
        Optional.of(mockSignalDetectorPlugin1));

    given(mockSignalDetectorPlugin2.detectSignals((ChannelSegment) notNull()))
        .willReturn(List.of(start.plusSeconds(1000), start.plusSeconds(1060)));
    given(mockSignalDetectorPlugin2.getName()).willReturn("mockDetector2");
    given(mockSignalDetectorPlugin2.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockSignalDetectorControlPluginRegistry.lookup(registrationInfo2)).willReturn(
        Optional.of(mockSignalDetectorPlugin2));

    given(mockOsdGatewayClient.loadConfiguration())
        .willReturn(mockSignalDetectorConfiguration);
    given(mockSignalDetectorConfiguration.createParameters(channelSegment.getProcessingChannelId()))
        .willReturn(Optional.of(mockSignalDetectorParameters));
    given(mockSignalDetectorParameters.signalDetectorPlugins())
        .willReturn(registrationInfos.stream());

    signalDetectorControl.initialize();

    ExecuteStreamingCommand command = ExecuteStreamingCommand.create(
        channelSegment, start, end,
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PUBLIC));

    Collection<SignalDetection> signalDetections = signalDetectorControl.execute(command);

    verify(mockOsdGatewayClient, times(1))
        .store(isNotNull(), isNotNull(), eq(StorageVisibility.PUBLIC));

    //TODO: Check for appropriate size depending on test case
    assertEquals(4, signalDetections.size());
    //TODO: Check that correct UUIDs were returned
  }
}
