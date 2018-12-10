package gms.core.waveformqc.waveformqccontrol.control;

import static gms.core.waveformqc.waveformqccontrol.util.TestUtility.buildCommand;
import static gms.core.waveformqc.waveformqccontrol.util.TestUtility.createMockWaveformQcChannelSohStatus;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.waveformqc.channelsohqc.plugin.ChannelSohQcPluginComponent;
import gms.core.waveformqc.waveformqccontrol.mock.MockWaveformQcPluginComponent;
import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcConfiguration;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.InvokeInputDataMap;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.OsdGatewayClient;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry.Entry;
import gms.core.waveformqc.waveformqccontrol.util.TestUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link WaveformQcControl}
 */
@RunWith(MockitoJUnitRunner.class)
public class WaveformQcControlComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformQcPluginRegistry registry;

  /**
   * Access library makes services calls to the OSD Gateway so it needs to be mocked in unit tests
   */
  @Mock
  private OsdGatewayClient gatewayClient;

  @Mock
  private WaveformQcConfiguration configuration;

  private WaveformQcControl control;

  @Before
  public void setUp() {
    control = new WaveformQcControl(registry, gatewayClient);
  }

  @After
  public void tearDown() {
    control = null;
  }

  /**
   * Mock the plugin registry, ensuring it will return two registered plugins.
   */
  private void mockRegistry() {
    WaveformQcPlugin plugin1 = MockWaveformQcPluginComponent.from();
    WaveformQcPlugin plugin2 = new ChannelSohQcPluginComponent();

    RegistrationInfo registration1 = RegistrationInfo.from(plugin1.getName(), plugin1.getVersion());
    RegistrationInfo registration2 = RegistrationInfo.from(plugin2.getName(), plugin2.getVersion());

    WaveformQcPluginRegistry.Entry entry1 = Entry.create(
        registration1, plugin1);

    WaveformQcPluginRegistry.Entry entry2 = Entry.create(
        registration2, plugin2);

    given(registry.entrySet())
        .willReturn(Set.of(entry1, entry2));

    given(registry.lookup(registration1))
        .willReturn(Optional.of(plugin1));

    given(registry.lookup(registration2))
        .willReturn(Optional.of(plugin2));
  }

  /**
   * Mock configuration, ensuring it will return the two registered plugins for the appropriate
   * processing channel ids.
   *
   * @param processingChannelId ProcessingChannel UUID used for the channel SOH plugin.
   */
  private void mockConfiguration(UUID processingChannelId) {
    given(configuration.createParameters(new UUID(0L, 0L)))
        .willReturn(Optional.of(WaveformQcParameters.create(new UUID(0L, 0L), Collections
            .singletonList(RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0))))));

    given(configuration.createParameters(processingChannelId))
        .willReturn(Optional.of(WaveformQcParameters.create(processingChannelId, Collections
            .singletonList(
                RegistrationInfo.from("channelSohQcPlugin", PluginVersion.from(1, 0, 0))))));
  }

  /**
   * Mock Gateway client, ensuring
   */
  private void mockGatewayClient(Set<UUID> processingChannelIds, WaveformQcChannelSohStatus status1,
      WaveformQcChannelSohStatus status2) {

    //load configuration
    given(gatewayClient.loadConfiguration())
        .willReturn(configuration);

    given(gatewayClient.loadPluginConfiguration(any()))
        .willReturn(TestUtility.TEST_PLUGIN_CONFIGURATION);

    given(gatewayClient.loadInvokeInputData(
        processingChannelIds, Instant.MIN, Instant.MAX))
        .willReturn(InvokeInputDataMap
            .create(Collections.emptySet(), Collections.emptySet(),
                new HashSet<>(Arrays.asList(status1, status2))));
  }

  /**
   * Validates that mock and ChannelSohQcPlugin operate as expected with the framework of
   * the WaveformQcControl exeucte method.
   */
  @Test
  public void testExecuteChannelSohQcPlugin() {
    //any non-nil id will get us the channel soh plugin
    final UUID processingChannelId = UUID.randomUUID();

    Set<UUID> processingChannelIds = Set.of(processingChannelId,
        new UUID(0L, 0L));

    WaveformQcChannelSohStatus status1 = createMockWaveformQcChannelSohStatus(
        true, 10, processingChannelId, QcMaskType.TIMING,
        ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD);

    //test plugin configuration turns off this subtype, so we shouldn't create a mask
    WaveformQcChannelSohStatus status2 = createMockWaveformQcChannelSohStatus(
        true, 10, processingChannelId, QcMaskType.TIMING,
        ChannelSohSubtype.GPS_RECEIVER_UNLOCKED);

    mockRegistry();
    mockConfiguration(processingChannelId);
    mockGatewayClient(processingChannelIds, status1, status2);

    control.initialize();

    List<QcMask> qcMasks = control.execute(buildCommand(processingChannelIds));

    verify(gatewayClient, times(1))
        .store(anyList(), any(), any());

    // we should have 6 QcMasks, 1 for the second processing channel (mock). and 5 for
    // the ChannelSohQcPlugin given the input WaveformQcChannelSohStatus
    assertEquals(6, qcMasks.size());

    // mock was called once, so one should be from mock
    assertEquals(1, qcMasks.stream()
        .filter(Predicate.isEqual(MockWaveformQcPluginComponent.mockQcMask()))
        .count());

    for (QcMask qcMask : qcMasks) {
      if (qcMask.getProcessingChannelId() == processingChannelId) {
        Assert
            .assertEquals(status1.getProcessingChannelId(), qcMask.getProcessingChannelId());
        Assert.assertEquals(ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD.getRationale(),
            qcMask.getCurrentQcMaskVersion().getRationale());
      }
    }
  }
}
