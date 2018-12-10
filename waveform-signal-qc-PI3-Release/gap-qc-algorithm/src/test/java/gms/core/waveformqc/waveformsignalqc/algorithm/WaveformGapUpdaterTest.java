package gms.core.waveformqc.waveformsignalqc.algorithm;

import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createChannelSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformGapUpdaterTest {

  private ChannelSegment channelSegmentOriginal;

  private WaveformGapQcMask originalGap;
  private WaveformGapQcMask beginGap;
  private WaveformGapQcMask endGap;
  private WaveformGapQcMask interiorGap1;
  private WaveformGapQcMask interiorGap2;
  private WaveformGapQcMask interiorGap3;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    final Instant start = Instant.EPOCH;

    TestUtility.createWaveform(start, start.plusSeconds(200), 40.0);
    final Waveform waveform_0_200 = TestUtility.createWaveform(start, start.plusSeconds(200), 40.0);
    final Waveform waveform_800_1000 = TestUtility
        .createWaveform(start.plusSeconds(800), start.plusSeconds(1000), 40.0);

    /*
    |                Original Gap                |
    |--------|                          |--------|
    |                                            |
    0       200                        800      1000
    */
    channelSegmentOriginal = createChannelSegment(List.of(waveform_0_200, waveform_800_1000));
    originalGap = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            UUID.randomUUID(), Instant.ofEpochSecond(200), Instant.ofEpochSecond(800));

    /*
    |                Original Gap                |
    |--------|                          |--------|
    |                                            |
    0       200                        800      1000

    |                         New Gap            |
    |---------|-----------|             |--------|
    |                                            |
    0        200         400           800      1000
    */
    endGap = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            UUID.randomUUID(), Instant.ofEpochSecond(400), Instant.ofEpochSecond(800));


    /*
    |                Original Gap                |
    |--------|                          |--------|
    |                                            |
    0       200                        800      1000

    |             New Gap                        |
    |---------|            |-------------|-------|
    |                                            |
    0        200          500           800      1000
    */
    beginGap = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            UUID.randomUUID(), Instant.ofEpochSecond(200), Instant.ofEpochSecond(500));

    /*
    |                Original Gap                |
    |--------|                          |--------|
    |                                            |
    0       200                        800      1000

    |          New Gap        New Gap            |
    |--------|        |-----|           |--------|
    |                                            |
    0       200      400   500         800      1000
    */
    UUID channelSegmentId = UUID.randomUUID();
    interiorGap1 = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            channelSegmentId, Instant.ofEpochSecond(200), Instant.ofEpochSecond(400));

    interiorGap2 = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            channelSegmentId, Instant.ofEpochSecond(500), Instant.ofEpochSecond(800));

    /*
    |                Original Gap                |
    |--------|                          |--------|
    |                                            |
    0       200                        800      1000

    |                 New Gap                    |
    |--------|--------|     |-----------|--------|
    |                                            |
    0       200      400   500         800      1000
    */
    interiorGap3 = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelSegmentOriginal.getProcessingChannelId(),
            channelSegmentId, Instant.ofEpochSecond(400), Instant.ofEpochSecond(500));
  }

  private QcMask createOriginalMask() {
    return QcMask
        .create(channelSegmentOriginal.getProcessingChannelId(), List.of(),
            List.of(channelSegmentOriginal.getId()), QcMaskCategory.WAVEFORM_QUALITY,
            QcMaskType.LONG_GAP, "Test Gap", Instant.ofEpochSecond(200), Instant.ofEpochSecond(800),
            UUID.randomUUID());
  }

  /**
   * Acquire a new data segment in the middle of an existing gap.  This results in the original
   * gap being split - the original rejected and two new masks for the new gap portions point back
   * to the original as their parent.
   */
  @Test
  public void testAcquireInteriorSegment() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(interiorGap1, interiorGap2), List.of(originalMask),
            channelSegmentOriginal.getId(), creationInfoId);

    // Expect: original mask rejected, two new masks point to original
    assertEquals(3, masksNew.size());
    assertTrue(masksNew.contains(originalMask));

    assertEquals(2, originalMask.getQcMaskVersions().size());
    assertTrue(originalMask.getCurrentQcMaskVersion().isRejected());

    Set<QcMask> nonRejected = masksNew.stream()
        .filter(m -> !m.getCurrentQcMaskVersion().isRejected()).collect(Collectors.toSet());

    QcMaskVersionReference parentRef = QcMaskVersionReference
        .from(originalMask.getId(), originalMask.getCurrentQcMaskVersion().getVersion());

    assertEquals(2, nonRejected.size());
    checkContainsGap(nonRejected, interiorGap1, parentRef);
    checkContainsGap(nonRejected, interiorGap2, parentRef);
  }

  private static void checkContainsGap(Set<QcMask> newMasks, WaveformGapQcMask gap,
      QcMaskVersionReference parentRef) {
    List<QcMask> matchingMask = newMasks.stream()
        .filter(m -> m.getCurrentQcMaskVersion().getStartTime().get().equals(gap.getStartTime()))
        .collect(Collectors.toList());

    assertEquals(1, matchingMask.size());

    QcMaskVersion curVersion = matchingMask.get(0).getCurrentQcMaskVersion();
    assertEquals(gap.getStartTime(), curVersion.getStartTime().get());
    assertEquals(gap.getEndTime(), curVersion.getEndTime().get());

    assertEquals(1, curVersion.getParentQcMasks().size());
    assertEquals(parentRef, curVersion.getParentQcMasks().iterator().next());
  }

  /**
   * Test acquiring data at the beginning of an existing gap.  The result is a new version
   * of the existing mask.
   */
  @Test
  public void testAcquireBeginning() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(endGap), List.of(originalMask), channelSegmentOriginal.getId(),
            creationInfoId);

    checkCurrentMaskVersionAgainstGap(originalMask, masksNew, endGap);
  }

  /**
   * Test acquiring data at the end of an existing gap.  The result is a new version
   * of the existing mask.
   */
  @Test
  public void testAcquireEndSegment() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(beginGap), List.of(originalMask), channelSegmentOriginal.getId(),
            creationInfoId);

    checkCurrentMaskVersionAgainstGap(originalMask, masksNew, beginGap);
  }

  /**
   * Test acquiring data at both the beginning and end of an existing gap.  The result is a new
   * version of the existing mask.
   */
  @Test
  public void testAcquireBeginningAndEnd() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(interiorGap3), List.of(originalMask), channelSegmentOriginal.getId(),
            creationInfoId);

    checkCurrentMaskVersionAgainstGap(originalMask, masksNew, interiorGap3);
  }

  private static void checkCurrentMaskVersionAgainstGap(QcMask originalMask, List<QcMask> newMask,
      WaveformGapQcMask gap) {
    assertEquals(1, newMask.size());
    assertTrue(newMask.contains(originalMask));
    assertEquals(2, originalMask.getQcMaskVersions().size());
    assertFalse(originalMask.getCurrentQcMaskVersion().isRejected());

    QcMaskVersion curVersion = originalMask.getCurrentQcMaskVersion();
    assertEquals(gap.getStartTime(), curVersion.getStartTime().get());
    assertEquals(gap.getEndTime(), curVersion.getEndTime().get());
  }

  /**
   * Test no new data is acquired but the data is reprocessed.  The result is no mask changes.
   */
  @Test
  public void testFindNewGapEqualToOriginalGap() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(originalGap), List.of(originalMask), channelSegmentOriginal.getId(),
            creationInfoId);

    assertEquals(0, masksNew.size());
  }

  /**
   * Test acquiring data that fills a gap.  Expected result is the mask is rejected.
   */
  @Test
  public void testAcquireFullGap() {
    QcMask originalMask = createOriginalMask();

    UUID creationInfoId = UUID.randomUUID();
    List<QcMask> masksNew = WaveformGapUpdater
        .updateQcMasks(List.of(), List.of(originalMask), channelSegmentOriginal.getId(),
            creationInfoId);

    assertEquals(1, masksNew.size());
    assertTrue(masksNew.contains(originalMask));
    assertEquals(2, originalMask.getQcMaskVersions().size());
    assertTrue(originalMask.getCurrentQcMaskVersion().isRejected());
  }

  @Test
  public void testNullGapsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformGapUpdater.updateQcMasks requires non-null gaps");
    WaveformGapUpdater.updateQcMasks(null, List.of(), UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testNullMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformGapUpdater.updateQcMasks requires non-null existing masks");
    WaveformGapUpdater.updateQcMasks(List.of(), null, UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testNullChannelSegmentIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformGapUpdater.updateQcMasks requires non-null channelSegmentId");
    WaveformGapUpdater.updateQcMasks(List.of(), List.of(), null, UUID.randomUUID());
  }

  @Test
  public void testNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformGapUpdater.updateQcMasks requires non-null creationInfoId");
    WaveformGapUpdater.updateQcMasks(List.of(), List.of(), UUID.randomUUID(), null);
  }
}
