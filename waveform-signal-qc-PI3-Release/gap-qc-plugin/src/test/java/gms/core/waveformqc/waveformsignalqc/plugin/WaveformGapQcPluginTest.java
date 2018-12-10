package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WaveformGapQcPluginTest {

  private static ChannelSegment channelSegment;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformGapQcPluginConfiguration mockWaveformGapQcPluginConfiguration;

  public static Waveform createWaveform(Instant start, Instant end, double samplesPerSec) {
    if (0 != Duration.between(start, end).getNano()) {
      throw new IllegalArgumentException(
          "Test can't create waveform where the sample rate does not evenly divide the duration");
    }

    int numSamples = (int) (Duration.between(start, end).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.create(start, end, samplesPerSec, numSamples, values);
  }

  @Before
  public void setUp() throws Exception {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(1000);

    final Waveform waveform = createWaveform(start, start.plusSeconds(200), 40.0);
    final Waveform waveform1 = createWaveform(start.plusSeconds(350), start.plusSeconds(400), 40.0);
    final Waveform waveform2 = createWaveform(start.plusSeconds(400), start.plusSeconds(600), 40.0);
    final Waveform waveform3 = createWaveform(start.plusSeconds(610), start.plusSeconds(800), 40.0);
    final Waveform waveform4 = createWaveform(start.plusSeconds(800), end, 40.0);

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.ChannelSegmentType.RAW,
        start, end, new TreeSet<>(List.of(waveform, waveform1, waveform2, waveform3, waveform4)),
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));

    given(mockWaveformGapQcPluginConfiguration.createParameters())
        .willReturn(WaveformGapQcPluginParameters.create(2));
  }

  @Test
  public void testCreateNullConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformGapQcPlugin create cannot accept null pluginConfiguration");
    WaveformGapQcPlugin.create(null);
  }

  /**
   * Make sure the plugin creates masks.  Don't check full details of the mask since that is done
   * in algorithm testing.
   */
  @Test
  public void testGapMaskCreation() {
    WaveformGapQcPlugin plugin = WaveformGapQcPlugin.create(mockWaveformGapQcPluginConfiguration);

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> gapMasks = plugin.createQcMasks(List.of(channelSegment), List.of(), creationInfoId)
        .collect(Collectors.toList());

    assertEquals(2, gapMasks.size());
  }

  /**
   * Make sure the plugin updates existing QC Masks.  Don't check full details since that is covered
   * in algorithm testing.
   */
  @Test
  public void testCreateQcMasksUpdatesExisting() {

    // Both will be rejected due to being filled
    List<QcMask> gaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP,
            channelSegment.getProcessingChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
            channelSegment.getProcessingChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    WaveformGapQcPlugin plugin = WaveformGapQcPlugin.create(mockWaveformGapQcPluginConfiguration);

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> gapMasks = plugin.createQcMasks(List.of(channelSegment), gaps, creationInfoId)
        .collect(Collectors.toList());

    // Two new, two rejected
    assertEquals(4, gapMasks.size());

    // Input masks should be returned since they are rejected
    List<QcMask> updated = gapMasks.stream().filter(gaps::contains).collect(Collectors.toList());
    assertEquals(2, updated.size());
    assertTrue(
        updated.stream().map(QcMask::getCurrentQcMaskVersion).allMatch(QcMaskVersion::isRejected));
  }

  /**
   * Make sure the plugin only updates gap masks.  Don't check full details since that is covered
   * in algorithm testing.
   */
  @Test
  public void testCreateQcMasksFiltersNonGapMasks() throws Exception {

    // If these were gaps they would be rejected
    List<QcMask> notGaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
            channelSegment.getProcessingChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.STATION_SOH, QcMaskType.TIMING,
            channelSegment.getProcessingChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    WaveformGapQcPlugin plugin = WaveformGapQcPlugin.create(mockWaveformGapQcPluginConfiguration);

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> gapMasks = plugin.createQcMasks(List.of(channelSegment), notGaps, creationInfoId)
        .collect(Collectors.toList());

    assertEquals(2, gapMasks.size());
  }

  /**
   * Make sure the plugin only updates existing gap masks on the correct processing channel.
   * Don't check full details since that is covered in algorithm testing.
   */
  @Test
  public void testCreateQcMasksFiltersMasksByChannel() throws Exception {

    UUID channelSegmentUuid = generateDifferentUuid(channelSegment.getId());
    UUID channelUuid = generateDifferentUuid(channelSegment.getProcessingChannelId());

    List<QcMask> notGaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP, channelUuid,
            channelSegmentUuid,
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP, channelUuid,
            channelSegmentUuid,
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    WaveformGapQcPlugin plugin = WaveformGapQcPlugin.create(mockWaveformGapQcPluginConfiguration);

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> gapMasks = plugin.createQcMasks(List.of(channelSegment), notGaps, creationInfoId)
        .collect(Collectors.toList());

    assertEquals(2, gapMasks.size());
  }

  private static UUID generateDifferentUuid(UUID uuid) {
    UUID other;
    do {
      other = UUID.randomUUID();
    } while (other.equals(uuid));

    return other;
  }

  private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
      UUID channelSegmentId,
      Instant start, Instant end) {

    return QcMask
        .create(processingChannelId, List.of(), List.of(channelSegmentId),
            category, type, "Test Mask", start, end, UUID.randomUUID());
  }
}
