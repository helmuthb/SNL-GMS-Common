package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link QcMask) creation and usage semantics
 */
public class QcMaskTests {

  private final UUID processingChannelId = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays
      .asList(channelSegmentId1, channelSegmentId2);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskType qcMaskType2 = QcMaskType.SPIKE;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
  private final UUID creationInfoId = UUID.randomUUID();
  private final QcMaskVersionReference parent1 = QcMaskVersionReference
      .from(UUID.randomUUID(), 3);
  private final QcMaskVersionReference parent2 = QcMaskVersionReference
      .from(UUID.randomUUID(), 1);
  private final List<QcMaskVersionReference> parents = Arrays.asList(parent1, parent2);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromNullParameters() throws Exception {
    QcMaskVersion qcMaskVersion = QcMaskVersion.create(0, Collections.emptyList(),
        channelSegmentIdList, qcMaskCategory, qcMaskType1, rationale, startTime, endTime,
        creationInfoId);

    TestUtilities.checkStaticMethodValidatesNullArguments(QcMask.class, "from",
        new UUID(0L, 0L), processingChannelId, Collections.singletonList(qcMaskVersion));
  }

  @Test
  public void testFrom() {
    UUID qcMaskId = UUID.randomUUID();
    QcMaskVersion qcMaskVersion = QcMaskVersion
        .create(0L, Collections.emptyList(),
            channelSegmentIdList, qcMaskCategory, qcMaskType1, rationale, startTime,
            endTime, creationInfoId);

    QcMask qcMask = QcMask
        .from(qcMaskId, processingChannelId, Collections.singletonList(qcMaskVersion));

    assertEquals(qcMaskId, qcMask.getId());
    assertEquals(processingChannelId, qcMask.getProcessingChannelId());
    assertEquals(1, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(0L, currentVersion.getVersion());
    assertEquals(channelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(channelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertTrue(currentVersion.getType().isPresent());
    assertEquals(qcMaskType1, currentVersion.getType().get());
    assertEquals(qcMaskCategory, currentVersion.getCategory());
    assertEquals(rationale, currentVersion.getRationale());
    assertTrue(currentVersion.getStartTime().isPresent());
    assertEquals(startTime, currentVersion.getStartTime().get());
    assertTrue(currentVersion.getEndTime().isPresent());
    assertEquals(endTime, currentVersion.getEndTime().get());
    assertEquals(creationInfoId, currentVersion.getCreationInfoId());
    assertTrue(currentVersion.getParentQcMasks().isEmpty());
  }

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(QcMask.class, "create",
        processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime, creationInfoId);
  }

  @Test
  public void testCreateRejectedCategoryExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot create QcMask with REJECTED QcMaskCategory");

    QcMask.create(processingChannelId, parents, channelSegmentIdList, QcMaskCategory.REJECTED,
        qcMaskType1, rationale, startTime, endTime, creationInfoId);
  }

  @Test
  public void testCreate() {
    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    assertEquals(UUID.class, qcMask.getId().getClass());
    assertEquals(processingChannelId, qcMask.getProcessingChannelId());
    assertEquals(1, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(0L, currentVersion.getVersion());
    assertEquals(2, currentVersion.getParentQcMasks().size());
    assertTrue(currentVersion.getParentQcMasks().contains(parent1));
    assertTrue(currentVersion.getParentQcMasks().contains(parent2));
    assertEquals(channelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(channelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertTrue(currentVersion.getType().isPresent());
    assertEquals(qcMaskType1, currentVersion.getType().get());
    assertEquals(qcMaskCategory, currentVersion.getCategory());
    assertEquals(rationale, currentVersion.getRationale());
    assertTrue(currentVersion.getStartTime().isPresent());
    assertEquals(startTime, currentVersion.getStartTime().get());
    assertTrue(currentVersion.getEndTime().isPresent());
    assertEquals(endTime, currentVersion.getEndTime().get());
    assertEquals(creationInfoId, currentVersion.getCreationInfoId());
  }

  @Test
  public void testAddQcMaskVersionNullParameters() throws Exception {
    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    TestUtilities.checkMethodValidatesNullArguments(qcMask, "addQcMaskVersion",
        Collections.emptyList(), QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
        "Rationale SPIKE", Instant.parse("2007-12-03T10:35:30.00Z"),
        Instant.parse("2007-12-03T11:45:30.00Z"),
        UUID.randomUUID());
  }

  @Test
  public void testAddQcMaskVersionRejectedCategoryExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot add QcMaskVersion with REJECTED QcMaskCategory");

    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    List<UUID> newVersionChannelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    Instant newStartTime = Instant.parse("2007-12-03T10:35:30.00Z");
    Instant newEndTime = Instant.parse("2007-12-03T10:45:30.00Z");
    UUID newCreationInfoId = UUID.randomUUID();

    qcMask.addQcMaskVersion(newVersionChannelSegmentIdList, QcMaskCategory.REJECTED,
        QcMaskType.SPIKE,
        "Rationale SPIKE", newStartTime, newEndTime, newCreationInfoId);
  }

  @Test
  public void testAddQcMaskVersion() {
    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    List<UUID> newVersionChannelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    Instant newStartTime = Instant.parse("2007-12-03T10:35:30.00Z");
    Instant newEndTime = Instant.parse("2007-12-03T10:45:30.00Z");
    UUID newCreationInfoId = UUID.randomUUID();

    qcMask.addQcMaskVersion(newVersionChannelSegmentIdList, QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE", newStartTime, newEndTime, newCreationInfoId);

    assertEquals(2, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(1L, currentVersion.getVersion());
    assertEquals(newVersionChannelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(newVersionChannelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertEquals(QcMaskType.SPIKE, currentVersion.getType().get());
    assertEquals(QcMaskCategory.WAVEFORM_QUALITY, currentVersion.getCategory());
    assertEquals("Rationale SPIKE", currentVersion.getRationale());
    assertEquals(newStartTime, currentVersion.getStartTime().get());
    assertEquals(newEndTime, currentVersion.getEndTime().get());
    assertEquals(newCreationInfoId, currentVersion.getCreationInfoId());

    assertEquals(1, currentVersion.getParentQcMasks().size());

    QcMaskVersionReference parentQcMask = currentVersion.getParentQcMasks().iterator().next();

    assertEquals(qcMask.getId(), parentQcMask.getQcMaskId());
    assertEquals(0, parentQcMask.getQcMaskVersionId());
  }

  @Test
  public void testRejectNullParameters() throws Exception {
    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    TestUtilities
        .checkMethodValidatesNullArguments(qcMask, "reject", rationale, channelSegmentIdList,
            creationInfoId);
  }

  @Test
  public void testReject() {
    QcMask qcMask = QcMask
        .create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime, creationInfoId);

    final QcMaskVersionReference expectedParentReference = QcMaskVersionReference
        .from(qcMask.getId(), qcMask.getCurrentQcMaskVersion().getVersion());

    qcMask.reject("Rejected Rationale", Collections.emptyList(), creationInfoId);
    assertEquals(2, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(1L, currentVersion.getVersion());
    assertEquals(QcMaskCategory.REJECTED, currentVersion.getCategory());
    assertEquals("Rejected Rationale", currentVersion.getRationale());
    assertEquals(true, currentVersion.isRejected());
    assertEquals(creationInfoId, currentVersion.getCreationInfoId());
    assertEquals(Collections.singletonList(expectedParentReference),
        currentVersion.getParentQcMasks());
    assertEquals(Collections.emptyList(), currentVersion.getChannelSegmentIds());
    assertFalse(currentVersion.getType().isPresent());
    assertFalse(currentVersion.getStartTime().isPresent());
    assertFalse(currentVersion.getEndTime().isPresent());
  }

  @Test
  public void testEqualsHashCode() {
    final QcMask a = QcMask.create(processingChannelId, parents, channelSegmentIdList,
        qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime, creationInfoId);

    final QcMask b = QcMask
        .from(a.getId(), processingChannelId, a.qcMaskVersions().collect(Collectors.toList()));

    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {
    final QcMask a = QcMask.create(processingChannelId, parents, channelSegmentIdList,
        qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime, creationInfoId);

    // Different ID
    QcMask b = QcMask.create(processingChannelId, parents, channelSegmentIdList, qcMaskCategory,
        qcMaskType1,
        rationale, startTime, endTime, creationInfoId);
    assertNotEquals(a, b);

    // Different processing channel id
    b = QcMask.from(a.getId(), UUID.fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb"),
        a.qcMaskVersions().collect(Collectors.toList()));
    assertNotEquals(a, b);

    // Different versions
    b = QcMask.from(a.getId(), UUID.fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb"),
        Collections.emptyList());
    assertNotEquals(a, b);
  }
}
