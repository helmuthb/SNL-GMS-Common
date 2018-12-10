package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link QcMaskVersion} factory creation
 *
 * Created by jrhipp on 9/13/17.
 */
public class QcMaskVersionTests {

  private final long qcMaskVersionId = 6L;
  private final UUID qcMaskVersionParentId = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
  private final long qcMaskVersionParentVersion = 5L;

  private final List<QcMaskVersionReference> qcMaskVersionReferences = Collections.singletonList(
      QcMaskVersionReference.from(qcMaskVersionParentId, qcMaskVersionParentVersion));

  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays.asList(channelSegmentId1,
      channelSegmentId2);

  private final QcMaskType qcMaskType = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");

  private final UUID creationInfoId = UUID
      .fromString("a7b9cce8-4531-4215-8eda-0f4be2a0aeb8");

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities
        .checkStaticMethodValidatesNullArguments(QcMaskVersion.class, "create", qcMaskVersionId,
            qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory, qcMaskType, rationale,
            startTime, endTime, creationInfoId);
  }

  @Test
  public void testCreateNullableParametersRejectedCategory() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullableArguments(QcMaskVersion.class, "create",
        Arrays.asList(4, 6, 7), qcMaskVersionId, qcMaskVersionReferences,
        channelSegmentIdList, QcMaskCategory.REJECTED, qcMaskType, rationale, startTime, endTime,
        creationInfoId);
  }

  @Test
  public void testCreateRejectedCategoryAllowNull() {
    QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
            QcMaskCategory.REJECTED, null, rationale, null, null, creationInfoId);
  }

  @Test
  public void testCreateStartTimeAfterEndTimeExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("QcMaskVersion's start time must be before or equal to end time.");

    QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType, rationale, endTime, startTime, creationInfoId);
  }

  @Test
  public void testCreateDuplicateParentQcMasksExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("QcMaskVersion's parentQcMasks cannot contain duplicates");

    QcMaskVersionReference qcMaskVersionReference1 = QcMaskVersionReference
        .from(qcMaskVersionParentId, qcMaskVersionParentVersion);
    QcMaskVersionReference qcMaskVersionReference2 = QcMaskVersionReference
        .from(qcMaskVersionParentId, qcMaskVersionParentVersion);

    QcMaskVersion
        .create(qcMaskVersionId, Arrays.asList(qcMaskVersionReference1, qcMaskVersionReference2),
            channelSegmentIdList, qcMaskCategory, qcMaskType, rationale, startTime, endTime,
            creationInfoId);

  }

  @Test
  public void testCreate() {

    QcMaskVersion qcMaskVersion = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
            qcMaskCategory, qcMaskType, rationale, startTime, endTime, creationInfoId);

    assertEquals(qcMaskVersionId, qcMaskVersion.getVersion());
    assertEquals(qcMaskVersionReferences, qcMaskVersion.getParentQcMasks());
    assertEquals(channelSegmentIdList.get(0),
        qcMaskVersion.getChannelSegmentIds().get(0));
    assertEquals(channelSegmentIdList.get(1),
        qcMaskVersion.getChannelSegmentIds().get(1));
    assertEquals(qcMaskType, qcMaskVersion.getType().get());
    assertEquals(qcMaskCategory, qcMaskVersion.getCategory());
    assertEquals(rationale, qcMaskVersion.getRationale());
    assertEquals(startTime, qcMaskVersion.getStartTime().get());
    assertEquals(endTime, qcMaskVersion.getEndTime().get());
    assertEquals(creationInfoId, qcMaskVersion.getCreationInfoId());
    assertEquals(true, qcMaskVersion.hasParent());
  }

  @Test
  public void testHasNoParent() {

    QcMask qcMask = QcMask
        .create(UUID.randomUUID(), Collections.emptyList(), channelSegmentIdList,
            qcMaskCategory, qcMaskType, rationale,
            startTime, endTime, creationInfoId);
    QcMaskVersion qcMaskVersion = qcMask.getCurrentQcMaskVersion();

    assertFalse(qcMaskVersion.hasParent());
  }

  @Test
  public void testEqualsHashCode() {
    final QcMaskVersion a = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime, creationInfoId);

    final QcMaskVersion b = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime, creationInfoId);

    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testTypeCategoryValidation() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("QCMaskVersion's QcMaskType is not valid for the input QcMaskCategory");

    // throws error ... SENSOR_PROBLEM type is not a WAVEFORM_QUALITY category.
    QcMaskVersion.create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
        QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SENSOR_PROBLEM, rationale, startTime, endTime,
        creationInfoId);
  }

  @Test
  public void testEqualsExpectInequality() {
    final QcMaskVersion a = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime, creationInfoId);

    // Different version id
    QcMaskVersion b = QcMaskVersion
        .create(qcMaskVersionId + 1, qcMaskVersionReferences,
            channelSegmentIdList, qcMaskCategory, qcMaskType, rationale, startTime, endTime,
            creationInfoId);
    assertNotEquals(a, b);

    // Different version references
    b = QcMaskVersion
        .create(qcMaskVersionId, Collections.emptyList(), channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different channel segment id list
    b = QcMaskVersion.create(qcMaskVersionId, qcMaskVersionReferences,
        Collections.singletonList(channelSegmentId1), qcMaskCategory, qcMaskType, rationale,
        startTime, endTime,
        creationInfoId);
    assertNotEquals(a, b);

    // Different QcMaskType
    b = QcMaskVersion.create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
        qcMaskCategory, QcMaskType.SPIKE, rationale, startTime, endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different QcMaskCategory
    b = QcMaskVersion.create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
        QcMaskCategory.ANALYST_DEFINED, qcMaskType, rationale, startTime, endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different rationale
    b = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            "Different", startTime, endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different start time
    b = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime.plusMillis(1), endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different end time
    b = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime.plusMillis(1), creationInfoId);
    assertNotEquals(a, b);

    // Different creationInfoId
    b = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList, qcMaskCategory,
            qcMaskType,
            rationale, startTime, endTime,
            UUID.fromString("5d3e7012-2133-4674-a3a0-cee0ea873986"));
    assertNotEquals(a, b);
  }
}
