package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import org.junit.Test;


/**
 *
 */
public class StationTests {

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Station.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Station.class, "from",
        TestFixtures.stationID, TestFixtures.stationName,
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.sites);
  }

  @Test
  public void createOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Station.class, "create",
        TestFixtures.stationName,
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.sites);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Station.create("",
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.sites);
  }
}


