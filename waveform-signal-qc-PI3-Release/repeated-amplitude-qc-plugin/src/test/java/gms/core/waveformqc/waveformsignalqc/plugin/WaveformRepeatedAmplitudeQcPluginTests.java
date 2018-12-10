package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeQcMask;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPlugin;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginConfiguration;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginParameters;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WaveformRepeatedAmplitudeQcPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformRepeatedAmplitudeQcPluginConfiguration mockPluginConfig;

  @Mock
  private WaveformRepeatedAmplitudeQcPluginConfiguration mockPluginConfigMergeThresholdZero;

  @Mock
  private WaveformRepeatedAmplitudeInterpreter mockWaveformRepeatedAmplitudeInterpreter;

  private static final int minRepeats = 2;
  private static final double maxDelta = 1.0;
  private static final double maskMergeThresholdSec = 1.0;
  private static final double startAmplitude = 2.5;

  private List<ChannelSegment> channelSegments;

  private Map<ChannelSegment, List<WaveformRepeatedAmplitudeQcMask>> expectedMasksByChannelSegment;

  private List<WaveformRepeatedAmplitudeQcMask> allRepeatedAmplitudeMasks;

  private QcMask existingQcMaskToMerge;

  @Before
  public void setUp() throws Exception {

    given(mockPluginConfig.createParameters())
        .willReturn(WaveformRepeatedAmplitudeQcPluginParameters
            .create(minRepeats, maxDelta, maskMergeThresholdSec));

    given(mockPluginConfigMergeThresholdZero.createParameters())
        .willReturn(WaveformRepeatedAmplitudeQcPluginParameters
            .create(minRepeats, maxDelta, 0.0));

    UUID processingChannelId1 = UUID.randomUUID();
    UUID processingChannelId2 = UUID.randomUUID();
    channelSegments = List.of(
        generateChannelSegment(processingChannelId1), generateChannelSegment(processingChannelId2)
    );

    expectedMasksByChannelSegment = new HashMap<>();
    channelSegments.forEach(c ->
        expectedMasksByChannelSegment.put(c, List.of(WaveformRepeatedAmplitudeQcMask
            .create(Instant.EPOCH.plusSeconds(5), Instant.EPOCH.plusSeconds(15),
                c.getProcessingChannelId(), c.getId())))
    );

    existingQcMaskToMerge = QcMask
        .create(processingChannelId1, List.of(), List.of(UUID.randomUUID()),
            QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE,
            "System created repeated adjacent amplitude values mask", Instant.EPOCH,
            Instant.EPOCH.plusSeconds(5), UUID.randomUUID());

    channelSegments.forEach(c ->
        given(mockWaveformRepeatedAmplitudeInterpreter
            .createWaveformRepeatedAmplitudeQcMasks(c, minRepeats, maxDelta))
            .willReturn(expectedMasksByChannelSegment.get(c)));

    allRepeatedAmplitudeMasks = expectedMasksByChannelSegment
        .values().stream().flatMap(List::stream).collect(Collectors.toList());
  }

  private ChannelSegment generateChannelSegment(UUID processingChannelId) {
    return ChannelSegment.create(processingChannelId, "test", ChannelSegmentType.RAW, Instant.EPOCH,
        Instant.EPOCH.plusSeconds(20), new TreeSet<>(List.of()), CreationInfo.DEFAULT);
  }

  private WaveformRepeatedAmplitudeQcPlugin createPluginWithMocks() {
    return WaveformRepeatedAmplitudeQcPlugin
        .create(mockPluginConfig,
            mockWaveformRepeatedAmplitudeInterpreter);
  }

  @Test
  public void testCreate() throws Exception {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    assertNotNull(plugin);
  }

  @Test
  public void testCreateNullConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPlugin create cannot accept null pluginConfiguration");
    WaveformRepeatedAmplitudeQcPlugin.create(null, mockWaveformRepeatedAmplitudeInterpreter);
  }

  @Test
  public void testCreateNullInterpreterExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPlugin create cannot accept null waveformRepeatedAmplitudeInterpreter");
    WaveformRepeatedAmplitudeQcPlugin
        .create(mockPluginConfig, null);
  }

  @Test
  public void testCreateQcMasks() throws Exception {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    UUID creationInfoId = UUID.randomUUID();
    Stream<QcMask> qcMasksStream = plugin.createQcMasks(channelSegments, List.of(), creationInfoId);

    verifyContainsExpectedMasks(qcMasksStream);
  }

  private static boolean equivalent(QcMask qcMask, WaveformRepeatedAmplitudeQcMask repeatedAmp) {
    QcMaskVersion curVer = qcMask.getCurrentQcMaskVersion();

    return qcMask.getProcessingChannelId().equals(repeatedAmp.getChannelId())
        && qcMask.getQcMaskVersions().size() == 1
        && !curVer.isRejected()
        && curVer.getStartTime().get().equals(repeatedAmp.getStartTime())
        && curVer.getEndTime().get().equals(repeatedAmp.getEndTime())
        && curVer.getChannelSegmentIds().size() == 1
        && curVer.getChannelSegmentIds().contains(repeatedAmp.getChannelSegmentId())
        && curVer.getCategory().equals(QcMaskCategory.WAVEFORM_QUALITY)
        && curVer.getType().get().equals(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE)
        && curVer.getRationale().equals(
        "System created repeated adjacent amplitude values mask");
  }

  @Test
  public void testCreateQcMasksMergesExistingMasks() throws Exception {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    UUID creationInfoId = UUID.randomUUID();
    Stream<QcMask> qcMasksStream = plugin
        .createQcMasks(List.of(channelSegments.get(0)), List.of(existingQcMaskToMerge),
            creationInfoId);

    assertNotNull(qcMasksStream);
    List<QcMask> qcMasksList = qcMasksStream.collect(Collectors.toList());

    assertEquals(1, qcMasksList.size());
    assertEquals(2, qcMasksList.get(0).getQcMaskVersions().size());

    QcMaskVersion curVersion = qcMasksList.get(0).getCurrentQcMaskVersion();
    assertFalse(curVersion.isRejected());

    assertEquals(Instant.EPOCH, curVersion.getStartTime().get());
    assertEquals(Instant.EPOCH.plusSeconds(15), curVersion.getEndTime().get());
    assertEquals(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE, curVersion.getType().get());
  }

  @Test
  public void testCreateQcMasksUsesConfiguredMergeThreshold() throws Exception {
    WaveformRepeatedAmplitudeQcPlugin plugin = WaveformRepeatedAmplitudeQcPlugin
        .create(mockPluginConfigMergeThresholdZero, mockWaveformRepeatedAmplitudeInterpreter);

    // If merge threshold is not used then the new and existing masks would merge
    // since they are 0.0 seconds apart
    UUID creationInfoId = UUID.randomUUID();
    Stream<QcMask> qcMasksStream = plugin
        .createQcMasks(channelSegments, List.of(existingQcMaskToMerge), creationInfoId);

    verifyContainsExpectedMasks(qcMasksStream);
  }

  @Test
  public void testCreateQcMasksFiltersNonRepeatedAmplitudeMasks() throws Exception {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    // If these were repeated adjacent amplitude masks the plugin would update them
    List<QcMask> notRepeatedAmplitudeMasks = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
            channelSegments.get(0).getProcessingChannelId(), channelSegments.get(0).getId(),
            Instant.ofEpochSecond(5), Instant.ofEpochSecond(15)),

        createQcMask(QcMaskCategory.ANALYST_DEFINED, QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE,
            channelSegments.get(0).getProcessingChannelId(), channelSegments.get(0).getId(),
            Instant.ofEpochSecond(5), Instant.ofEpochSecond(15)),

        createQcMask(QcMaskCategory.STATION_SOH, QcMaskType.TIMING,
            channelSegments.get(1).getProcessingChannelId(), channelSegments.get(1).getId(),
            Instant.ofEpochSecond(0), Instant.ofEpochSecond(20))
    );

    UUID creationInfoId = UUID.randomUUID();
    Stream<QcMask> qcMasksStream = plugin
        .createQcMasks(channelSegments, notRepeatedAmplitudeMasks, creationInfoId);

    verifyContainsExpectedMasks(qcMasksStream);
  }

  private void verifyContainsExpectedMasks(Stream<QcMask> qcMasksStream) {
    assertNotNull(qcMasksStream);
    List<QcMask> qcMasksList = qcMasksStream.collect(Collectors.toList());

    assertEquals(expectedMasksByChannelSegment.values().stream().mapToInt(List::size).sum(),
        qcMasksList.size());
    assertTrue(qcMasksList.stream().allMatch(
        q -> allRepeatedAmplitudeMasks.stream().filter(r -> equivalent(q, r)).count() == 1));
  }

  private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
      UUID channelSegmentId, Instant start, Instant end) {

    return QcMask
        .create(processingChannelId, List.of(), List.of(channelSegmentId),
            category, type, "System created: amplitudes within " + maxDelta + " of "
                + startAmplitude, start, end, UUID.randomUUID());
  }

  @Test
  public void testCreateQcMasksNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null channelSegments");
    createPluginWithMocks().createQcMasks(null, List.of(), UUID.randomUUID());
  }

  @Test
  public void testCreateQcMasksNullExistingMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null existingQcMasks");
    createPluginWithMocks().createQcMasks(List.of(), null, UUID.randomUUID());
  }

  @Test
  public void testCreateQcMasksNullCreationInfoUuidExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null creationInfoId");
    createPluginWithMocks().createQcMasks(List.of(), List.of(), null);
  }
}
