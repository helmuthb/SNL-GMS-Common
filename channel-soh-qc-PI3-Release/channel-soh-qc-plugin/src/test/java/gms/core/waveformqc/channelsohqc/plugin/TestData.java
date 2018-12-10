package gms.core.waveformqc.channelsohqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Groups data shared by plugin unit tests and has some verification operations based on this data.
 * The merged masks are separated by 25ms.  Tests based on this data will fail if the merging
 * threshold is less than 25ms.
 */
class TestData {

  QcMask qcMaskChan1;
  QcMask qcMaskNewOverlap;
  QcMask qcMaskChan1AfterGap;
  Collection<WaveformQcChannelSohStatus> overlapWaveformQcChannelSohStatus;

  QcMask qcMaskChan1NoOverlap;
  QcMask qcMaskChan1DiffType;
  QcMask qcMaskChan2;

  UUID processingChannelId1;
  Instant start1;
  Instant endMerged;
  Instant endMerged2;

  TestData() {

    processingChannelId1 = UUID.fromString("04e7d88d-13ef-4e06-ab63-f81c6a170784");
    final UUID processingChannelId2 = UUID
        .fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb");
    final List<QcMaskVersionReference> parents = Collections.emptyList();
    final List<UUID> channelSegmentIdList = new ArrayList<>();
    UUID channelSegmentId1 = UUID.randomUUID();
    channelSegmentIdList.add(channelSegmentId1);

    start1 = Instant.parse("2017-10-02T10:15:30.00Z");
    Instant end1 = Instant.parse("2017-10-02T10:15:40.00Z");

    qcMaskChan1 = QcMask
        .create(processingChannelId1, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(), start1,
            end1, UUID.randomUUID());

    Instant start2 = Instant.parse("2017-10-02T10:10:30.00Z");
    Instant end2 = Instant.parse("2017-10-02T10:10:40.00Z");
    qcMaskChan1NoOverlap = QcMask
        .create(processingChannelId1, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(), start2,
            end2, UUID.randomUUID());

    qcMaskChan1DiffType = QcMask
        .create(processingChannelId1, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.DIGITIZER_ANALOG_INPUT_SHORTED.getRationale(),
            start1, end1, UUID.randomUUID());

    qcMaskChan2 = QcMask
        .create(processingChannelId2, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(), start1,
            end1, UUID.randomUUID());

    // Overlaps with qcMaskChan1 (starts 25ms after qcMask1)
    endMerged = end1.plusMillis(25).plusSeconds(10);
    qcMaskNewOverlap = QcMask
        .create(processingChannelId1, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(),
            end1.plusMillis(25), endMerged, UUID.randomUUID());

    // WaveformQcChannelSohStatus to build qcMaskNewOverlap
    overlapWaveformQcChannelSohStatus = Collections.singletonList(WaveformQcChannelSohStatus
        .builder(processingChannelId1, AcquiredChannelSohType.CALIBRATION_UNDERWAY,
            qcMaskNewOverlap.getCurrentQcMaskVersion().getStartTime().get(),
            qcMaskNewOverlap.getCurrentQcMaskVersion().getEndTime().get(), true,
            Duration.ofSeconds(5))
        .build());

    // Overlaps with qcMaskChan1 and qcMaskNewOverlap (starts 25ms after qcMaskNewOverlap)
    endMerged2 = endMerged.plusMillis(25).plusSeconds(10);
    qcMaskChan1AfterGap = QcMask
        .create(processingChannelId1, parents, channelSegmentIdList, QcMaskCategory.STATION_SOH,
            QcMaskType.CALIBRATION, ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(),
            endMerged.plusMillis(25), endMerged2, UUID.randomUUID());
  }

  /**
   * Verifies the provided qcMasks contains 3 masks: a merged mask spanning qcMaskChan1 through
   * qcMaskChan1NoOverlap, a rejected qcMaskChan1, and a rejected qcMaskChan1NoOverlap are rejected.
   * Use this operation to verify tests providing qcMaskChan1 and qcMaskChan1NoOverlap as existing
   * masks and which generate (or accept) a new mask filling the gap between them.
   *
   * @param qcMasks list that must contain only the merged and rejected masks to avoid an assertion
   * failure
   */
  void verifyPluginMergeQcMasks(List<QcMask> qcMasks) {
    // Expect 3 returned masks: 2 rejected (qcMaskChan1AfterGap, qcMaskChan1) and the merged mask
    assertEquals(3, qcMasks.size());
    assertFalse(qcMasks.contains(this.qcMaskChan1NoOverlap));

    final int qcMaskChan1Index = qcMasks.indexOf(this.qcMaskChan1);
    assertNotEquals(-1, qcMaskChan1Index);
    assertTrue(qcMasks.get(qcMaskChan1Index).getCurrentQcMaskVersion().isRejected());

    final int qcMaskChan1AfterGapIndex = qcMasks.indexOf(this.qcMaskChan1AfterGap);
    assertNotEquals(-1, qcMaskChan1AfterGapIndex);
    assertTrue(qcMasks.get(qcMaskChan1AfterGapIndex).getCurrentQcMaskVersion().isRejected());

    Optional<QcMask> newQcMask = qcMasks.stream()
        .filter(q -> !q.getCurrentQcMaskVersion().isRejected()).findFirst();
    assertTrue(newQcMask.isPresent());
    assertEquals(this.qcMaskChan1.getQcMaskVersions().get(0).getStartTime(),
        newQcMask.get().getCurrentQcMaskVersion().getStartTime());
    assertEquals(this.qcMaskChan1AfterGap.getQcMaskVersions().get(0).getEndTime(),
        newQcMask.get().getCurrentQcMaskVersion().getEndTime());
  }

  /**
   * Verifies the result mask contains the same information as the expected mask and that the
   * result mask is not the same QcMask Entity as any of the expectedNotEqual masks
   *
   * @param results masks to verify, not null
   * @param expected mask the result should match, not null
   * @param expectedNotEqual collection of masks the result should not match, not null
   */
  public static void verifySingleMask(List<QcMask> results, QcMask expected,
      Collection<QcMask> expectedNotEqual) {

    // Expect a single new mask.  Verify none of the existing masks exist in the plugin's output
    assertEquals(1, results.size());
    final QcMask newMask = results.get(0);
    assertTrue(expectedNotEqual.stream().map(QcMask::getId).noneMatch(newMask.getId()::equals));
    assertEquals(1, newMask.getQcMaskVersions().size());

    // New QcMask should match the testData.qcMaskNewOverlap mask
    final QcMaskVersion expectedVersion = expected.getCurrentQcMaskVersion();
    final QcMaskVersion newVersion = newMask.getCurrentQcMaskVersion();
    assertEquals(expectedVersion.getStartTime(), newVersion.getStartTime());
    assertEquals(expectedVersion.getEndTime(), newVersion.getEndTime());
    assertEquals(expectedVersion.getType(), newVersion.getType());
    assertEquals(expectedVersion.getRationale(), newVersion.getRationale());
  }
}
