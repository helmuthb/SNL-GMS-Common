package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;


/**
 * Tests the serialization and deserialization of {@link QcMaskVersion} objects to/from {@link
 * QcMaskVersionDto} objects.
 *
 * Does not test for exception or other error conditions handled by the QcMaskVersion creation
 * operations.
 *
 * Created by jrhipp on 9/12/17.
 */
public class QcMaskVersionDtoTests {

  private final UUID qcMaskVersionParentId = UUID.randomUUID();
  private final long qcMaskVersionParentVersion = 5L;

  private final long qcMaskVersionId = 6L;

  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays
      .asList(channelSegmentId1, channelSegmentId2);

  private final QcMaskType qcMaskType = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
  private final UUID creationInfoId = UUID.randomUUID();

  @Test
  public void testToDto() throws Exception {
    List<QcMaskVersionReference> qcMaskVersionReferences = Collections
        .singletonList(QcMaskVersionReference
            .from(qcMaskVersionParentId, qcMaskVersionParentVersion));
    QcMaskVersion qcMaskVersion = QcMaskVersion
        .create(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
            qcMaskCategory, qcMaskType, rationale, startTime, endTime, creationInfoId);

    String json = TestFixtures.objMapper.writeValueAsString(qcMaskVersion);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    QcMaskVersion deserializedQcMaskVersion = callDeserializer(json);
    assertNotNull(deserializedQcMaskVersion);
    assertEquals(qcMaskVersion, deserializedQcMaskVersion);
  }

  @Test
  public void testToDtoRejectedVersion() throws Exception {
    List<QcMaskVersionReference> qcMaskVersionReferences = Collections
        .singletonList(QcMaskVersionReference
            .from(qcMaskVersionParentId, qcMaskVersionParentVersion));

    QcMaskVersion qcMaskVersion = QcMaskVersion
        .from(qcMaskVersionId, qcMaskVersionReferences, channelSegmentIdList,
            QcMaskCategory.REJECTED, null, rationale, null, null, creationInfoId);

    String json = TestFixtures.objMapper.writeValueAsString(qcMaskVersion);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    QcMaskVersion deserializedQcMaskVersion = callDeserializer(json);
    assertNotNull(deserializedQcMaskVersion);
    assertEquals(qcMaskVersion, deserializedQcMaskVersion);
  }

  private static QcMaskVersion callDeserializer(String json) throws Exception {
    return TestFixtures.objMapper.readValue(json, QcMaskVersion.class);
  }
}
