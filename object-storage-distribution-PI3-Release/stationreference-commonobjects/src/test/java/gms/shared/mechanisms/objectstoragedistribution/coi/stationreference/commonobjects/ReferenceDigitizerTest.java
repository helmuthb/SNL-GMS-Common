package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ReferenceDigitizerTest {


  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceDigitizer.class);
  }

  @Test
  public void testReferenceDigitizerCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceDigitizer.class, "create",
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.source,
        TestFixtures.digitComment, "desc");
  }

  @Test
  public void testReferenceDigitizerFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceDigitizer.class, "from",
        TestFixtures.digitizerId, UUID.randomUUID(),
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source, "desc",
        TestFixtures.digitComment);
  }

  @Test
  public void testReferenceDigitizerCreateNewVersionNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceDigitizer.class, "createNewVersion",
        TestFixtures.digitizerId,
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.source,
        TestFixtures.digitComment,
        "desc");
  }

  @Test
  public void testReferenceDigitizerCreate() {
    String desc = "description";
    ReferenceDigitizer digitizer = ReferenceDigitizer.create(
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.source,
        TestFixtures.digitComment, desc);
    assertEquals(TestFixtures.digitName, digitizer.getName());
    assertEquals(TestFixtures.digitManufacturer, digitizer.getManufacturer());
    assertEquals(TestFixtures.digitModel, digitizer.getModel());
    assertEquals(TestFixtures.digitSerial, digitizer.getSerialNumber());
    assertEquals(TestFixtures.actualTime, digitizer.getActualChangeTime());
    assertNotEquals(TestFixtures.systemTime, digitizer.getSystemChangeTime());
    assertEquals(TestFixtures.source, digitizer.getInformationSource());
    assertEquals(TestFixtures.digitComment, digitizer.getComment());
    assertEquals(desc, digitizer.getDescription());
  }

  @Test
  public void testReferenceDigitizerFrom() {
    UUID versionId = UUID.randomUUID();
    String desc = "description";
    ReferenceDigitizer digitizer = ReferenceDigitizer.from(
        TestFixtures.digitizerId, versionId,
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.digitComment, desc);
    assertEquals(TestFixtures.digitizerId, digitizer.getEntityId());
    assertEquals(versionId, digitizer.getVersionId());
    assertEquals(TestFixtures.digitName, digitizer.getName());
    assertEquals(TestFixtures.digitManufacturer, digitizer.getManufacturer());
    assertEquals(TestFixtures.digitModel, digitizer.getModel());
    assertEquals(TestFixtures.digitSerial, digitizer.getSerialNumber());
    assertEquals(TestFixtures.actualTime, digitizer.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, digitizer.getSystemChangeTime());
    assertEquals(TestFixtures.source, digitizer.getInformationSource());
    assertEquals(TestFixtures.digitComment, digitizer.getComment());
    assertEquals(desc, digitizer.getDescription());
  }
}
