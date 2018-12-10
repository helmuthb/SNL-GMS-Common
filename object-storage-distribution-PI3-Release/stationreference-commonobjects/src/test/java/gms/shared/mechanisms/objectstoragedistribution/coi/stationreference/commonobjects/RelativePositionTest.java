package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import org.junit.Test;

public class RelativePositionTest {

  private final double north = 1.453;
  private final double east = 3.01;
  private final double vertical = 0.05;
  private final double precision = 0.001;
  
  
  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(RelativePosition.class);
  }

  @Test
  public void testRelativePositionCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RelativePosition.class, "create", north, east, vertical);
  }

  @Test
  public void testRelativePositionFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RelativePosition.class, "from", north, east, vertical);
  }

  @Test
  public void testRelativePositionCreate() {
    RelativePosition relpos = RelativePosition.create(north, east, vertical);
    assertEquals(north, relpos.getNorthDisplacement(), precision);
    assertEquals(east, relpos.getEastDisplacement(), precision);
    assertEquals(vertical, relpos.getVerticalDisplacement(), precision);

  }

  @Test
  public void testRelativePositionFrom() {
    RelativePosition relpos = RelativePosition.from(north, east, vertical);
    assertEquals(north, relpos.getNorthDisplacement(), precision);
    assertEquals(east, relpos.getEastDisplacement(), precision);
    assertEquals(vertical, relpos.getVerticalDisplacement(), precision);

  }

}
