package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link QcMaskVersionReference} creation and usage semantics
 */
public class QcMaskVersionReferenceTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final UUID qcMaskId = new UUID(0L, 0L);
  private final long qcMaskVersionId = 0L;

  @Test
  public void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(QcMaskVersionReference.class,
        "from", qcMaskId, qcMaskVersionId);
  }

  @Test
  public void testParentIdExpectEqualsIdValue() {
    final UUID id1 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id2 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id3 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");

    QcMaskVersionReference qcMaskVersionReference1 = QcMaskVersionReference
        .from(id1, qcMaskVersionId);
    QcMaskVersionReference qcMaskVersionReference2 = QcMaskVersionReference
        .from(id2, qcMaskVersionId);
    QcMaskVersionReference qcMaskVersionReference3 = QcMaskVersionReference
        .from(id3, qcMaskVersionId);

    assertEquals(qcMaskVersionReference1.getQcMaskId(),
        qcMaskVersionReference2.getQcMaskId());
    assertEquals(qcMaskVersionReference2.getQcMaskId(),
        qcMaskVersionReference3.getQcMaskId());
  }

  @Test
  public void testParentIdExpectNotEqualsIdValue() {
    final UUID id1 = UUID
        .fromString("04e7d88d-13ef-4e06-ab63-f81c6a170784");
    final UUID id2 = UUID
        .fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb");

    QcMaskVersionReference qcMaskVersionReference1 = QcMaskVersionReference
        .from(id1, qcMaskVersionId);
    QcMaskVersionReference qcMaskVersionReference2 = QcMaskVersionReference
        .from(id2, qcMaskVersionId);

    assertNotEquals(qcMaskVersionReference1.getQcMaskId(),
        qcMaskVersionReference2.getQcMaskId());
  }

  @Test
  public void testParentVersionExpectEqualsIdValue() {
    final long id1 = 3;
    final long id2 = 3;
    final long id3 = 3;

    QcMaskVersionReference qcMaskVersionReference1 = QcMaskVersionReference.from(qcMaskId, id1);
    QcMaskVersionReference qcMaskVersionReference2 = QcMaskVersionReference.from(qcMaskId, id2);
    QcMaskVersionReference qcMaskVersionReference3 = QcMaskVersionReference.from(qcMaskId, id3);

    assertEquals(qcMaskVersionReference1.getQcMaskVersionId(),
        qcMaskVersionReference2.getQcMaskVersionId());
    assertEquals(qcMaskVersionReference2.getQcMaskVersionId(),
        qcMaskVersionReference3.getQcMaskVersionId());
  }

  @Test
  public void testParentVersionExpectNotEqualsIdValue() {
    final long id1 = 3;
    final long id2 = 4;

    QcMaskVersionReference qcMaskVersionReference1 = QcMaskVersionReference.from(qcMaskId, id1);
    QcMaskVersionReference qcMaskVersionReference2 = QcMaskVersionReference.from(qcMaskId, id2);

    assertNotEquals(qcMaskVersionReference1.getQcMaskVersionId(),
        qcMaskVersionReference2.getQcMaskVersionId());
  }

}
