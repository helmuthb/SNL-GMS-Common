package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import java.util.UUID;

/**
 * Tests the serialization and deserialization of {@link QcMask} objects to/from {@link QcMaskDto}
 * objects.
 *
 * Does not test for exception or other error conditions handled by the QcMask creation operations.
 *
 * Created by jrhipp on 9/12/17.
 */
public class QcMaskDtoTests {

  private final UUID processingChannelId = UUID.randomUUID();
  private final List<QcMaskVersionReference> parents = Arrays.asList(
      QcMaskVersionReference.from(UUID.randomUUID(), 1),
      QcMaskVersionReference.from(UUID.randomUUID(), 2));
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays
      .asList(channelSegmentId1, channelSegmentId2);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
  private final UUID creationInfoId = UUID.randomUUID();


  @Test
  public void testSerialization() throws Exception {
    // Use the builder to quickly assemble a list from QcMaskVersion to use in testing
    QcMask qcMask = QcMask
        .create(this.processingChannelId, parents, this.channelSegmentIdList, this.qcMaskCategory,
            this.qcMaskType1,
            this.rationale, this.startTime, this.endTime, this.creationInfoId);

    String json = TestFixtures.objMapper.writeValueAsString(qcMask);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    QcMask deserializedQcMask = callDeserializer(json);
    assertNotNull(deserializedQcMask);
    assertEquals(qcMask, deserializedQcMask);
  }

  private static QcMask callDeserializer(String json) throws Exception {
    return TestFixtures.objMapper.readValue(json, QcMask.class);
  }
}
