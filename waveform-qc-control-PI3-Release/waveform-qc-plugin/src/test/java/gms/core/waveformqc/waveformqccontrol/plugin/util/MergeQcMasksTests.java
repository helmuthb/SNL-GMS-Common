package gms.core.waveformqc.waveformqccontrol.plugin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MergeQcMasksTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private MergeQcMasksTestData testData;
  private Duration threshold;
  private UUID creationInfoId;

  @Before
  public void setUp() throws Exception {
    testData = new MergeQcMasksTestData();

    final double sampleLengthSec = 1.0 / 40.0;
    threshold = Duration.ofNanos((long) (1.5 * sampleLengthSec * 1e9));
    creationInfoId = new UUID(0L, 0L);
  }

  /**
   * Verifies the merge operation extends an existing mask.  Also verifies it does not return
   * existing masks unaffected by the merge.
   */
  @Test
  public void testMergeExtendExistingMask() throws Exception {

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold, creationInfoId);

    assertEquals(1, result.size());

    QcMask merged = result.iterator().next();
    assertEquals(testData.qcMaskChan1.getId(), merged.getId());
    assertEquals(2, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertEquals(testData.start1, mergedVersion.getStartTime().get());
    assertEquals(testData.endMerged, mergedVersion.getEndTime().get());
    assertEquals(QcMaskType.CALIBRATION, mergedVersion.getType().get());
    assertEquals(ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(),
        mergedVersion.getRationale());

    verifyMergedCreationInfoId(result, creationInfoId);
  }

  /**
   * Verifies the merging of a duplicate new mask with an existing one causes no modifications to
   * the existing mask nor creation of new masks. The existing mask should simply be returned.
   */
  @Test
  public void testMergeDuplicateExistingMask() throws Exception {
    MergeQcMasksTestData existingTestData = new MergeQcMasksTestData();

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskChan1);
    Collection<QcMask> existingMasks = Collections.singleton(existingTestData.qcMaskChan1);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold, creationInfoId);

    assertEquals(1, result.size());
    QcMask merged = result.iterator().next();
    assertEquals(existingTestData.qcMaskChan1, merged);
    assertEquals(1, merged.qcMaskVersions().count());
  }

  @Test
  public void testMergeTwoExistingMasks() throws Exception {

    // Merged mask expected parents are the current versions of qcMaskChan1AfterGap and qcMaskChan1
    final QcMaskVersionReference afterGapParent = QcMaskVersionReference
        .from(testData.qcMaskChan1AfterGap.getId(),
            testData.qcMaskChan1AfterGap.getCurrentQcMaskVersion().getVersion());
    final QcMaskVersionReference preGapParent = QcMaskVersionReference
        .from(testData.qcMaskChan1.getId(),
            testData.qcMaskChan1.getCurrentQcMaskVersion().getVersion());

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold, creationInfoId);

    // Expect 3 returned masks: 2 rejected (qcMaskChan1AfterGap, qcMaskChan1) and the merged mask
    assertEquals(3, result.size());
    assertFalse(result.contains(testData.qcMaskNewOverlap));
    assertFalse(result.contains(testData.qcMaskChan1NoOverlap));

    // Find and verify the single new merged mask
    QcMask merged = verifySingleUnrejectedMask(result);
    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    verifyMergedQcMask(mergedVersion);

    // Verify correct parents
    final Collection<QcMaskVersionReference> parents = mergedVersion.getParentQcMasks();
    assertEquals(2, parents.size());
    assertTrue(parents.contains(afterGapParent));
    assertTrue(parents.contains(preGapParent));

    // Verify the rejected masks have correct attributes
    assertRejected(testData.qcMaskChan1AfterGap.getId(), result, merged.getId());
    assertRejected(testData.qcMaskChan1.getId(), result, merged.getId());

    verifyMergedCreationInfoId(result, creationInfoId);
  }

  private QcMask verifySingleUnrejectedMask(Collection<QcMask> result) {
    final Predicate<QcMask> unrejectedMaskPredicate = q -> !q.getCurrentQcMaskVersion()
        .isRejected();
    assertEquals(1, result.stream().filter(unrejectedMaskPredicate).count());

    Optional<QcMask> mergedOptional = result.stream().filter(unrejectedMaskPredicate).findAny();
    assertTrue(mergedOptional.isPresent());
    assertEquals(1, mergedOptional.get().qcMaskVersions().count());

    return mergedOptional.get();
  }

  @Test
  public void testMergeThreeNewAndNoExistingMasks() throws Exception {

    Collection<QcMask> newMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1AfterGap, testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Collections.emptyList();
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold, creationInfoId);

    // Expect 1 returned masks: the newly merged mask
    assertEquals(1, result.size());
    assertFalse(result.contains(testData.qcMaskChan1));
    assertFalse(result.contains(testData.qcMaskChan1AfterGap));
    assertFalse(result.contains(testData.qcMaskNewOverlap));

    // Find and verify the new merged mask
    QcMask merged = result.iterator().next();
    assertEquals(1, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    verifyMergedQcMask(mergedVersion);
    assertEquals(Collections.emptyList(), mergedVersion.getParentQcMasks());

    verifyMergedCreationInfoId(result, creationInfoId);
  }

  @Test
  public void testMergeTwoMasksExistingInsideNew() throws Exception {
    Instant start = Instant.ofEpochSecond(0);
    Instant end = Instant.ofEpochSecond(300);

    UUID processingChannelId = UUID.randomUUID();
    QcMask newMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start, end,
            new UUID(0L, 0L));

    QcMask existingMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start.plusSeconds(10),
            end.minusSeconds(10), new UUID(0L, 0L));

    Collection<QcMask> result = MergeQcMasks
        .merge(Collections.singleton(newMask), Collections.singleton(existingMask),
            threshold, creationInfoId);

    assertEquals(1, result.size());
    assertTrue(result.contains(existingMask));

    QcMask merged = result.iterator().next();
    assertEquals(2, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertTrue(mergedVersion.getStartTime().isPresent());
    assertEquals(start, mergedVersion.getStartTime().get());
    assertTrue(mergedVersion.getEndTime().isPresent());
    assertEquals(end, mergedVersion.getEndTime().get());

    verifyMergedCreationInfoId(result, creationInfoId);
  }

  @Test
  public void testMergeTwoMasksNewInsideExisting() throws Exception {
    Instant start = Instant.ofEpochSecond(0);
    Instant end = Instant.ofEpochSecond(300);

    UUID processingChannelId = UUID.randomUUID();
    QcMask newMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start.plusSeconds(10),
            end.minusSeconds(10), new UUID(0L, 0L));

    QcMask existingMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start, end,
            new UUID(0L, 0L));

    Collection<QcMask> result = MergeQcMasks
        .merge(Collections.singleton(newMask), Collections.singleton(existingMask),
            threshold, creationInfoId);

    assertEquals(1, result.size());
    assertTrue(result.contains(existingMask));

    QcMask merged = result.iterator().next();
    assertEquals(1, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertTrue(mergedVersion.getStartTime().isPresent());
    assertEquals(start, mergedVersion.getStartTime().get());
    assertTrue(mergedVersion.getEndTime().isPresent());
    assertEquals(end, mergedVersion.getEndTime().get());

    verifyMergedCreationInfoId(result, creationInfoId);
  }

  private static void verifyMergedCreationInfoId(Collection<QcMask> result,
      UUID expectedCreationInfoId) {

    assertTrue(result.stream()
        .map(QcMask::getCurrentQcMaskVersion)
        .map(QcMaskVersion::getCreationInfoId)
        .allMatch(expectedCreationInfoId::equals));
  }

  private void verifyMergedQcMask(QcMaskVersion mergedVersion) {
    assertEquals(testData.start1, mergedVersion.getStartTime().get());
    assertEquals(testData.endMerged2, mergedVersion.getEndTime().get());
    assertEquals(QcMaskType.CALIBRATION, mergedVersion.getType().get());
    assertEquals(ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale(),
        mergedVersion.getRationale());
  }

  private void assertRejected(UUID qcMaskId, Collection<QcMask> result, UUID id) {
    Optional<QcMask> actual = result.stream().filter(q -> q.getId().equals(qcMaskId)).findAny();
    assertTrue(actual.isPresent());
    assertEquals(2, actual.get().qcMaskVersions().count());
    assertTrue(actual.get().getCurrentQcMaskVersion().isRejected());

    final String expectedRationale = "Merged to form QcMask with ID: " + id;
    assertEquals(expectedRationale, actual.get().getCurrentQcMaskVersion().getRationale());
  }

  @Test
  public void testMergeReturnsSingleNewMaskExactly() throws Exception {
    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, Collections.emptyList(), threshold, creationInfoId);

    assertEquals(1, result.size());
    assertEquals(testData.qcMaskNewOverlap, result.iterator().next());
  }

  @Test
  public void testMergeThresholdNonInclusive() throws Exception {
    Duration maskSeparation = Duration.ofMillis(25);

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, maskSeparation, creationInfoId);

    // Verify the two masks are actually separated by maskSeparation
    final Instant start = testData.qcMaskNewOverlap.getCurrentQcMaskVersion().getStartTime().get();
    final Instant end = testData.qcMaskChan1.getCurrentQcMaskVersion().getEndTime().get();
    assertEquals(maskSeparation, Duration.between(end, start));

    // Verify only the new mask is returned
    assertEquals(1, result.size());

    QcMask newMask = result.iterator().next();
    assertEquals(testData.qcMaskNewOverlap.getId(), newMask.getId());
    assertEquals(1, newMask.qcMaskVersions().count());
  }

  @Test
  public void testMergeNullNewMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("MergeQcMasks requires non-null newMasks");
    MergeQcMasks.merge(null, Collections.emptySet(), threshold, creationInfoId);
  }

  @Test
  public void testMergeNullExistingMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("MergeQcMasks requires non-null existingMasks");
    MergeQcMasks.merge(Collections.emptySet(), null, threshold, creationInfoId);
  }

  @Test
  public void testMergeNullThresholdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("MergeQcMasks requires non-null threshold");
    MergeQcMasks.merge(Collections.emptySet(), Collections.emptySet(), null, creationInfoId);
  }

  @Test
  public void testMergeNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("MergeQcMasks requires non-null creationInfoId");
    MergeQcMasks.merge(Collections.emptySet(), Collections.emptySet(), threshold, null);
  }

  @Test
  public void testMergeDifferentExistingTypesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks have the same category, type and rationale");
    MergeQcMasks
        .merge(Collections.singleton(testData.qcMaskNewOverlap),
            List.of(testData.qcMaskChan1, testData.qcMaskChan1DiffType), threshold,
            creationInfoId);
  }

  @Test
  public void testMergeDifferentCategoriesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks have the same category, type and rationale");
    MergeQcMasks
        .merge(Collections.singleton(testData.analystDefinedOverlap),
            List.of(testData.systemDefinedOverlap), threshold,
            creationInfoId);
  }

  @Test
  public void testMergeDifferentNewExistingTypesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks have the same category, type and rationale");
    MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
        Collections.singletonList(testData.qcMaskChan1DiffType), threshold, creationInfoId);
  }

  @Test
  public void testMergeDifferentNewTypesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks have the same category, type and rationale");
    MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap, testData.qcMaskChan1DiffType),
        Collections.singletonList(testData.qcMaskChan1), threshold, creationInfoId);
  }

  @Test
  public void testMergeDifferentNewRationaleExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks have the same category, type and rationale");
    MergeQcMasks.merge(List.of(testData.qcMaskNewOverlapWrongRationale),
        Collections.singletonList(testData.qcMaskNewOverlap), threshold, creationInfoId);
  }

  @Test
  public void testMergeDifferentExistingChannelsExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks apply to the same ProcessingChannel");
    MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
        List.of(testData.qcMaskChan1, testData.qcMaskChan2), threshold, creationInfoId);
  }

  @Test
  public void testMergeDifferentNewExistingChannelsExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks apply to the same ProcessingChannel");
    MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
        Collections.singletonList(testData.qcMaskChan2), threshold, creationInfoId);
  }

  @Test
  public void testMergeDifferentNewChannelExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires all newMasks and existingMasks apply to the same ProcessingChannel");
    MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap, testData.qcMaskChan2),
        Collections.singletonList(testData.qcMaskChan1), threshold, creationInfoId);
  }

  @Test
  public void testMergeNoNewMasksExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "MergeQcMasks requires at least one newMask");
    MergeQcMasks
        .merge(Collections.emptyList(), Collections.singletonList(testData.qcMaskChan1), threshold,
            creationInfoId);
  }

  @Test
  public void testMergeRejectedMasksExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("MergeQcMasks cannot merge rejected QcMasks");
    testData.qcMaskChan1.reject("Rejected", Collections.emptyList(), creationInfoId);
    MergeQcMasks.merge(Collections.singletonList(testData.qcMaskNewOverlap),
        Collections.singletonList(testData.qcMaskChan1), threshold, creationInfoId);
  }
}
