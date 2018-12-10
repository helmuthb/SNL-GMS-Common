package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReferenceSiteTest {

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceSite.class);
  }

  @Test
  public void testReferenceSiteCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSite.class, "create",
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
  }

  @Test
  public void testReferenceSiteFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSite.class, "from",
        TestFixtures.siteId,
        UUID.randomUUID(),
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
  }

  @Test
  public void testReferenceSiteCreateNewVersionNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSite.class, "createNewVersion",
        TestFixtures.siteId,
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
  }

  @Test
  public void testAddAlias() {
    ReferenceSite site = ReferenceSite.create(
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.position,
        new ArrayList<>());
    site.addAlias(TestFixtures.siteAlias);
    assertTrue(site.getAliases().size() == 1);
    assertEquals(site.getAliases().get(0), TestFixtures.siteAlias);
  }

  @Test
  public void testReferenceSiteCreate() {
    ReferenceSite site = ReferenceSite.create(
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
    assertEquals(TestFixtures.siteName.toUpperCase(), site.getName());
    assertEquals(TestFixtures.description, site.getDescription());
    assertEquals(TestFixtures.source, site.getSource());
    assertEquals(TestFixtures.comment, site.getComment());
    assertEquals(TestFixtures.latitude, site.getLatitude(), TestFixtures.precision);
    assertEquals(TestFixtures.longitude, site.getLongitude(), TestFixtures.precision);
    assertEquals(TestFixtures.elevation, site.getElevation(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, site.getActualChangeTime());
    assertNotEquals(TestFixtures.systemTime, site.getSystemChangeTime());
    assertEquals(TestFixtures.siteAliases, site.getAliases());
  }

  @Test
  public void testReferenceSiteFrom() {
    UUID versionId = UUID.randomUUID();
    ReferenceSite site = ReferenceSite.from(
        TestFixtures.siteId, versionId,
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
    assertEquals(TestFixtures.siteId, site.getEntityId());
    assertEquals(versionId, site.getVersionId());
    assertEquals(TestFixtures.siteName.toUpperCase(), site.getName());
    assertEquals(TestFixtures.description, site.getDescription());
    assertEquals(TestFixtures.source, site.getSource());
    assertEquals(TestFixtures.comment, site.getComment());
    assertEquals(TestFixtures.latitude, site.getLatitude(), TestFixtures.precision);
    assertEquals(TestFixtures.longitude, site.getLongitude(), TestFixtures.precision);
    assertEquals(TestFixtures.elevation, site.getElevation(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, site.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, site.getSystemChangeTime());
    assertEquals(TestFixtures.siteAliases, site.getAliases());
  }


}
