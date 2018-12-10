package gms.core.waveformqc.waveformqccontrol.control;

import static gms.core.waveformqc.waveformqccontrol.util.TestUtility.buildCommand;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.waveformqc.waveformqccontrol.mock.Mock2WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.mock.MockWaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcConfiguration;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.InvokeInputDataMap;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.OsdGatewayClient;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry;
import gms.core.waveformqc.waveformqccontrol.util.TestUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
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
public class WaveformQcControlTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformQcPluginRegistry pluginRegistry;

  @Mock
  private OsdGatewayClient gatewayClient;

  @Mock
  private WaveformQcConfiguration waveformQcConfiguration;

  private WaveformQcControl waveformQcControl;

  @Before
  public void setUp() {
    this.waveformQcControl = new WaveformQcControl(pluginRegistry,
        gatewayClient);
  }

  @After
  public void tearDown() {
    this.waveformQcControl = null;
  }

  @Test
  public void testInitialize() {
    RegistrationInfo mockInfo = RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0));
    WaveformQcPlugin mockPlugin = mock(MockWaveformQcPlugin.class);

    //load mock configuration
    given(gatewayClient.loadConfiguration())
        .willReturn(waveformQcConfiguration);
    given(pluginRegistry.entrySet())
        .willReturn(
            Set.of(WaveformQcPluginRegistry.Entry.create(mockInfo, mockPlugin)));
    given(gatewayClient.loadPluginConfiguration(mockInfo))
        .willReturn(TestUtility.TEST_PLUGIN_CONFIGURATION);

    waveformQcControl.initialize();
    verify(gatewayClient, times(1)).loadConfiguration();
    verify(mockPlugin, times(1)).initialize(TestUtility.TEST_PLUGIN_CONFIGURATION);
  }

  @Test
  public void testExecuteNoConfigurationExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcControl cannot execute with a null ServiceConfiguration");
    waveformQcControl.execute(buildCommand());
  }

  @Test
  public void testExecuteNoParametersForProcessingChannelIdExpectIllegalStateException() {

    //load mock configuration
    given(gatewayClient.loadConfiguration())
        .willReturn(waveformQcConfiguration);
    waveformQcControl.initialize();

    //set up no parameters returned from configuration
    UUID processingChannelId = UUID.randomUUID();
    Set<UUID> processingChannelIds = Collections.singleton(processingChannelId);

    //no parameters for processing channel
    given(waveformQcConfiguration.createParameters(processingChannelId))
        .willReturn(Optional.empty());

    //empty run with just the processing channel to try and load the parameters
    given(gatewayClient.loadInvokeInputData(
        processingChannelIds, Instant.MIN, Instant.MAX))
        .willReturn(InvokeInputDataMap
            .create(Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));

    exception.expect(IllegalStateException.class);
    exception
        .expectMessage("Cannot execute Waveform QC. Parameters not found for processing channel"
            + processingChannelId.toString());

    waveformQcControl.execute(buildCommand(processingChannelIds));
  }

  @Test
  public void testExecuteNoPluginInRegistryExpectIllegalStateException() {

    //load mock configuration
    given(gatewayClient.loadConfiguration())
        .willReturn(waveformQcConfiguration);
    waveformQcControl.initialize();

    //set up parameters and registry to not return a plugin
    RegistrationInfo badRegistrationInfo = RegistrationInfo
        .from("badName", PluginVersion.from(99, 99, -10000));
    UUID processingChannelId = UUID.randomUUID();
    Set<UUID> processingChannelIds = Collections.singleton(processingChannelId);
    WaveformQcParameters badParameters = WaveformQcParameters.create(processingChannelId,
        Collections.singletonList(badRegistrationInfo));

    //configuration always returns the badPlugin's information
    given(waveformQcConfiguration.createParameters(processingChannelId))
        .willReturn(Optional.of(badParameters));

    //registry does not have the bad plugin
    given(pluginRegistry.lookup(badRegistrationInfo))
        .willReturn(Optional.empty());

    //empty run with just the processing channel to try and load the plugin
    given(gatewayClient.loadInvokeInputData(
        processingChannelIds, Instant.MIN, Instant.MAX))
        .willReturn(InvokeInputDataMap
            .create(Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));

    exception.expect(IllegalStateException.class);
    exception
        .expectMessage(
            "Cannot execute Waveform QC. No plugins found for: " + Collections
                .singletonList(badRegistrationInfo).toString());

    waveformQcControl.execute(buildCommand(processingChannelIds));
  }

  @Test
  public void testExecute() {
    //load mock configuration
    given(gatewayClient.loadConfiguration())
        .willReturn(waveformQcConfiguration);
    waveformQcControl.initialize();

    //set up processing channels
    UUID processingChannelId1 = UUID.randomUUID();
    UUID processingChannelId2 = UUID.randomUUID();
    Set<UUID> processingChannelIds = new HashSet<>(
        Arrays.asList(processingChannelId1, processingChannelId2));

    //set up plugins
    RegistrationInfo registrationInfo1 = RegistrationInfo.from("mock",
        PluginVersion.from(1, 0, 0));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("mock2", PluginVersion.from(2, 1, 1));

    //set up configuration parameters
    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(processingChannelId1, Collections.singletonList(registrationInfo1));
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters.create(processingChannelId2,
        Arrays.asList(registrationInfo1, registrationInfo2));

    //configuration returns the right parameters for each processing channel
    given(waveformQcConfiguration.createParameters(processingChannelId1))
        .willReturn(Optional.of(waveformQcParameters1));
    given(waveformQcConfiguration.createParameters(processingChannelId2))
        .willReturn(Optional.of(waveformQcParameters2));

    //registry returns the right plugin for each parameter
    given(pluginRegistry.lookup(registrationInfo1))
        .willReturn(Optional.of(new MockWaveformQcPlugin()));
    given(pluginRegistry.lookup(registrationInfo2))
        .willReturn(Optional.of(new Mock2WaveformQcPlugin()));

    //run for the two processing ids, input data doesn't matter
    given(gatewayClient.loadInvokeInputData(
        processingChannelIds, Instant.MIN, Instant.MAX))
        .willReturn(InvokeInputDataMap
            .create(Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));

    List<QcMask> qcMasks = waveformQcControl.execute(buildCommand(processingChannelIds));

    //make sure store was called
    verify(gatewayClient, times(1))
        .store(anyList(), any(), any());

    //we should have 3 QcMasks, 1 for the first processing channel, 2 for the second.
    assertEquals(3, qcMasks.size());

    //mock1 should have been called twice, so two of these qcmasks should match its constant output
    assertEquals(2, qcMasks.stream()
        .filter(Predicate.isEqual(MockWaveformQcPlugin.mockQcMask()))
        .count());

    //mock2 should have been called once, so two of these qcmasks should match its constant output
    assertEquals(1, qcMasks.stream()
        .filter(Predicate.isEqual(Mock2WaveformQcPlugin.mockQcMask()))
        .count());
  }


  @Test
  public void testExecuteNullCommandExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcControl cannot execute a null command");
    waveformQcControl.execute(null);
  }

  @Test
  public void testConstructNullRegistryExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcControl requires a non-null WaveformQcPluginRegistry");
    new WaveformQcControl(null,
        gatewayClient);
  }

  @Test
  public void testConstructNullAccessLibraryExpectIllegalArgumentException() {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();

    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformQcControl requires a non-null OsdGatewayClient");
    new WaveformQcControl(registry, null);
  }
}

