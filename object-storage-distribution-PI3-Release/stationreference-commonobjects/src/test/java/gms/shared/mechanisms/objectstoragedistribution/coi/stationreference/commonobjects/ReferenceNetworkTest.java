package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import org.junit.Test;

public class ReferenceNetworkTest {

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceNetwork.class);
  }

  @Test
  public void testReferenceNetworkCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetwork.class, "create",
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime);
  }

  @Test
  public void testReferenceNetworkFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetwork.class, "from",
        TestFixtures.networkId,
        TestFixtures.networkVersionId,
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
  }

  @Test
  public void testReferenceNetworkCreateNewVersionNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetwork.class, "createNewVersion",
        TestFixtures.networkId,
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime);
  }

  /**
   * Test that arguments are saved correctly.  We check that the name was
   * converted to uppercase.
   * @throws Exception
   */
  @Test
  public void testReferenceNetworkCreate() {
    ReferenceNetwork alias = ReferenceNetwork.create(
        TestFixtures.networkName.toUpperCase(),
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime);
    assertEquals(TestFixtures.networkName.toUpperCase(), alias.getName());
    assertEquals(TestFixtures.description, alias.getDescription());
    assertEquals(TestFixtures.networkOrg, alias.getOrganization());
    assertEquals(TestFixtures.networkRegion, alias.getRegion());
    assertEquals(TestFixtures.comment, alias.getComment());
    assertEquals(TestFixtures.actualTime, alias.getActualChangeTime());
    assertNotEquals(TestFixtures.systemTime, alias.getSystemChangeTime());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was
   * converted to uppercase.
   * @throws Exception
   */
  @Test
  public void testReferenceNetworkFrom() {
    ReferenceNetwork network = ReferenceNetwork.from(
        TestFixtures.networkId,
        TestFixtures.networkVersionId,
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
    assertEquals(TestFixtures.networkId, network.getEntityId());
    assertEquals(TestFixtures.networkVersionId, network.getVersionId());
    assertEquals(TestFixtures.networkName.toUpperCase(), network.getName());
    assertEquals(TestFixtures.description, network.getDescription());
    assertEquals(TestFixtures.networkOrg, network.getOrganization());
    assertEquals(TestFixtures.networkRegion, network.getRegion());
    assertEquals(TestFixtures.comment, network.getComment());
    assertEquals(TestFixtures.actualTime, network.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, network.getSystemChangeTime());
  }

}
