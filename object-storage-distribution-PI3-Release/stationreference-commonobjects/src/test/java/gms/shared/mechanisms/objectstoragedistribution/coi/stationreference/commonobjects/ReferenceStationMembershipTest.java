package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceStationMembershipTest {
  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID stationId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID siteId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now();
  private final StatusType status = StatusType.ACTIVE;


  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceStationMembership.class);
  }

  @Test
  public void testReferenceStationMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStationMembership.class, "create", comment, actualTime,
        stationId, siteId, status);
  }

  @Test
  public void testReferenceStationMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStationMembership.class, "from", id, comment, actualTime, systemTime,
        stationId, siteId, status);

  }
    /**
     * Test that arguments are saved correctly.
     * @throws Exception
     */
    @Test
    public void testReferenceStationMembCreate() {
      ReferenceStationMembership alias = ReferenceStationMembership.create(comment, actualTime,
          stationId, siteId, status);
      assertNotEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertNotEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(stationId, alias.getStationId());
      assertEquals(status, alias.getStatus());
    }


    /**
     * Test that arguments are saved correctly.  We check that the name was
     * converted to uppercase.
     * @throws Exception
     */
    @Test
    public void testReferenceStationMembFrom() {
      ReferenceStationMembership alias = ReferenceStationMembership.from(id, comment, actualTime,
          systemTime, stationId, siteId, status);
      assertEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(stationId, alias.getStationId());
      assertEquals(status, alias.getStatus());
    }



}
