package gms.core.signalenhancement.waveformfiltering.control;

import static gms.core.signalenhancement.waveformfiltering.TestFixtures.createWaveform;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.signalenhancement.waveformfiltering.TestFixtures;
import gms.core.signalenhancement.waveformfiltering.objects.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.objects.FilterParameters;
import gms.core.signalenhancement.waveformfiltering.objects.PluginVersion;
import gms.core.signalenhancement.waveformfiltering.objects.RegistrationInfo;
import gms.core.signalenhancement.waveformfiltering.objects.StreamingFilterPluginParameters;
import gms.core.signalenhancement.waveformfiltering.osdgateway.client.OsdGatewayClient;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterControlPluginRegistry;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.core.signalenhancement.waveformfiltering.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
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

@RunWith(MockitoJUnitRunner.class)

public class FilterControlTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();
  @Mock
  private FilterPlugin mockFilterPlugin;
  @Mock
  private OsdGatewayClient mockOsdGatewayClient;
  @Mock
  private FilterConfiguration mockFilterConfiguration;
  @Mock
  private FilterParameters mockFilterParameters;
  @Mock
  private StreamingFilterPluginParameters mockStreamingFilterPluginParameters;
  @Mock
  private FilterControlPluginRegistry mockFilterControlPluginRegistry;

  private FilterControl filterControl;

  @Before
  public void setUp() {
    this.filterControl = FilterControl
        .create(mockFilterControlPluginRegistry, mockOsdGatewayClient);
  }

  @Test
  public void testCreateNullRegistryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error creating FilterControl: registry cannot be null");
    FilterControl.create(null, mock(OsdGatewayClient.class));
  }

  @Test
  public void testCreateNullOsdGatewayAccessLibraryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error creating FilterControl: osdGatewayAccessLibrary cannot be null");
    FilterControl.create(new FilterControlPluginRegistry(), null);
  }

  @Test
  public void testCreate() {
    assertNotNull(
        FilterControl.create(new FilterControlPluginRegistry(), mock(OsdGatewayClient.class)));
  }

  @Test
  public void testInitialize() {
    RegistrationInfo mockInfo = RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0));

    //load mock configuration
    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterControlPluginRegistry.entrySet())
        .willReturn(Set.of(FilterControlPluginRegistry.Entry.create(mockInfo, mockFilterPlugin)));

    given(mockOsdGatewayClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(PluginConfiguration.from(Map.of()));

    filterControl.initialize();

    verify(mockOsdGatewayClient, times(1)).loadConfiguration();
    verify(mockFilterPlugin, times(1)).initialize(any(PluginConfiguration.class));
  }

  @Test
  public void testExecuteClaimCheckNullClaimCheckExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControl cannot execute a null ClaimCheck");
    filterControl.initialize();
    filterControl.execute((ExecuteClaimCheckCommand) null);
  }

  @Test
  public void testFilterControlExecuteClaimCheckBeforeInitializeExpectIllegalStateException() {

    exception.expect(IllegalStateException.class);
    exception.expectMessage("FilterControl must be initialized before execution");
    filterControl.execute(mock(ExecuteClaimCheckCommand.class));
  }

  @Test
  public void testExecuteClaimCheckNoFilterParametersExpectIllegalArgumentException() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Filter configuration must define parameters");

    RegistrationInfo mockInfo = RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0));

    //load mock configuration
    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterControlPluginRegistry.entrySet())
        .willReturn(Set.of(FilterControlPluginRegistry.Entry.create(mockInfo, mockFilterPlugin)));

    given(mockOsdGatewayClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(PluginConfiguration.from(Map.of()));

    ProcessingContext processingContext = mock(ProcessingContext.class);

    filterControl.initialize();
    filterControl.execute(ExecuteClaimCheckCommand
        .create(Map.of(), UUID.randomUUID(), start, end, processingContext));
  }

  @Test
  public void testExecuteClaimCheckMissingPluginsExpectIllegalArgumentException() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);
    final UUID processingChannelStep = UUID.randomUUID();

    RegistrationInfo mockInfo = RegistrationInfo.from("mock",
        PluginVersion.from(1, 0, 0));

    final Map<RegistrationInfo, List<FilterDefinition>> pluginFilterDefinitions =
        Map.of(mockInfo, List.of(TestFixtures.getFilterDefinition()));

    //load mock configuration
    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterControlPluginRegistry.entrySet())
        .willReturn(Set.of(FilterControlPluginRegistry.Entry.create(mockInfo, mockFilterPlugin)));

    given(mockOsdGatewayClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(PluginConfiguration.from(Map.of()));

    ProcessingContext processingContext = mock(ProcessingContext.class);

    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterConfiguration.createParameters(processingChannelStep))
        .willReturn(Optional.of(mockFilterParameters));

    given(mockFilterParameters.getInfoListMap()).willReturn(pluginFilterDefinitions);

    // don't define mockFilterControlPluginRegistry.lookup(mockInfo)) and plugin will show up as
    // missing

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "Cannot execute Waveform Filtering. Missing plugins found for: " + List.of(mockInfo)
            .toString());

    filterControl.initialize();
    filterControl.execute(ExecuteClaimCheckCommand
        .create(Map.of(), processingChannelStep, start, end, processingContext));
  }

  @Test
  public void testExecuteClaimCheck() {
    final UUID outUuid1 = UUID.randomUUID();
    final UUID outUuid2 = UUID.randomUUID();
    final UUID processingChannelStep = UUID.randomUUID();

    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(7864);

    final Waveform waveform1 = createWaveform(start, end, 40.0);
    final Waveform waveform2 = createWaveform(end, end.plusSeconds(1000), 40.0);

    final Waveform waveform3 = createWaveform(end.plusSeconds(1000), end.plusSeconds(2000), 40.0);
    final Waveform waveform4 = createWaveform(end.plusSeconds(2000), end.plusSeconds(3000), 40.0);

    final ChannelSegment channelSegment1 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform1, waveform2));
    final ChannelSegment channelSegment2 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform3, waveform4));

    final Set<ChannelSegment> loadedChannelSegments = Set.of(channelSegment1, channelSegment2);
    final Map<UUID, UUID> inToOutChannelUuids = Map.of(
        channelSegment1.getProcessingChannelId(), outUuid1,
        channelSegment2.getProcessingChannelId(), outUuid2);

    final RegistrationInfo registrationInfo1 = RegistrationInfo.from("mockFilter",
        PluginVersion.from(1, 0, 0));

    final Map<RegistrationInfo, List<FilterDefinition>> pluginFilterDefinitions =
        Map.of(registrationInfo1, List.of(TestFixtures.getFilterDefinition()));

    // define processingContext so CreationInformation is created properly
    ProcessingContext processingContext = mock(ProcessingContext.class);
    given(processingContext.getStorageVisibility()).willReturn(StorageVisibility.PUBLIC);
    given(processingContext.getAnalystActionReference()).
        willReturn(Optional.of(AnalystActionReference
            .from(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())));
    given(processingContext.getProcessingStepReference()).
        willReturn(Optional.empty());

    given(mockOsdGatewayClient.loadChannelSegments(inToOutChannelUuids.keySet(), start, end))
        .willReturn(loadedChannelSegments);

    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterConfiguration.createParameters(processingChannelStep))
        .willReturn(Optional.of(mockFilterParameters));

    given(mockFilterParameters.getInfoListMap()).willReturn(pluginFilterDefinitions);

    //registry returns the right plugin for each parameter
    given(mockFilterControlPluginRegistry.lookup(registrationInfo1))
        .willReturn(Optional.of(mockFilterPlugin));

    given(mockFilterPlugin.getName()).willReturn("mockFilter");
    given(mockFilterPlugin.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockFilterPlugin.filter(channelSegment1, TestFixtures.getFilterDefinition()))
        .willReturn(List.of(waveform1, waveform2));
    given(mockFilterPlugin.filter(channelSegment2, TestFixtures.getFilterDefinition()))
        .willReturn(List.of(waveform3, waveform4));

    // Execute claim check command
    filterControl.initialize();
    List<UUID> newChannelSegmentChannelIds = filterControl.execute(ExecuteClaimCheckCommand
        .create(inToOutChannelUuids, processingChannelStep, start, end, processingContext));

    verify(mockOsdGatewayClient, times(1))
        .loadChannelSegments(inToOutChannelUuids.keySet(), start, end);

    verify(mockOsdGatewayClient, times(1))
        .store(isNotNull(), isNotNull(), eq(StorageVisibility.PUBLIC));

    // Validate two new ChannelSegments are returned with there channel ids set to outUuid1 and
    // outUuid2
    assertTrue(newChannelSegmentChannelIds.size() == 2);
    assertTrue(Set.of(newChannelSegmentChannelIds.get(0),
        newChannelSegmentChannelIds.get(1)).containsAll(List.of(outUuid1, outUuid2)));
  }

  @Test
  public void testExecuteStreamingNullStreamingExpectNullPointerException() {
    filterControl.initialize();
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControl cannot execute a null StreamingCommand");
    filterControl.execute((ExecuteStreamingCommand) null);
  }

  @Test
  public void testFilterControlExecuteStreamingBeforeInitializeExpectIllegalStateException() {

    exception.expect(IllegalStateException.class);
    exception.expectMessage("FilterControl must be initialized before execution");
    filterControl.execute(mock(ExecuteStreamingCommand.class));
  }

  @Test
  public void testExecuteStreamingNoPluginExpectIllegalStateException() {
    final UUID outUuid1 = UUID.randomUUID();
    final UUID outUuid2 = UUID.randomUUID();
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(1000);

    final Waveform waveform1 = createWaveform(start, end, 40.0);
    final Waveform waveform2 = createWaveform(end, end.plusSeconds(1000), 40.0);

    final Waveform waveform3 = createWaveform(end.plusSeconds(1000), end.plusSeconds(2000), 40.0);
    final Waveform waveform4 = createWaveform(end.plusSeconds(2000), end.plusSeconds(3000), 40.0);

    final ChannelSegment channelSegment1 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform1, waveform2));
    final ChannelSegment channelSegment2 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform3, waveform4));

    final Map<ChannelSegment, UUID> inToOutChannelUuids =
        Map.of(channelSegment1, outUuid1, channelSegment2, outUuid2);

    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("Filter2", PluginVersion.from(2, 1, 1));

    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterConfiguration.createStreamingFilterPluginParameters())
        .willReturn(mockStreamingFilterPluginParameters);

    given(mockStreamingFilterPluginParameters.lookupPlugin(FilterType.FIR_HAMMING))
        .willReturn(registrationInfo2);

    // don't define mockFilterControlPluginRegistry.lookup(registrationInfo2) and the plugin will not
    // be found
    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "Cannot execute streaming filter plugin. Plugin not found: " + registrationInfo2
            .toString());

    filterControl.initialize();
    filterControl.execute(ExecuteStreamingCommand
        .create(inToOutChannelUuids, TestFixtures.getFilterDefinition(),
            TestFixtures.getProcessingContext()));
  }

  @Test
  public void testExecuteStreaming() {
    final UUID outUuid1 = UUID.randomUUID();
    final UUID outUuid2 = UUID.randomUUID();
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(1000);

    final Waveform waveform1 = createWaveform(start, end, 40.0);
    final Waveform waveform2 = createWaveform(end, end.plusSeconds(1000), 40.0);

    final Waveform waveform3 = createWaveform(end.plusSeconds(1000), end.plusSeconds(2000), 40.0);
    final Waveform waveform4 = createWaveform(end.plusSeconds(2000), end.plusSeconds(3000), 40.0);

    final ChannelSegment channelSegment1 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform1, waveform2));
    final ChannelSegment channelSegment2 = TestFixtures
        .channelSegmentFromWaveforms(List.of(waveform3, waveform4));

    final Map<ChannelSegment, UUID> inToOutChannelUuids =
        Map.of(channelSegment1, outUuid1, channelSegment2, outUuid2);

    given(mockOsdGatewayClient.loadConfiguration()).willReturn(mockFilterConfiguration);
    given(mockFilterConfiguration.createStreamingFilterPluginParameters())
        .willReturn(mockStreamingFilterPluginParameters);

    given(mockFilterPlugin.getName()).willReturn("mockFilter");
    given(mockFilterPlugin.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    given(mockFilterPlugin.filter(channelSegment1,
        TestFixtures.getFilterDefinition())).willReturn(List.of(waveform1, waveform2));
    given(mockFilterPlugin.filter(channelSegment2,
        TestFixtures.getFilterDefinition())).willReturn(List.of(waveform3, waveform4));

    //set up plugins
    RegistrationInfo registrationInfo1 = RegistrationInfo.from("mockFilter",
        PluginVersion.from(1, 0, 0));

    //registry returns the right plugin for each parameter
    given(mockFilterControlPluginRegistry.lookup(registrationInfo1))
        .willReturn(Optional.of(mockFilterPlugin));

    given(mockStreamingFilterPluginParameters.lookupPlugin(FilterType.FIR_HAMMING))
        .willReturn(registrationInfo1);

    filterControl.initialize();

    final ProcessingContext context = TestFixtures.getProcessingContext();
    List<ChannelSegment> newChannelSegments = filterControl.execute(ExecuteStreamingCommand
        .create(inToOutChannelUuids, TestFixtures.getFilterDefinition(), context));

    assertTrue(newChannelSegments.size() == 2);
    assertTrue(Set.of(newChannelSegments.get(0).getProcessingChannelId(),
        newChannelSegments.get(1).getProcessingChannelId())
        .containsAll(List.of(outUuid1, outUuid2)));
    verify(mockFilterConfiguration).createStreamingFilterPluginParameters();

    verify(mockOsdGatewayClient, times(1))
        .store(isNotNull(), isNotNull(), eq(context.getStorageVisibility()));
  }
}
