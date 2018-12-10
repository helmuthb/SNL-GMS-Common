package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import org.junit.Test;


/**
 *
 */
public class SiteTests {

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Site.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Site.class, "from",
        TestFixtures.siteID, TestFixtures.siteName,
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.channels);
  }

  @Test
  public void createOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Site.class, "create",
        TestFixtures.siteName,
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.channels);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Site.create("",
        TestFixtures.lat, TestFixtures.lon, TestFixtures.elev,
        TestFixtures.channels);
  }
}


