package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceSiteMembershipTest {
  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID siteId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID channelId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now();
  private final StatusType status = StatusType.ACTIVE;




  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceSiteMembership.class);
  }

  @Test
  public void testReferenceSiteMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSiteMembership.class, "create", comment, actualTime,
        siteId, channelId, status);
  }

  @Test
  public void testReferenceSiteMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSiteMembership.class, "from", id, comment, actualTime, systemTime,
        siteId, channelId, status);

  }
    /**
     * Test that arguments are saved correctly.
     * @throws Exception
     */
    @Test
    public void testReferenceSiteMembCreate() {
      ReferenceSiteMembership alias = ReferenceSiteMembership.create(comment, actualTime,
          siteId, channelId, status);
      assertNotEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertNotEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(siteId, alias.getSiteId());
      assertEquals(channelId, alias.getChannelId());
      assertEquals(status, alias.getStatus());
    }


    /**
     * Test that arguments are saved correctly.  We check that the name was
     * converted to uppercase.
     * @throws Exception
     */
    @Test
    public void testReferenceSiteMembFrom() {
      ReferenceSiteMembership alias = ReferenceSiteMembership.from(id, comment, actualTime,
          systemTime, siteId, channelId, status);
      assertEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(siteId, alias.getSiteId());
      assertEquals(channelId, alias.getChannelId());
      assertEquals(status, alias.getStatus());
    }



}
